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
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public abstract class JDBCDataStoreTest extends JDBCTestSupport {
    public void testGetNames() throws IOException {
        String[] typeNames = dataStore.getTypeNames();
        assertTrue(new HashSet(Arrays.asList(typeNames)).contains(tname("ft1")));
    }

    public void testGetSchema() throws Exception {
        SimpleFeatureType ft1 = dataStore.getSchema(tname("ft1"));
        assertNotNull(ft1);

        assertNotNull(ft1.getDescriptor(aname("geometry")));
        assertNotNull(ft1.getDescriptor(aname("intProperty")));
        assertNotNull(ft1.getDescriptor(aname("doubleProperty")));
        assertNotNull(ft1.getDescriptor(aname("stringProperty")));

        assertTrue(Geometry.class.isAssignableFrom(ft1.getDescriptor(aname("geometry")).getType()
                                                      .getBinding()));
        assertTrue(Number.class.isAssignableFrom( ft1.getDescriptor(aname("intProperty")).getType().getBinding()) );
        assertEquals(Double.class, ft1.getDescriptor(aname("doubleProperty")).getType().getBinding());
        assertEquals(String.class, ft1.getDescriptor(aname("stringProperty")).getType().getBinding());
    }

    public void testCreateSchema() throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(tname("ft2"));
        builder.setNamespaceURI(dataStore.getNamespaceURI());
        builder.add(aname("geometry"), Geometry.class);
        builder.add(aname("intProperty"), Integer.class);
        builder.add(aname("dateProperty"), Date.class);

        SimpleFeatureType featureType = builder.buildFeatureType();
        dataStore.createSchema(featureType);

        SimpleFeatureType ft2 = dataStore.getSchema(tname("ft2"));
        assertEquals(ft2, featureType);
        // GEOT-2031
        assertNotSame(ft2, featureType);

        Connection cx = dataStore.createConnection();
        Statement st = cx.createStatement();
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT * FROM ");

            if (dataStore.getDatabaseSchema() != null) {
                dataStore.getSQLDialect().encodeSchemaName(dataStore.getDatabaseSchema(), sql);
                sql.append(".");
            }

            dataStore.getSQLDialect().encodeTableName("ft2", sql);
            rs = st.executeQuery(sql.toString());
        } catch (SQLException e) {
            throw e;
        } finally {
            if(rs != null)
                rs.close();
            st.close();
        }
    }

    public void testGetFeatureSource() throws Exception {
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(tname("ft1"));
        assertNotNull(featureSource);
    }

    public void testGetFeatureReader() throws Exception {
        GeometryFactory gf = dataStore.getGeometryFactory();

        DefaultQuery query = new DefaultQuery(tname("ft1"));
         FeatureReader<SimpleFeatureType, SimpleFeature> reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);

        for (int i = 0; i < 3; i++) {
            assertTrue(reader.hasNext());

            SimpleFeature feature = reader.next();
            assertNotNull(feature);
            assertEquals(4, feature.getAttributeCount());

            Point p = gf.createPoint(new Coordinate(i, i));
            assertTrue(p.equals((Geometry) feature.getAttribute(aname("geometry"))));

            Number ip = (Number) feature.getAttribute(aname("intProperty"));
            assertEquals(i, ip.intValue());
        }

        assertFalse(reader.hasNext());
        reader.close();

        query.setPropertyNames(new String[] { aname("intProperty") });
        reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);

        for (int i = 0; i < 3; i++) {
            assertTrue(reader.hasNext());

            SimpleFeature feature = reader.next();
            assertEquals(1, feature.getAttributeCount());
        }

        assertFalse(reader.hasNext());
        reader.close();

        FilterFactory ff = dataStore.getFilterFactory();
        Filter f = ff.equals(ff.property(aname("intProperty")), ff.literal(1));
        query.setFilter(f);

        reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);

        for (int i = 0; i < 1; i++) {
            assertTrue(reader.hasNext());

            SimpleFeature feature = reader.next();
        }

        assertFalse(reader.hasNext());
        reader.close();
    }

    public void testGetFeatureWriter() throws IOException {
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore.getFeatureWriter(tname("ft1"), Transaction.AUTO_COMMIT);

        while (writer.hasNext()) {
            SimpleFeature feature = writer.next();
            feature.setAttribute(aname("stringProperty"), "foo");
            writer.write();
        }

        writer.close();

        DefaultQuery query = new DefaultQuery(tname("ft1"));
         FeatureReader<SimpleFeatureType, SimpleFeature> reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
        assertTrue(reader.hasNext());

        while (reader.hasNext()) {
            SimpleFeature feature = reader.next();
            assertEquals("foo", feature.getAttribute(aname("stringProperty")));
        }

        reader.close();
    }

    public void testGetFeatureWriterWithFilter() throws IOException {
        FilterFactory ff = dataStore.getFilterFactory();

        Filter f = ff.equals(ff.property(aname("intProperty")), ff.literal(100));
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = dataStore.getFeatureSource(tname("ft1")).getFeatures(f);
        assertEquals(0, features.size());

        f = ff.equals(ff.property(aname("intProperty")), ff.literal(1));

        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore.getFeatureWriter(tname("ft1"), f, Transaction.AUTO_COMMIT);

        while (writer.hasNext()) {
            SimpleFeature feature = writer.next();
            feature.setAttribute(aname("intProperty"), new Integer(100));
            writer.write();
        }

        writer.close();

        f = ff.equals(ff.property(aname("intProperty")), ff.literal(100));
        features = dataStore.getFeatureSource(tname("ft1")).getFeatures(f);
        assertEquals(1, features.size());
    }

    public void testGetFeatureWriterAppend() throws IOException {
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore.getFeatureWriterAppend(tname("ft1"), Transaction.AUTO_COMMIT);

        for (int i = 3; i < 6; i++) {
            SimpleFeature feature = writer.next();
            feature.setAttribute(aname("intProperty"), new Integer(i));
            writer.write();
        }

        writer.close();

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = dataStore.getFeatureSource(tname("ft1")).getFeatures();
        assertEquals(6, features.size());
    }
}