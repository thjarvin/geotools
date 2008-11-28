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
package org.geotools.data.wfs.v1_1_0;

import static org.junit.Assert.*;
import java.util.Arrays;

import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;
import org.geotools.filter.Capabilities;
import org.geotools.filter.function.FilterFunction_geometryType;
import org.geotools.filter.function.math.FilterFunction_abs;
import org.junit.Test;

/**
 * Test case where only specific functions are supported.
 * 
 * @author Jesse
 *
 */
@SuppressWarnings({"nls", "unchecked"})
public class PostPreProcessFilterSplitterVisitorFunctionTest extends AbstractPostPreProcessFilterSplittingVisitorTests {
    
    PostPreProcessFilterSplittingVisitor visitor;

    @Test
	public void testSupportAll() throws Exception {
        
		PropertyIsEqualTo filter1 = createFunctionFilter();
		FilterFunction_abs filterFunction_abs = new FilterFunction_abs();
		filterFunction_abs.setParameters(Arrays.asList(new Expression[]{ff.property("name")}));
        PropertyIsEqualTo filter2 = ff.equals(ff.property("name"), filterFunction_abs);
		
		Filter filter=ff.and(filter1,filter2);

        visitor = newVisitor(new Capabilities());
        filter.accept(visitor, null);
		
		assertEquals(Filter.INCLUDE, visitor.getFilterPre());
		assertEquals(filter, visitor.getFilterPost());
        
        Capabilities filterCapabilitiesMask = new Capabilities();

		filterCapabilitiesMask.addName(testFunction.getName());
		filterFunction_abs = new FilterFunction_abs();
		filterCapabilitiesMask.addName(filterFunction_abs.getName());
		filterCapabilitiesMask.addAll(Capabilities.SIMPLE_COMPARISONS_OPENGIS);
        filterCapabilitiesMask.addAll(Capabilities.LOGICAL_OPENGIS);
		visitor=newVisitor(filterCapabilitiesMask);

        filter.accept(visitor, null);
		
		assertEquals(Filter.INCLUDE, visitor.getFilterPost());
		assertEquals(filter, visitor.getFilterPre());
	}

    @Test
	public void testSupportOnlySome() throws Exception {

        PropertyIsEqualTo filter1 = createFunctionFilter();
        FilterFunction_abs filterFunction_abs = new FilterFunction_abs();
        filterFunction_abs.setParameters(Arrays.asList(new Expression[]{ff.property("name")}));
        PropertyIsEqualTo filter2 = ff.equals(ff.property("name"), filterFunction_abs);
        
        Filter filter=ff.and(filter1,filter2);

        Capabilities filterCapabilitiesMask = new Capabilities();
		filterCapabilitiesMask.addName(testFunction.getName(), testFunction.getParameters().size());
		filterCapabilitiesMask.addAll(Capabilities.SIMPLE_COMPARISONS_OPENGIS);
        filterCapabilitiesMask.addAll(Capabilities.LOGICAL_OPENGIS);
		visitor=newVisitor(filterCapabilitiesMask);

        filter.accept(visitor, null);
		
		assertEquals(filter1, visitor.getFilterPre());
		assertEquals(filter2, visitor.getFilterPost());
		
	}
}