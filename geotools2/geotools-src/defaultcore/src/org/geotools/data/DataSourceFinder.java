/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data;

import sun.misc.Service;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;


/*
 * DataSourceFinder.java
 * Provides methods which enable programs to find all available datasource implementations.
 * In order to be located by this finder datasources must provide an implementation of
 * the DataSourceFactorySpi interface. In addition to implementing this
 * interface datasouces should have a services file:
 * META-INF/services/org.geotools.data.DataSourceFactorySpi The file should
 * contain a single line which gives the full name of the implementing class.
 * e.g. org.geotools.data.mytype.MyTypeDataSourceFacotry
 */
public class DataSourceFinder {
    /** The logger for the data module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data");

    private DataSourceFinder() {
    }

    /**
     * Checks each available datasource implementation in turn and returns the
     * first one which claims to support the resource identified by the params
     * object.
     *
     * @param params A Map object which contains a defenition of the resource
     *        to connect to. for file based resources the property 'url'
     *        should be set within this Map.
     *
     * @return The first datasource which claims to process the required
     *         resource, returns null if none can be found.
     *
     * @throws DataSourceException If a sutiable loader can be found, but it
     *         can not be attached to the specified resource without errors.
     */
    public static DataSource getDataSource(Map params)
        throws DataSourceException {
        Iterator ps = Service.providers(DataSourceFactorySpi.class);
        LOGGER.finer("Available Data Sources:");

        while (ps.hasNext()) {
            DataSourceFactorySpi fac = (DataSourceFactorySpi) ps.next();
            LOGGER.finer(fac.getDescription());

            if (fac.canProcess(params)) {
                return fac.createDataSource(params);
            }
        }

        return null;
    }

    /**
     * Finds all implemtaions of DataSourceFactory which have registered using
     * the services mechanism.
     *
     * @return An iterator over all discovered datasources which have
     *         registered factories
     *
     * @task REVISIT: Currently uses sun.misc classes which may not always be
     *       available, we can probably work round this.
     */
    public static Iterator getAvailableDataSources() {
        return Service.providers(DataSourceFactorySpi.class);
    }
}
