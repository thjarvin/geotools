package org.geotools.gce.imagemosaic.jdbc;

import org.geotools.data.jdbc.datasource.DataSourceFinder;

import java.io.File;

import java.net.URL;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;


public abstract class JDBCSetup {
    protected DataSource dataSource;
    protected Config config;

    public JDBCSetup(Config config) {
		super();
		this.config = config;
	}

    static JDBCSetup getJDBCSetup(Config config) {
        SpatialExtension type = config.getSpatialExtension();
        if (type == null)  return null;

        if (type == SpatialExtension.DB2) {
            return new DB2Setup(config);
        } else if (type == SpatialExtension.POSTGIS) {
        	return new PostgisSetup(config);
        } else if (type == SpatialExtension.MYSQL) {
        	return new MySqlSetup(config);
        } else if (type == SpatialExtension.UNIVERSAL) {
        	return new H2Setup(config);
        } else if (type == SpatialExtension.ORACLE) {
        	return new OracleSetup(config);
        } else {
        	return null;
        }

    }
    
	public abstract String getConfigUrl();

    protected abstract String getBLOBSQLType();

    protected abstract String getMulitPolygonSQLType();

    protected abstract String getDriverClassName();

    protected abstract String getJDBCUrl(String host, Integer port,
        String dbName);

    protected abstract String getXMLConnectFragmentName();

    protected Config getConfig()  {
        return config;
    }

    protected String[] getTileTableNames() {
        return new String[] {  /* "TILES0", */"TILES1", "TILES2", "TILES3" };
    }

    protected String[] getSpatialTableNames() {
        return new String[] {  /* "SPATIAL0", */"SPATIAL1", "SPATIAL2", "SPATIAL3" };
    }

    private DataSource getDataSource() throws Exception {
        if (dataSource != null) {
            return dataSource;
        }

        Config config = getConfig();
        dataSource = DataSourceFinder.getDataSource(config.getDataSourceParams());

        return dataSource;
    }

    protected Connection getConnection() throws Exception {
        Connection con = getDataSource().getConnection();
        con.setAutoCommit(false);

        return con;
    }

    String getDropTableStatemnt(String tableName) {
    	return "drop table " + tableName;
    }
    private void drop(String tableName, Connection con) {
        try {
            con.prepareStatement(getDropTableStatemnt(tableName)).execute();
        } catch (SQLException e) {
            // e.printStackTrace();
        }
    }

    
    protected void registerSpatial(String tn, Connection con) throws Exception{

    }

    
    protected void unregisterSpatial(String tn, Connection con)
        throws Exception {
    }

    protected abstract String getCreateIndexStatement(String tn) throws Exception;
    
    protected  void createIndex(String tn, Connection con) 
        throws Exception {
    	con.prepareStatement(getCreateIndexStatement(tn)).execute();
    }
        

    String getDropIndexStatment(String tn) {
    	return "drop index IX_" + tn;
    }
    
    private void dropIndex(String tn, Connection con)
        throws Exception {
        try {
            con.prepareStatement(getDropIndexStatment(tn)).execute();
        } catch (SQLException e) {
        }
    }

    public void dropAll() throws Exception {
        Connection con = null;

        try {
            con = getConnection();
        } catch (Exception e) {
            return;
        }

        for (String tn : getTileTableNames())
            drop(tn, con);

        con.commit();
        con.close();

        con = getConnection();
        drop(getConfig().getMasterTable(), con);

        for (String tn : getSpatialTableNames())
            dropIndex(tn, con);

        for (String tn : getSpatialTableNames())
            unregisterSpatial(tn, con);

        for (String tn : getSpatialTableNames())
            drop(tn, con);

        con.commit();
        con.close();
    }

    protected String getDoubleSQLType() {
        return "DOUBLE";
    }

    String getCreateMasterStatement() throws Exception {
        Config config = getConfig();
        String doubleType = getDoubleSQLType();
        String statement = " CREATE TABLE " + config.getMasterTable();
        statement += ("(" + config.getCoverageNameAttribute() +
        " CHARACTER (64)  NOT NULL");
        statement += ("," + config.getSpatialTableNameAtribute() +
        " VARCHAR (256)  NOT NULL");
        statement += ("," + config.getTileTableNameAtribute() +
        " VARCHAR (256)  NOT NULL");
        statement += ("," + config.getResXAttribute() + " " + doubleType + "," +
        config.getResYAttribute() + " " + doubleType);
        statement += ("," + config.getMinXAttribute() + " " + doubleType + "," +
        config.getMinYAttribute() + " " + doubleType);
        statement += ("," + config.getMaxXAttribute() + " " + doubleType + "," +
        config.getMaxYAttribute() + " " + doubleType);
        statement += ",CONSTRAINT MASTER_PK PRIMARY KEY (";
        statement += (config.getCoverageNameAttribute() + "," +
        config.getSpatialTableNameAtribute() + "," +
        config.getTileTableNameAtribute());
        statement += "))";

        return statement;
    }

    String getCreateTileTableStatement(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += ("(" + getConfig().getKeyAttributeNameInTileTable() +
        " CHAR(64) NOT NULL ");
        statement += ("," + getConfig().getBlobAttributeNameInTileTable() +
        " " + getBLOBSQLType());
        statement += (",CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInTileTable());
        statement += "))";

        return statement;
    }

    protected String getCreateSpatialTableStatement(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" ( " + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL, " +
        getConfig().getGeomAttributeNameInSpatialTable() + " " +
        getMulitPolygonSQLType() + " NOT NULL ");
        statement += (",CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    protected String getCreateSpatialTableStatementJoined(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" ( " + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL, " +
        getConfig().getGeomAttributeNameInSpatialTable() + " " +
        getMulitPolygonSQLType() + " NOT NULL ");
        statement += ("," + getConfig().getBlobAttributeNameInTileTable() +
        " " + getBLOBSQLType());
        statement += (",CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    public void createAll(String outputDir) throws Exception {
        String createMaster = getCreateMasterStatement();
        Connection con = getConnection();
        con.prepareStatement(createMaster).execute();

        for (String tn : getTileTableNames()) {
            con.prepareStatement(getCreateTileTableStatement(tn)).execute();
        }

        for (String tn : getSpatialTableNames()) {
            con.prepareStatement(getCreateSpatialTableStatement(tn)).execute();
            registerSpatial(tn, con);
        }

        con.commit();

        for (int i = 0; i < getTileTableNames().length; i++) {
            URL shapeFileUrl = new URL("file:" +
            		outputDir + i + File.separator +
                    "index.shp");
            Import imp = new Import(getConfig(), getSpatialTableNames()[i],
                    getTileTableNames()[i], shapeFileUrl, "LOCATION", 2, con,
                    true);
            imp.fillSpatialTable();

        }

        for (String tn : getSpatialTableNames()) {
            createIndex(tn, con);
        }

        con.commit();
        con.close();
    }

    public void createAllJoined(String outputDir) throws Exception {
        String createMaster = getCreateMasterStatement();
        Connection con = getConnection();
        con.prepareStatement(createMaster).execute();

        for (String tn : getSpatialTableNames()) {
            con.prepareStatement(getCreateSpatialTableStatementJoined(tn))
               .execute();
            registerSpatial(tn, con);
        }

        con.commit();

        for (int i = 0; i < getTileTableNames().length; i++) {
            URL csvFileUrl = new URL("file:" +
                    outputDir + i + File.separator +
                    "index.csv");
            Import imp = new Import(getConfig(), getSpatialTableNames()[i],
                    getSpatialTableNames()[i], csvFileUrl, ";", 2, con, false);
            imp.fillSpatialTable();

        }

        for (String tn : getSpatialTableNames()) {
            createIndex(tn, con);
        }

        con.commit();
        con.close();
    }

}
