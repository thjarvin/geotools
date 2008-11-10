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
package org.geotools.data.db2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.jdbc.BasicSQLDialect;
import org.geotools.jdbc.JDBCDataStore;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


public class DB2SQLDialectBasic extends BasicSQLDialect {

	private DB2SQLDialect delegate  = null;
	
    public DB2SQLDialectBasic(JDBCDataStore dataStore) {
        super(dataStore);
        delegate  = new DB2SQLDialect(dataStore);
    }
    
    /* (non-Javadoc)
     * @see org.geotools.jdbc.SQLDialect#createCRS(int, java.sql.Connection)
     *      
     */
    @Override
    public CoordinateReferenceSystem createCRS(int srid, Connection cx) throws SQLException {
    	return delegate.createCRS(srid, cx);
    }
    
    @Override
    public FilterToSQL createFilterToSQL() {
    	return new DB2FilterToSQL();
    }

    
    @Override
    public void encodePrimaryKey(String column, StringBuffer sql) {
    	delegate.encodePrimaryKey(column, sql);
    }
    

    @Override
    public String getGeometryTypeName(Integer type) {
    	return delegate.getGeometryTypeName(type);
    	        
    }

    @Override
    public Integer getGeometrySRID(String schemaName, String tableName, String columnName,
        Connection cx) throws SQLException {
    	
		return delegate.getGeometrySRID(schemaName, tableName, columnName, cx);

    }
    

    
    public void encodeGeometryColumn(GeometryDescriptor gatt,StringBuffer sql) {
    	delegate.encodeGeometryColumn(gatt, sql);
    }
    
    public void encodeGeometryColumn(GeometryDescriptor gatt, int srid, StringBuffer sql) {
    	delegate.encodeGeometryColumn(gatt, srid, sql);    	
    }


    @Override    
    public void encodeGeometryEnvelope(String tableName,String geometryColumn, StringBuffer sql) {
    	delegate.encodeGeometryEnvelope(tableName, geometryColumn, sql);
    }

    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
                Connection cx) throws SQLException, IOException {
    	
    	return delegate.decodeGeometryEnvelope(rs, column, cx);
    }

    
    @Override
    public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql) throws IOException {
    	DB2Util.encodeGeometryValue(value, srid, sql);
    }
    
    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, String name,
        GeometryFactory factory, Connection cx ) throws IOException, SQLException {
    	return delegate.decodeGeometryValue(descriptor, rs, name, factory, cx);    	
    }

    
    @Override
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
    	delegate.registerClassToSqlMappings(mappings);
    }

    @Override
    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
    	delegate.registerSqlTypeToClassMappings(mappings);
    }

    @Override
    public void registerSqlTypeNameToClassMappings(Map<String, Class<?>> mappings) {
    	delegate.registerSqlTypeNameToClassMappings(mappings);
    }


    @Override
    public void postCreateTable(String schemaName, SimpleFeatureType featureType, Connection cx)
    throws SQLException {
    	delegate.postCreateTable(schemaName, featureType, cx);    	    	
    }

        	
    @Override
    public String getSequenceForColumn(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
    	return delegate.getSequenceForColumn(schemaName, tableName, columnName, cx);    	
    }
    
    @Override
    public Object getNextSequenceValue(String schemaName, String sequenceName,
            Connection cx) throws SQLException {
    	return delegate.getNextSequenceValue(schemaName, sequenceName, cx);
        
    }

}