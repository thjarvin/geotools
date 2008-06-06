/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gml.producer;

import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


//import org.geotools.feature.*;

/*
 * Utilities for gml and xml;
 * @author Chris Holmes, TOPP
 * @source $URL$
 */
final class GMLUtils {
    /** The logger for the filter module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
    "org.geotools.gml.producer");
    
    /** Internal representation of URL used to represent GML */
    public static final String GML_URL = "http://www.opengis.net/gml";
    
    /** Internal representation of OGC SF Point */
    protected static final int POINT = 1;
    
    /** Internal representation of OGC SF LineString */
    protected static final int LINESTRING = 2;
    
    /** Internal representation of OGC SF Polygon */
    protected static final int POLYGON = 3;
    
    /** Internal representation of OGC SF MultiPoint */
    protected static final int MULTIPOINT = 4;
    
    /** Internal representation of OGC SF MultiLineString */
    protected static final int MULTILINESTRING = 5;
    
    /** Internal representation of OGC SF MultiPolygon */
    protected static final int MULTIPOLYGON = 6;
    
    /** Internal representation of OGC SF MultiGeometry */
    protected static final int MULTIGEOMETRY = 7;
    
    /**
     * private constructor so default is not used.
     */
    private GMLUtils() {
    }
    
    /**
     * Gets the String representation for the given Geometry
     *
     * @param geometry a Geometry
     *
     * @return String representation of geometry
     */
    public static String getGeometryName(Geometry geometry) {
        LOGGER.entering("GMLUtils", "getGeometryName", geometry);
        
        Class geomClass = geometry.getClass();
        String returnValue = null;
        
        if (geomClass.equals(Point.class)) {
            returnValue = "Point";
        } else if (geomClass.equals(LineString.class)) {
            returnValue = "LineString";
        } else if (geomClass.equals(Polygon.class)) {
            returnValue = "Polygon";
        } else if (geomClass.equals(MultiPoint.class)) {
            returnValue = "MultiPoint";
        } else if (geomClass.equals(MultiLineString.class)) {
            returnValue = "MultiLineString";
        } else if (geomClass.equals(MultiPolygon.class)) {
            returnValue = "MultiPolygon";
        } else if (geomClass.equals(GeometryCollection.class)) {
            returnValue = "GeometryCollection";
        } else {
            //HACK!!! throw exception
            returnValue = null;
        }
        
        LOGGER.exiting("GMLUtils", "getGeometryName", returnValue);
        
        return returnValue;
    }
    
    /**
     * Gets the internal representation for the given Geometry
     *
     * @param geometry a Geometry
     *
     * @return int representation of Geometry
     */
    public static int getGeometryType(Geometry geometry) {
        //LOGGER.entering("GMLUtils", "getGeometryType", geometry);
        
        Class geomClass = geometry.getClass();
        int returnValue = -1;
        
        if (geomClass.equals(Point.class)) {
            //LOGGER.finest("found point");
            returnValue = POINT;
        } else if (geomClass.equals(LineString.class)) {
            //LOGGER.finest("found linestring");
            returnValue = LINESTRING;
        } else if (geomClass.equals(Polygon.class)) {
            //LOGGER.finest("found polygon");
            returnValue = POLYGON;
        } else if (geomClass.equals(MultiPoint.class)) {
            //LOGGER.finest("found multiPoint");
            returnValue = MULTIPOINT;
        } else if (geomClass.equals(MultiLineString.class)) {
            returnValue = MULTILINESTRING;
        } else if (geomClass.equals(MultiPolygon.class)) {
            returnValue = MULTIPOLYGON;
        } else if (geomClass.equals(GeometryCollection.class)) {
            returnValue = MULTIGEOMETRY;
        } else {
            returnValue = -1;
            
            //HACK!!! throw exception.
        }
        
        //LOGGER.exiting("GMLUtils", "getGeometryType", new Integer(returnValue));
        
        return returnValue;
    }

    public static String getMemberName(int geometryType) {
        //String member;
        
        switch (geometryType) {
            case GMLUtils.MULTIPOINT:
                return "pointMember";
                
            case GMLUtils.MULTILINESTRING:
                return "lineStringMember";
                
            case GMLUtils.MULTIPOLYGON:
                return "polygonMember";
                
            default:
                return "geometryMember";
        }
    }
    
    /**
     * Parses the passed string, and encodes the special characters (used in
     * xml for special purposes) with the appropriate codes. e.g. right
     * bracket char is changed to '&lt;'
     *
     * @param inData the string to encode.
     *
     * @return the encoded string. Returns null, if null is passed as argument
     *
     * @task TODO: Take output as a param, write directly to out, send the
     *       characters straight out, doing translation on the fly.
     */
    public static String encodeXML(String inData) {
        //LOGGER.entering("GMLUtils", "encodeXML", inData);
        
        //return null, if null is passed as argument
        if (inData == null) {
            return null;
        }
        
        //if no special characters, just return
        //(for optimization. Though may be an overhead, but for most of the
        //strings, this will save time)
        if ((inData.indexOf('&') == -1) && (inData.indexOf('<') == -1)
        && (inData.indexOf('>') == -1) && (inData.indexOf('\'') == -1)
        && (inData.indexOf('\"') == -1)) {
            return inData;
        }
        
        //get the length of input String
        int length = inData.length();
        
        //create a StringBuffer of double the size (size is just for guidance
        //so as to reduce increase-capacity operations. The actual size of
        //the resulting string may be even greater than we specified, but is
        //extremely rare)
        StringBuffer buffer = new StringBuffer(2 * length);
        
        char charToCompare;
        
        //iterate over the input String
        for (int i = 0; i < length; i++) {
            charToCompare = inData.charAt(i);
            
            //if the ith character is special character, replace by code
            if (charToCompare == '&') {
                buffer.append("&amp;");
            } else if (charToCompare == '<') {
                buffer.append("&lt;");
            } else if (charToCompare == '>') {
                buffer.append("&gt;");
            } else if (charToCompare == '\"') {
                buffer.append("&quot;");
            } else if (charToCompare == '\'') {
                buffer.append("&apos;");
            } else {
                buffer.append(charToCompare);
            }
        }
        
        //LOGGER.exiting("GMLUtils", "encodeXML", buffer.toString());
        
        //return the encoded string
        return buffer.toString();
    }
}
