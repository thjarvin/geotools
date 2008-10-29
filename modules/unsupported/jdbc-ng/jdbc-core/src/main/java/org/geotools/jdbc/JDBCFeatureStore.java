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
package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.FilteringFeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.ReTypeFeatureReader;
import org.geotools.data.jdbc.JDBCFeatureCollection;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.data.store.ContentState;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.FilterAttributeExtractor;
import org.geotools.filter.visitor.PostPreProcessFilterSplittingVisitor;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Association;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;


/**
 * FeatureStore implementation for jdbc based relational database tables.
 * <p>
 * All the operations of this class are delegated to {@link JDBCFeatureCollection}
 * via the {@link #all(ContentState)} and {@link #filtered(ContentState, Filter)}
 * methods.
 *
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 */
public final class JDBCFeatureStore extends ContentFeatureStore {
    
    /**
     * primary key of the table
     */
    PrimaryKey primaryKey;

    /**
     * Creates the new feature store.
     * @param entry The datastore entry.
     * @param query The defining query.
     */
    public JDBCFeatureStore(ContentEntry entry,Query query) throws IOException {
        super(entry,query);

        //TODO: cache this
        primaryKey = ((JDBCDataStore) entry.getDataStore()).getPrimaryKey(entry);
    }

    /**
     * Type narrow to {@link JDBCDataStore}.
     */
    public JDBCDataStore getDataStore() {
        return (JDBCDataStore) super.getDataStore();
    }

    /**
     * Type narrow to {@link JDBCState}.
     */
    public JDBCState getState() {
        return (JDBCState) super.getState();
    }

    /**
     * Returns the primary key of the table backed by feature store.
     */
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

//    /**
//     * This method operates by delegating to the
//     * {@link JDBCFeatureCollection#update(AttributeDescriptor[], Object[])}
//     * method provided by the feature collection resulting from
//     * {@link #filtered(ContentState, Filter)}.
//     *
//     * @see FeatureStore#modifyFeatures(AttributeDescriptor[], Object[], Filter)
//     */
//    public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
//        throws IOException {
//        if (filter == null) {
//            String msg = "Must specify a filter, must not be null.";
//            throw new IllegalArgumentException(msg);
//        }
//
//        JDBCFeatureCollection features = (JDBCFeatureCollection) filtered(getState(), filter);
//        features.update(type, value);
//    }

    /**
     * Builds the feature type from database metadata.
     */
    protected SimpleFeatureType buildFeatureType() throws IOException {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        AttributeTypeBuilder ab = new AttributeTypeBuilder();

        //set up the name
        String tableName = entry.getName().getLocalPart();
        tb.setName(tableName);

        //set the namespace, if not null
        if (entry.getName().getNamespaceURI() != null) {
            tb.setNamespaceURI(entry.getName().getNamespaceURI());
        } else {
            //use the data store
            tb.setNamespaceURI(getDataStore().getNamespaceURI());
        }

        //grab the schema
        String databaseSchema = getDataStore().getDatabaseSchema();

        //ensure we have a connection
        Connection cx = getDataStore().getConnection(getState());

        //get metadata about columns from database
        try {
            DatabaseMetaData metaData = cx.getMetaData();

            /*
             *        <LI><B>COLUMN_NAME</B> String => column name
             *        <LI><B>DATA_TYPE</B> int => SQL type from java.sql.Types
             *        <LI><B>TYPE_NAME</B> String => Data source dependent type name,
             *  for a UDT the type name is fully qualified
             *        <LI><B>COLUMN_SIZE</B> int => column size.  For char or date
             *            types this is the maximum number of characters, for numeric or
             *            decimal types this is precision.
             *        <LI><B>BUFFER_LENGTH</B> is not used.
             *        <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
             *        <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
             *        <LI><B>NULLABLE</B> int => is NULL allowed.
             *      <UL>
             *      <LI> columnNoNulls - might not allow <code>NULL</code> values
             *      <LI> columnNullable - definitely allows <code>NULL</code> values
             *      <LI> columnNullableUnknown - nullability unknown
             *      </UL>
             *         <LI><B>COLUMN_DEF</B> String => default value (may be <code>null</code>)
             *        <LI><B>IS_NULLABLE</B> String => "NO" means column definitely
             *      does not allow NULL values; "YES" means the column might
             *      allow NULL values.  An empty string means nobody knows.
             */
            ResultSet columns = metaData.getColumns(null, databaseSchema, tableName, "%");

            try {
                SQLDialect dialect = getDataStore().getSQLDialect();

                while (columns.next()) {
                    String name = columns.getString("COLUMN_NAME");

                    //do not include primary key in the type
                    /*
                     *        <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
                     *        <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
                     *        <LI><B>TABLE_NAME</B> String => table name
                     *        <LI><B>COLUMN_NAME</B> String => column name
                     *        <LI><B>KEY_SEQ</B> short => sequence number within primary key
                     *        <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
                     */
                    ResultSet primaryKeys = metaData.getPrimaryKeys(null, databaseSchema, tableName);

                    try {
                        while (primaryKeys.next()) {
                            String keyName = primaryKeys.getString("COLUMN_NAME");

                            if (name.equals(keyName)) {
                                name = null;

                                break;
                            }
                        }
                    } finally {
                        getDataStore().closeSafe(primaryKeys);
                    }

                    if (name == null) {
                        continue;
                    }

                    //check for association
                    if (getDataStore().isAssociations()) {
                        getDataStore().ensureAssociationTablesExist(cx);

                        //check for an association
                        String sql = getDataStore().selectRelationshipSQL(tableName, name);

                        Statement st = cx.createStatement();

                        try {
                            ResultSet relationships = st.executeQuery(sql);

                            try {
                                if (relationships.next()) {
                                    //found, create a special mapping 
                                    tb.add(name, Association.class);

                                    continue;
                                }
                            } finally {
                                getDataStore().closeSafe(relationships);
                            }
                        } finally {
                            getDataStore().closeSafe(st);
                        }
                    }

                    //figure out the type mapping

                    //first ask the dialect
                    Class binding = dialect.getMapping(columns, cx);

                    if (binding == null) {
                        //determine from type mappings
                        int dataType = columns.getInt("DATA_TYPE");
                        binding = getDataStore().getMapping(dataType);
                    }

                    if (binding == null) {
                        //determine from type name mappings
                        String typeName = columns.getString("TYPE_NAME");
                        binding = getDataStore().getMapping(typeName);
                    }

                    //if still not found, resort to Object
                    if (binding == null) {
                        getDataStore().getLogger().warning("Could not find mapping for:" + name);
                        binding = Object.class;
                    }

                    //nullability
                    if ( "NO".equalsIgnoreCase( columns.getString( "IS_NULLABLE" ) ) ) {
                        ab.nillable(false);
                        ab.minOccurs(1);
                    }
                    
                    //determine if this attribute is a geometry or not
                    if (Geometry.class.isAssignableFrom(binding)) {
                        //add the attribute as a geometry, try to figure out 
                        // its srid first
                        Integer srid = null;
                        CoordinateReferenceSystem crs = null;
                        try {
                            srid = dialect.getGeometrySRID(databaseSchema, tableName, name, cx);
                            if(srid != null)
                                crs = dialect.createCRS(srid, cx);
                        } catch (Exception e) {
                            String msg = "Error occured determing srid for " + tableName + "."
                                + name;
                            getDataStore().getLogger().log(Level.WARNING, msg, e);
                        }

                        ab.setBinding(binding);
                        ab.setName(name);
                        ab.setCRS(crs);
                        if(srid != null)
                            ab.addUserData(JDBCDataStore.JDBC_NATIVE_SRID, srid);
                        tb.add(ab.buildDescriptor(name, ab.buildGeometryType()));
                    } else {
                        //add the attribute
                        ab.setName(name);
                        ab.setBinding(binding);
                        tb.add(ab.buildDescriptor(name, ab.buildType()));
                    }
                }

                return tb.buildFeatureType();
            } finally {
                getDataStore().closeSafe(columns);
            }
        } catch (SQLException e) {
            String msg = "Error occurred building feature type";
            throw (IOException) new IOException().initCause(e);
        }
        finally {
            getDataStore().releaseConnection( cx, getState() );
        }
    }

    /**
     * Helper method for splitting a filter.
     */
    Filter[] splitFilter(Filter original) {
        Filter[] split = new Filter[2];
        if ( original != null ) {
            //create a filter splitter
            PostPreProcessFilterSplittingVisitor splitter = new PostPreProcessFilterSplittingVisitor(getDataStore()
                    .getFilterCapabilities(), getSchema(), null);
            original.accept(splitter, null);
        
            split[0] = splitter.getFilterPre();
            split[1] = splitter.getFilterPost();
        }
        
        SimplifyingFilterVisitor visitor = new SimplifyingFilterVisitor();
        split[0] = (Filter) split[0].accept(visitor, null);
        split[1] = (Filter) split[1].accept(visitor, null);
        
        return split;
    }

    protected int getCountInternal(Query query) throws IOException {
        JDBCDataStore dataStore = getDataStore();

        //split the filter
        Filter[] split = splitFilter( query.getFilter() );
        Filter preFilter = split[0];
        Filter postFilter = split[1];
        
        
            if ((postFilter != null) && (postFilter != Filter.INCLUDE)) {
                try {
                    //calculate manually, dont use datastore optimization
                    getDataStore().getLogger().fine("Calculating size manually");
    
                    int count = 0;
    
                    //grab a reader
                     FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader( query );
                    try {
                        while (reader.hasNext()) {
                            reader.next();
                            count++;
                        }
                    } finally {
                        reader.close();
                    }
    
                    return count;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                //no post filter, we have a preFilter, or preFilter is null.. 
                // either way we can use the datastore optimization
                Connection cx = dataStore.getConnection(getState());
                try {
                    int count = dataStore.getCount(getSchema(), preFilter, cx);
                    if(query.getMaxFeatures() > 0 && count > query.getMaxFeatures())
                        return query.getMaxFeatures();
                    else
                        return count;
                }
                finally {
                    dataStore.releaseConnection(cx, getState());
                }
            } 
        
    }
    
    protected ReferencedEnvelope getBoundsInternal(Query query)
            throws IOException {
        JDBCDataStore dataStore = getDataStore();

        //split the filter
        Filter[] split = splitFilter( query.getFilter() );
        Filter preFilter = split[0];
        Filter postFilter = split[1];
        
        try {
            if ((postFilter != null) && (postFilter != Filter.INCLUDE)) {
                //calculate manually, don't use datastore optimization
                getDataStore().getLogger().fine("Calculating bounds manually");

                // grab the 2d part of the crs 
                CoordinateReferenceSystem flatCRS = CRS.getHorizontalCRS(getSchema().getCoordinateReferenceSystem());
                ReferencedEnvelope bounds = new ReferencedEnvelope(flatCRS);

                // grab a reader
                FeatureReader<SimpleFeatureType, SimpleFeature> i = getReader(postFilter);
                try {
                    if (i.hasNext()) {
                        SimpleFeature f = (SimpleFeature) i.next();
                        bounds.init(f.getBounds());

                        while (i.hasNext()) {
                            f = i.next();
                            bounds.include(f.getBounds());
                        }
                    }
                } finally {
                    i.close();
                }

                return bounds;
            } 
            else {
                //post filter was null... pre can be set or null... either way
                // use datastore optimization
                Connection cx = dataStore.getConnection(getState());
                try {
                    return dataStore.getBounds(getSchema(), preFilter, cx);
                }
                finally {
                    getDataStore().releaseConnection( cx, getState() );
                }
            } 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected boolean canFilter() {
        return true;
    }
    
    protected boolean canSort() {
        return true;
    }
    
    protected boolean canRetype() {
        return true;
    }
    
    protected  FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
        // split the filter
        Filter[] split = splitFilter(query.getFilter());
        Filter preFilter = split[0];
        Filter postFilter = split[1];
        
        // Build the feature type returned by this query. Also build an eventual extra feature type
        // containing the attributes we might need in order to evaluate the post filter
        SimpleFeatureType querySchema;
        SimpleFeatureType returnedSchema;
        if(query.getPropertyNames() == Query.ALL_NAMES) {
            returnedSchema = querySchema = getSchema();
        } else {
            returnedSchema = SimpleFeatureTypeBuilder.retype(getSchema(), query.getPropertyNames());
            FilterAttributeExtractor extractor = new FilterAttributeExtractor(getSchema());
            postFilter.accept(extractor, null);
            String[] extraAttributes = extractor.getAttributeNames();
            if(extraAttributes == null || extraAttributes.length == 0) {
                querySchema = returnedSchema;
            } else {
                List<String> allAttributes = new ArrayList<String>(Arrays.asList(query.getPropertyNames())); 
                for (String extraAttribute : extraAttributes) {
                    if(!allAttributes.contains(extraAttribute))
                        allAttributes.add(extraAttribute);
                }
                String[] allAttributeArray =  (String[]) allAttributes.toArray(new String[allAttributes.size()]);
                querySchema = SimpleFeatureTypeBuilder.retype(getSchema(), allAttributeArray);
            }
        }
        
        //grab connection
        Connection cx = getDataStore().getConnection(getState());
        
        //create the reader
        FeatureReader<SimpleFeatureType, SimpleFeature> reader;
        
        SQLDialect dialect = getDataStore().getSQLDialect();
        if ( dialect.isUsingPreparedStatements() ) {
            try {
                PreparedStatement ps = 
                    getDataStore().selectSQLPS(querySchema, preFilter, query.getSortBy(), cx);
                reader = new JDBCFeatureReader( ps, cx, this, querySchema, query.getHints() );
            } 
            catch (SQLException e) {
                throw (IOException) new IOException().initCause(e);
            }
        }
        else {
            //build up a statement for the content
            String sql = getDataStore().selectSQL(querySchema, preFilter, query.getSortBy());
            getDataStore().getLogger().fine(sql);

            try {
                reader = new JDBCFeatureReader( sql, cx, this, querySchema, query.getHints() );
            } catch (SQLException e) {
                throw (IOException) new IOException("error create reader").initCause( e );
            }
        }
        

        // if post filter, wrap it
        if (postFilter != null && postFilter != Filter.INCLUDE) {
            reader = new FilteringFeatureReader<SimpleFeatureType, SimpleFeature>(reader,postFilter);
            if(!returnedSchema.equals(querySchema))
                reader = new ReTypeFeatureReader(reader, returnedSchema);
        }

        return reader;
    }
    
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> getWriterInternal(Query query, int flags)
            throws IOException {
        
        if ( flags == 0 ) {
            throw new IllegalArgumentException( "no write flags set" );
        }
        
        //get connection from current state
        Connection cx = getDataStore().getConnection(getState());
        
        Filter postFilter;
        //check for update only case
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
        try {
            //check for insert only
            if ( (flags | WRITER_ADD) == WRITER_ADD ) {
                if ( getDataStore().getSQLDialect().isUsingPreparedStatements() ) {
                    PreparedStatement ps = getDataStore().selectSQLPS(getSchema(), Filter.EXCLUDE, query.getSortBy(), cx);
                    return new JDBCInsertFeatureWriter( ps, cx, this, query.getHints() );
                }
                else {
                    //build up a statement for the content, inserting only so we dont want
                    // the query to return any data ==> Filter.EXCLUDE
                    String sql = getDataStore().selectSQL(getSchema(), Filter.EXCLUDE, query.getSortBy());
                    getDataStore().getLogger().fine(sql);
    
                    return new JDBCInsertFeatureWriter( sql, cx, this, query.getHints() );
                }
            }
            
            //split the filter
            Filter[] split = splitFilter(query.getFilter());
            Filter preFilter = split[0];
            postFilter = split[1];
            
            // build up a statement for the content
            if(getDataStore().getSQLDialect().isUsingPreparedStatements()) {
                PreparedStatement ps = getDataStore().selectSQLPS(getSchema(), preFilter, query.getSortBy(), cx);
                if ( (flags | WRITER_UPDATE) == WRITER_UPDATE ) {
                    writer = new JDBCUpdateFeatureWriter(ps, cx, this, query.getHints() );
                } else {
                    //update insert case
                    writer = new JDBCUpdateInsertFeatureWriter(ps, cx, this, query.getPropertyNames(), query.getHints() );
                }
            } else {
                String sql = getDataStore().selectSQL(getSchema(), preFilter, query.getSortBy());
                getDataStore().getLogger().fine(sql);
                
                if ( (flags | WRITER_UPDATE) == WRITER_UPDATE ) {
                    writer = new JDBCUpdateFeatureWriter( sql, cx, this, query.getHints() );
                } else {
                    //update insert case
                    writer = new JDBCUpdateInsertFeatureWriter( sql, cx, this, query.getHints() );
                }
            }
            
        } 
        catch (SQLException e) {
            throw (IOException) new IOException( ).initCause(e);
        }
        
        //check for post filter and wrap accordingly
        if ( postFilter != null && postFilter != Filter.INCLUDE ) {
            writer = new FilteringFeatureWriter( writer, postFilter );
        }
        return writer;
    }
}