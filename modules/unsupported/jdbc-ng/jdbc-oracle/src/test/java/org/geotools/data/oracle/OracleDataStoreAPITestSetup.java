/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.oracle;

import org.geotools.jdbc.JDBCDataStoreAPITestSetup;

public class OracleDataStoreAPITestSetup extends JDBCDataStoreAPITestSetup {

    protected OracleDataStoreAPITestSetup(OracleTestSetup delegate) {
        super(delegate);
    }
    
    @Override
    protected void createLakeTable() throws Exception {
        //set up table
        run("CREATE TABLE lake (fid int, id int, "
            + "geom MDSYS.SDO_GEOMETRY, name varchar(255), PRIMARY KEY (fid) )");
        run("CREATE SEQUENCE lake_pkey_seq START WITH 0 MINVALUE 0");
        run("CREATE TRIGGER lake_pkey_trigger " + 
            "BEFORE INSERT ON lake " + 
            "FOR EACH ROW " + 
              "BEGIN " + 
                "SELECT lake_pkey_seq.nextval INTO :new.fid FROM dual; " + 
              "END;");
        run("INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)" 
                + " VALUES ('lake','geom',MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',-180,180,0.5), " 
                + "MDSYS.SDO_DIM_ELEMENT('Y',-90,90,0.5)), 4326)");   
        run("CREATE INDEX LAKE_GEOM_IDX ON lake(GEOM) INDEXTYPE IS MDSYS.SPATIAL_INDEX" //
                + " PARAMETERS ('SDO_INDX_DIMS=2 LAYER_GTYPE=\"POLYGON\"')");
        
        //insert data
        run("INSERT INTO lake (id,geom,name) VALUES ( 0,"
            + "MDSYS.SDO_GEOMETRY( 2003, 4326, NULL, SDO_ELEM_INFO_ARRAY(1,1003,1), "
            + "SDO_ORDINATE_ARRAY(2,6, 14,8, 16,6, 16,4, 14,4, 12,6)), 'muddy')");
    }
    

    @Override
    protected void createRiverTable() throws Exception {
        //set up table
        run("CREATE TABLE river (fid int, id int, "
            + "geom MDSYS.SDO_GEOMETRY, river varchar(255), flow float, PRIMARY KEY (fid) )");
        run("CREATE SEQUENCE river_pkey_seq START WITH 0 MINVALUE 0");
        run("CREATE TRIGGER river_pkey_trigger " + 
            "BEFORE INSERT ON river " + 
            "FOR EACH ROW " + 
              "BEGIN " + 
                "SELECT river_pkey_seq.nextval INTO :new.fid FROM dual; " + 
              "END;");
        run("INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)" 
                + " VALUES ('river','geom',MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',-180,180,0.5), " 
                + "MDSYS.SDO_DIM_ELEMENT('Y',-90,90,0.5)), 4326)");   
        run("CREATE INDEX RIVER_GEOM_IDX ON RIVER(GEOM) INDEXTYPE IS MDSYS.SPATIAL_INDEX" //
                + " PARAMETERS ('SDO_INDX_DIMS=2 LAYER_GTYPE=\"LINE\"')");
        
        //insert data
        run("INSERT INTO river (id,geom,river, flow ) VALUES (0,"
            + "MDSYS.SDO_GEOMETRY( 2002, 4326, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), "
            + "MDSYS.SDO_ORDINATE_ARRAY(5,5, 7,4, 7,5, 9,7, 13,7, 7,5, 9,3, 11,3))," + "'rv1', 4.5)");
        run("INSERT INTO river (id,geom,river, flow ) VALUES (1,"
                + "MDSYS.SDO_GEOMETRY( 2002, 4326, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), "
                + "MDSYS.SDO_ORDINATE_ARRAY(4,6, 4,8, 6,10))," + "'rv2', 3.0)");
    }

    @Override
    protected void createRoadTable() throws Exception {
        //set up table
        run("CREATE TABLE road (fid int, id int, "
            + "geom MDSYS.SDO_GEOMETRY, name varchar(255), PRIMARY KEY (fid) )");
        run("CREATE SEQUENCE road_pkey_seq START WITH 0 MINVALUE 0");
        run("CREATE TRIGGER road_pkey_trigger " + 
            "BEFORE INSERT ON road " + 
            "FOR EACH ROW " + 
              "BEGIN " + 
                "SELECT road_pkey_seq.nextval INTO :new.fid FROM dual; " + 
              "END;");
        run("INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)" 
            + " VALUES ('road','geom',MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',-180,180,0.5), " 
            + "MDSYS.SDO_DIM_ELEMENT('Y',-90,90,0.5)), 4326)");
        run("CREATE INDEX ROAD_GEOM_IDX ON ROAD(GEOM) INDEXTYPE IS MDSYS.SPATIAL_INDEX" //
                + " PARAMETERS ('SDO_INDX_DIMS=2 LAYER_GTYPE=\"LINE\"')");
              
        //insert data
        run("INSERT INTO road (id,geom,name) VALUES (0,"
            + "MDSYS.SDO_GEOMETRY( 2002, 4326, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), "
            + "MDSYS.SDO_ORDINATE_ARRAY(1,1, 2,2, 4,2, 5,1))," + "'r1')");
        run("INSERT INTO road (id,geom,name) VALUES (1,"
                + "MDSYS.SDO_GEOMETRY( 2002, 4326, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), "
                + "MDSYS.SDO_ORDINATE_ARRAY(3,0, 3,2, 3,3, 3,4))," + "'r2')");
        run("INSERT INTO road (id,geom,name) VALUES (3,"
                + "MDSYS.SDO_GEOMETRY( 2002, 4326, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), "
                + "MDSYS.SDO_ORDINATE_ARRAY(3,2, 4,2, 5,3))," + "'r3')");
    }

    @Override
    protected void dropLakeTable() throws Exception {
        runSafe( "DROP TRIGGER lake_pkey_trigger");
        runSafe( "DROP SEQUENCE lake_pkey_seq");
        runSafe( "DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'LAKE'" );
        runSafe( "DROP TABLE lake");
        
    }

    @Override
    protected void dropRiverTable() throws Exception {
        runSafe( "DROP TRIGGER river_pkey_trigger");
        runSafe( "DROP SEQUENCE river_pkey_seq");
        runSafe( "DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'RIVER'" );
        runSafe( "DROP TABLE river");
    }

    @Override
    protected void dropRoadTable() throws Exception {
        runSafe( "DROP TRIGGER road_pkey_trigger");
        runSafe( "DROP SEQUENCE road_pkey_seq");
        runSafe( "DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'ROAD'" );
        runSafe( "DROP TABLE road");
    }
    
    @Override
    protected void dropBuildingTable() throws Exception {
        runSafe( "DROP TABLE building");
        run("DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'BUILDING'");
    }


}
