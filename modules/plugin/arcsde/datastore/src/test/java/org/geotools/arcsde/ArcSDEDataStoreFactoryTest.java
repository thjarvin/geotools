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
 *
 */
package org.geotools.arcsde;

import static org.geotools.arcsde.pool.ArcSDEConnectionConfig.VERSION_PARAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.arcsde.data.ArcSDEDataStore;
import org.geotools.arcsde.data.InProcessViewSupportTestData;
import org.geotools.arcsde.data.TestData;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ISession;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeVersion;

/**
 * Test suite for {@link ArcSDEDataStoreFactory}
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java
 *         /org/geotools/arcsde/ArcSDEDataStoreFactoryTest.java $
 * @version $Id$
 * @since 2.4.x
 */
@SuppressWarnings("unchecked")
public class ArcSDEDataStoreFactoryTest {

    /**
     * A datastore factory set up with the {@link #workingParams}
     */
    private ArcSDEDataStoreFactory dsFactory;

    /**
     * A set of datastore parameters that are meant to work
     */
    private Map workingParams;

    /**
     * Aset of datastore parameters that though valid (contains all the required parameters) point
     * to a non available server
     */
    private Map nonWorkingParams;

    /**
     * A set of datastore parameters that does not conform to the parameters required by the ArcSDE
     * plugin
     */
    private Map illegalParams;

    private TestData testData;

    @Before
    public void setUp() throws Exception {
        this.testData = new TestData();
        testData.setUp();

        workingParams = testData.getConProps();

        nonWorkingParams = new HashMap(workingParams);
        nonWorkingParams.put(ArcSDEConnectionConfig.SERVER_NAME_PARAM, "non-existent-server");

        illegalParams = new HashMap(workingParams);
        illegalParams.put(ArcSDEConnectionConfig.DBTYPE_PARAM, "non-existent-db-type");

        dsFactory = new ArcSDEDataStoreFactory();
    }

    @After
    public void tearDown() throws Exception {
        this.testData.tearDown(true, true);
    }

    @Test
    public void testGetDataStore() throws IOException {
        DataStore dataStore;

        try {
            DataStoreFinder.getDataStore(nonWorkingParams);
            fail("should have failed with non working parameters");
        } catch (DataSourceException e) {
            assertTrue(true);
        }
        dataStore = DataStoreFinder.getDataStore(workingParams);
        assertNotNull(dataStore);
        assertTrue(dataStore instanceof ArcSDEDataStore);
        dataStore.dispose();
    }

    @Test
    public void testDataStoreFinderFindsIt() throws IOException {
        Iterator<DataStoreFactorySpi> allFactories = DataStoreFinder.getAllDataStores();
        ArcSDEDataStoreFactory sdeFac = null;
        while (allFactories.hasNext()) {
            DataAccessFactory next = allFactories.next();
            if (next instanceof ArcSDEDataStoreFactory) {
                sdeFac = (ArcSDEDataStoreFactory) next;
                break;
            }
        }
        assertNotNull(sdeFac);
    }

    @Test
    public void testDataAccessFinderFindsIt() throws IOException {
        Iterator<DataAccessFactory> allFactories = DataAccessFinder.getAllDataStores();
        ArcSDEDataStoreFactory sdeFac = null;
        while (allFactories.hasNext()) {
            DataAccessFactory next = allFactories.next();
            if (next instanceof ArcSDEDataStoreFactory) {
                sdeFac = (ArcSDEDataStoreFactory) next;
                break;
            }
        }
        assertNotNull(sdeFac);
    }

    /**
     * Test method for
     * {@link org.geotools.arcsde.ArcSDEDataStoreFactory#createNewDataStore(java.util.Map)}.
     */
    @Test
    public void testCreateNewDataStore() {
        try {
            dsFactory.createNewDataStore(Collections.EMPTY_MAP);
            fail("Expected UOE as we can't create new datastores");
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link org.geotools.arcsde.ArcSDEDataStoreFactory#canProcess(java.util.Map)}.
     */
    @Test
    public void testCanProcess() {
        assertFalse(dsFactory.canProcess(illegalParams));
        assertTrue(dsFactory.canProcess(nonWorkingParams));
        assertTrue(dsFactory.canProcess(workingParams));
    }

    /**
     * Test method for
     * {@link org.geotools.arcsde.ArcSDEDataStoreFactory#createDataStore(java.util.Map)}.
     * 
     * @throws IOException
     */
    @Test
    public void testCreateDataStore() throws IOException {
        try {
            dsFactory.createDataStore(nonWorkingParams);
        } catch (IOException e) {
            assertTrue(true);
        }

        DataStore store = dsFactory.createDataStore(workingParams);
        assertNotNull(store);
        assertTrue(store instanceof ArcSDEDataStore);
        store.dispose();
    }

    /**
     * Test method for
     * {@link org.geotools.arcsde.ArcSDEDataStoreFactory#createDataStore(java.util.Map)}.
     * 
     * @throws IOException
     * @throws SeException
     */
    @Test
    public void testCreateDataStoreWithInProcessViews() throws IOException, SeException {
        ISession session = testData.getConnectionPool().getSession();
        try {
            InProcessViewSupportTestData.setUp(session, testData);
        } finally {
            session.dispose();
        }

        Map workingParamsWithSqlView = new HashMap(workingParams);
        workingParamsWithSqlView.putAll(InProcessViewSupportTestData.registerViewParams);

        DataStore store = dsFactory.createDataStore(workingParamsWithSqlView);
        assertNotNull(store);

        SimpleFeatureType viewType = store.getSchema(InProcessViewSupportTestData.typeName);
        assertNotNull(viewType);
        assertEquals(InProcessViewSupportTestData.typeName, viewType.getTypeName());
        store.dispose();
    }

    @Test
    public void testVersionParamCheck() throws IOException {
        ISession session = testData.getConnectionPool().getSession();
        final String versionName = "testVersionParamCheck";
        try {
            testData.createVersion(session, versionName,
                    SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME);
        } finally {
            session.dispose();
        }

        Map paramsWithVersion = new HashMap(workingParams);
        try {
            paramsWithVersion.put(VERSION_PARAM, "Non existent version name");
            dsFactory.createDataStore(paramsWithVersion);
        } catch (IOException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith(
                    "Specified ArcSDE version does not exist"));
        }
    }

    @Test
    public void testVersioned() throws IOException {
        ISession session = testData.getConnectionPool().getSession();
        final String versionName = "testVersioned";
        try {
            testData.createVersion(session, versionName,
                    SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME);
        } finally {
            session.dispose();
        }

        Map paramsWithVersion = new HashMap(workingParams);
        paramsWithVersion.put(VERSION_PARAM, versionName);
        DataStore ds = dsFactory.createDataStore(paramsWithVersion);
        assertNotNull(ds);
        ds.dispose();

        String qualifiedVersionName = session.getUser() + "." + versionName;
        paramsWithVersion.put(VERSION_PARAM, qualifiedVersionName);
        ds = dsFactory.createDataStore(paramsWithVersion);
        assertNotNull(ds);
        ds.dispose();

        // version name should be case insensitive
        qualifiedVersionName = qualifiedVersionName.toUpperCase();
        paramsWithVersion.put(VERSION_PARAM, qualifiedVersionName);
        ds = dsFactory.createDataStore(paramsWithVersion);
        assertNotNull(ds);
        ds.dispose();

        qualifiedVersionName = qualifiedVersionName.toLowerCase();
        paramsWithVersion.put(VERSION_PARAM, qualifiedVersionName);
        ds = dsFactory.createDataStore(paramsWithVersion);
        assertNotNull(ds);
        ds.dispose();
    }
}
