package org.geotools.filter.function.math;

/*
 *    GeoTools - The Open Source Java GIS Tookit
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

//this code is autogenerated - you shouldnt be modifying it!
import org.geotools.filter.FunctionExpressionImpl;

public class FilterFunction_max_2 extends FunctionExpressionImpl {

    public FilterFunction_max_2() {
        super("max_2");
    }

    public int getArgCount() {
        return 2;
    }

    public Object evaluate(Object feature) {
        long arg0;
        long arg1;

        try { // attempt to get value and perform conversion
            arg0 = ((Number) getExpression(0).evaluate(feature)).longValue();
        } catch (Exception e) {
            // probably a type error
            throw new IllegalArgumentException(
                    "Filter Function problem for function max argument #0 - expected type long");
        }

        try { // attempt to get value and perform conversion
            arg1 = ((Number) getExpression(1).evaluate(feature)).longValue();
        } catch (Exception e) {
            // probably a type error
            throw new IllegalArgumentException(
                    "Filter Function problem for function max argument #1 - expected type long");
        }

        return new Long(Math.max(arg0, arg1));
    }
}
