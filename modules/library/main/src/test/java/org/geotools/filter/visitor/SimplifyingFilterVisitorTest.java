/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter.visitor;

import java.util.HashSet;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.identity.Identifier;

public class SimplifyingFilterVisitorTest extends TestCase {
    
    FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
    Id emptyFid;
    SimplifyingFilterVisitor visitor;
    PropertyIsEqualTo property;
    
    @Override
    protected void setUp() throws Exception {
        emptyFid = ff.id(new HashSet<Identifier>());
        property = ff.equal(ff.property("test"), ff.literal("oneTwoThree"), false);        
        visitor = new SimplifyingFilterVisitor();
    }

    public void testIncludeAndInclude() {
        Filter result = (Filter) ff.and(Filter.INCLUDE, Filter.INCLUDE).accept(visitor, null);
        assertEquals(Filter.INCLUDE, result);
    }
    
    public void testIncludeAndExclude() {
        Filter result = (Filter) ff.and(Filter.INCLUDE, Filter.EXCLUDE).accept(visitor, null);
        assertEquals(Filter.EXCLUDE, result);
    }
    
    public void testExcludeAndExclude() {
        Filter result = (Filter) ff.and(Filter.EXCLUDE, Filter.EXCLUDE).accept(visitor, null);
        assertEquals(Filter.EXCLUDE, result);
    }
    
    public void testIncludeAndProperty() {
        Filter result = (Filter) ff.and(Filter.INCLUDE, property).accept(visitor, null);
        assertEquals(property, result);
    }
    
    public void testExcludeAndProperty() {
        Filter result = (Filter) ff.or(Filter.EXCLUDE, property).accept(visitor, null);
        assertEquals(property, result);
    }
    
    public void testIncludeOrInclude() {
        Filter result = (Filter) ff.or(Filter.INCLUDE, Filter.INCLUDE).accept(visitor, null);
        assertEquals(Filter.INCLUDE, result);
    }
    
    public void testIncludeOrExclude() {
        Filter result = (Filter) ff.or(Filter.INCLUDE, Filter.EXCLUDE).accept(visitor, null);
        assertEquals(Filter.INCLUDE, result);
    }
    
    public void testExcludeOrExclude() {
        Filter result = (Filter) ff.or(Filter.EXCLUDE, Filter.EXCLUDE).accept(visitor, null);
        assertEquals(Filter.EXCLUDE, result);
    }
    
    public void testIncludeOrProperty() {
        Filter result = (Filter) ff.or(Filter.INCLUDE, property).accept(visitor, null);
        assertEquals(Filter.INCLUDE, result);
    }
    
    public void testExcludeOrProperty() {
        Filter result = (Filter) ff.or(Filter.EXCLUDE, property).accept(visitor, null);
        assertEquals(property, result);
    }
    
    public void testEmptyFid() {
        Filter result = (Filter) emptyFid.accept(visitor, null);
        assertEquals(Filter.EXCLUDE, result);    
    }
    
    public void testRecurseAnd() {
        Filter test = ff.and(Filter.INCLUDE, ff.or(property, Filter.EXCLUDE));
        assertEquals(property, test.accept(visitor, null));
    }
    
    public void testRecurseOr() {
        Filter test = ff.or(Filter.EXCLUDE, ff.and(property, Filter.INCLUDE));
        assertEquals(property, test.accept(visitor, null));
    }
}