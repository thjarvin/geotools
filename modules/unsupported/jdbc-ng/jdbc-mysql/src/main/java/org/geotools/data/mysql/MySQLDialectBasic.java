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
package org.geotools.data.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.jdbc.BasicSQLDialect;
import org.geotools.jdbc.JDBCDataStore;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * MySQL database dialect based on basic (non-prepared) statements.
 * 
 * @author Justin Deoliveira, OpenGEO
 *
 *
 * @source $URL$
 */
public class MySQLDialectBasic extends BasicSQLDialect {
    
    MySQLDialect delegate;
    
    public MySQLDialectBasic(JDBCDataStore dataStore) {
        super( dataStore );
        delegate = new MySQLDialect(dataStore);
    }
    
    public void setStorageEngine(String storageEngine) {
        delegate.setStorageEngine(storageEngine);
    }
    
    @Override
    public String getNameEscape() {
        return delegate.getNameEscape();
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

    @Override
    public void encodeColumnName(String raw, StringBuffer sql) {
        delegate.encodeColumnName(raw, sql);
    }
    
    @Override
    public void encodeGeometryColumn(GeometryDescriptor gatt, int srid, StringBuffer sql) {
        delegate.encodeGeometryColumn(gatt, srid, sql);
    }
    
    @Override
    public void encodeColumnType(String sqlTypeName, StringBuffer sql) {
        delegate.encodeColumnType(sqlTypeName, sql);
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
    public void registerSqlTypeToSqlTypeNameOverrides(
            Map<Integer, String> overrides) {
        delegate.registerSqlTypeToSqlTypeNameOverrides(overrides);
    }
    
    @Override
    public void encodePostCreateTable(String tableName, StringBuffer sql) {
        delegate.encodePostCreateTable(tableName, sql);
    }

    @Override
    public void encodePostColumnCreateTable(AttributeDescriptor att, StringBuffer sql) {
        delegate.encodePostColumnCreateTable(att, sql);
    }
    
    @Override
    public void postCreateTable(String schemaName, SimpleFeatureType featureType, Connection cx)
            throws SQLException, IOException {
        delegate.postCreateTable(schemaName, featureType, cx);
    }
    
    @Override
    public void encodePrimaryKey(String column, StringBuffer sql) {
        delegate.encodePrimaryKey(column, sql);
    }

    @Override
    public boolean lookupGeneratedValuesPostInsert() {
        return delegate.lookupGeneratedValuesPostInsert();
    }
    
    @Override
    public Object getNextAutoGeneratedValue(String schemaName,
            String tableName, String columnName, Connection cx)
            throws SQLException {
        return delegate.getNextAutoGeneratedValue(schemaName, tableName, columnName, cx);
    }
    
    @Override
    public Object getLastAutoGeneratedValue(String schemaName, String tableName, String columnName,
            Connection cx) throws SQLException {
        return delegate.getLastAutoGeneratedValue(schemaName, tableName, columnName, cx);
    }
    
    @Override
    public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql)
            throws IOException {
        sql.append("GeomFromText('");
        sql.append(new WKTWriter().write(value));
        sql.append("')");
    }

    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor,
            ResultSet rs, String column, GeometryFactory factory, Connection cx)
            throws IOException, SQLException {
        byte[] bytes = rs.getBytes(column);
        if ( bytes == null ) {
            return null;
        }
        
        try {
            return new WKBReader(factory).read(bytes);
        } catch (ParseException e) {
            String msg = "Error decoding wkb";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    @Override
    public void encodeGeometryEnvelope(String tableName, String geometryColumn,
            StringBuffer sql) {
        sql.append("asWKB(");
        sql.append("envelope(");
        encodeColumnName(geometryColumn, sql);
        sql.append("))");
    }
    
    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
            Connection cx) throws SQLException, IOException {
        byte[] wkb = rs.getBytes(column);

        try {
            //TODO: srid
            Polygon polygon = (Polygon) new WKBReader().read(wkb);

            return polygon.getEnvelopeInternal();
        } catch (ParseException e) {
            String msg = "Error decoding wkb for envelope";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }
    
    @Override
    public boolean isLimitOffsetSupported() {
        return delegate.isLimitOffsetSupported();
    }
    
    @Override
    public void applyLimitOffset(StringBuffer sql, int limit, int offset) {
        delegate.applyLimitOffset(sql, limit, offset);
    }

    @Override
    public FilterToSQL createFilterToSQL() {
        return new MySQLFilterToSQL();
    }
}
