/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.filter;

import java.util.*;
import org.apache.log4j.Level;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import junit.framework.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.feature.*;


/**
 * Unit test for filters.  Note that this unit test does not encompass all of
 * filter package, just the filters themselves.  There is a seperate unit test
 * for expressions.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */                                
public class FilterTest extends TestCase {
    
    /** Standard logging instance */
    private static Logger _log;

    /** Feature on which to preform tests */
    private static Feature testFeature = null;

    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;
    boolean set = false;
    /** Test suite for this test case */
    TestSuite suite = null;


    /** 
     * Constructor with test name.
     */
    public FilterTest(String testName) {
        super(testName);
        //BasicConfigurator.configure();
        _log = Logger.getLogger(FilterTest.class);
        _log.getLoggerRepository().setThreshold(Level.INFO);
        
    }        
    
    /** 
     * Main for test runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** 
     * Required suite builder.
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite(FilterTest.class);
        return suite;
    }
    
    /** 
     * Sets up a schema and a test feature.
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() 
        throws SchemaException, IllegalFeatureException {
        if(set) return;
        set = true;
        // Create the schema attributes
        _log.debug("creating flat feature...");
        AttributeType geometryAttribute = 
            new AttributeTypeDefault("testGeometry", LineString.class);
        _log.debug("created geometry attribute");
        AttributeType booleanAttribute = 
            new AttributeTypeDefault("testBoolean", Boolean.class);
        _log.debug("created boolean attribute");
        AttributeType charAttribute = 
            new AttributeTypeDefault("testCharacter", Character.class);
        AttributeType byteAttribute = 
            new AttributeTypeDefault("testByte", Byte.class);
        AttributeType shortAttribute = 
            new AttributeTypeDefault("testShort", Short.class);
        AttributeType intAttribute = 
            new AttributeTypeDefault("testInteger", Integer.class);
        AttributeType longAttribute = 
            new AttributeTypeDefault("testLong", Long.class);
        AttributeType floatAttribute = 
            new AttributeTypeDefault("testFloat", Float.class);
        AttributeType doubleAttribute = 
            new AttributeTypeDefault("testDouble", Double.class);
        AttributeType stringAttribute = 
            new AttributeTypeDefault("testString", String.class);
        
        // Builds the schema
        testSchema = new FeatureTypeFlat(geometryAttribute); 
        _log.debug("created feature type and added geometry");
        testSchema = testSchema.setAttributeType(booleanAttribute);
        _log.debug("added boolean to feature type");
        testSchema = testSchema.setAttributeType(charAttribute);
        _log.debug("added character to feature type");
        testSchema = testSchema.setAttributeType(byteAttribute);
        _log.debug("added byte to feature type");
        testSchema = testSchema.setAttributeType(shortAttribute);
        _log.debug("added short to feature type");
        testSchema = testSchema.setAttributeType(intAttribute);
        _log.debug("added int to feature type");
        testSchema = testSchema.setAttributeType(longAttribute);
        _log.debug("added long to feature type");
        testSchema = testSchema.setAttributeType(floatAttribute);
        _log.debug("added float to feature type");
        testSchema = testSchema.setAttributeType(doubleAttribute);
        _log.debug("added double to feature type");
        testSchema = testSchema.setAttributeType(stringAttribute);
        _log.debug("added string to feature type");
        
        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1,2);
        coords[1] = new Coordinate(3,4);
        coords[2] = new Coordinate(5,6);
        
        // Builds the test feature
        Object[] attributes = new Object[10];
        attributes[0] = new LineString(coords, new PrecisionModel(), 1);
        attributes[1] = new Boolean(true);
        attributes[2] = new Character('t');
        attributes[3] = new Byte("10");
        attributes[4] = new Short("101");
        attributes[5] = new Integer(1002);
        attributes[6] = new Long(10003);
        attributes[7] = new Float(10000.4);
        attributes[8] = new Double(100000.5);
        attributes[9] = "test string data";
        
        // Creates the feature itself
        FeatureFactory factory = new FeatureFactory(testSchema);
        testFeature = factory.create(attributes);
        _log.debug("...flat feature created");
    }


    /** 
     * Sets up a schema and a test feature.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testCompare()
        throws IllegalFilterException {

        // Test all integer permutations
        Expression testAttribute = new ExpressionAttribute(testSchema, "testInteger");
        compareNumberRunner(testAttribute, AbstractFilter.COMPARE_EQUALS, false, true, false);
        compareNumberRunner(testAttribute, AbstractFilter.COMPARE_GREATER_THAN, true, false, false);
        compareNumberRunner(testAttribute, AbstractFilter.COMPARE_LESS_THAN, false, false, true);
        compareNumberRunner(testAttribute, AbstractFilter.COMPARE_GREATER_THAN_EQUAL, true, true, false);
        compareNumberRunner(testAttribute, AbstractFilter.COMPARE_LESS_THAN_EQUAL, false, true, true);

        // Set up the string test.
        testAttribute = new ExpressionAttribute(testSchema, "testString");
        CompareFilter filter = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression testLiteral;
        filter.addLeftValue(testAttribute);
        
        // Test for false positive.
        testLiteral = new ExpressionLiteral("test string data");
        filter.addRightValue(testLiteral);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));
        
        // Test for false negative.
        testLiteral = new ExpressionLiteral("incorrect test string data");
        filter.addRightValue(testLiteral);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
    }
    

    /** 
     * Helper class for the integer compare operators.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public static void compareNumberRunner(Expression testAttribute, 
                                           short filterType, 
                                           boolean test1, boolean test2, boolean test3)
        throws IllegalFilterException {
        CompareFilter filter = new CompareFilter(filterType);
        Expression testLiteral;
        filter.addLeftValue(testAttribute);
        
        testLiteral = new ExpressionLiteral(new Integer(1001));
        filter.addRightValue(testLiteral);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test1);
        
        testLiteral = new ExpressionLiteral(new Integer(1002));
        filter.addRightValue(testLiteral);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test2);
        
        testLiteral = new ExpressionLiteral(new Integer(1003));
        filter.addRightValue(testLiteral);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test3);
    }


    /** 
     * Tests the like operator.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testLike()
        throws IllegalFilterException {
        _log.getLoggerRepository().setThreshold("Debug");
        Expression testAttribute = null;

        // Set up string
        testAttribute = new ExpressionAttribute(testSchema, "testString");
        LikeFilter filter = new LikeFilter();
        filter.setValue(testAttribute);
        
        // Test for false negative.
        filter.setPattern(new ExpressionLiteral("test*"),"*",".","!");
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));
        
        // Test for false positive.
        filter.setPattern(new ExpressionLiteral("cows*"),"*",".","!");
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
        
        // Test for \ as an escape char
        
        filter.setPattern(new ExpressionLiteral("cows*"),"*",".","\\");
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertEquals("filter string doesn't match","[ testString is like cows.* ]",filter.toString());
    
        // test for escaped escapes
        
        filter.setPattern(new ExpressionLiteral("co!!ws*"),"*",".","!");
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertEquals("filter string doesn't match","[ testString is like co!ws.* ]",filter.toString());
        
        // test for escaped escapes followed by wildcard
        
        filter.setPattern(new ExpressionLiteral("co!!*ws*"),"*",".","!");
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        // curently fails
        //assertEquals("filter string doesn't match","[ testString is like co!.*ws.* ]",filter.toString());
    }


    /** 
     * Test the null operator.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testNull()
        throws IllegalFilterException {

        Expression testAttribute = null;

        // Test for false positive.
        testAttribute = new ExpressionAttribute(testSchema, "testString");
        NullFilter filter = new NullFilter();
        filter.nullCheckValue(testAttribute);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
        
        /*
          testFeature.setAttribute("testString", null);
          assertTrue(!filter.contains(testFeature));
          
          testFeature.setAttribute("testString", "test string data");
          _log.info( filter.toString());            
          _log.info( "contains feature: " + filter.contains(testFeature));
          assertTrue(!filter.contains(testFeature));
        */
    }

    /** 
     * Test the between operator.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testBetween()
        throws IllegalFilterException {

        // Set up the integer
        BetweenFilter filter = new BetweenFilter();
        Expression testLiteralLower = new ExpressionLiteral(new Integer(1001));
        Expression testAttribute = new ExpressionAttribute(testSchema, "testInteger");
        Expression testLiteralUpper = new ExpressionLiteral(new Integer(1003));
        
        // String tests
        filter.addLeftValue(testLiteralLower);
        filter.addMiddleValue(testAttribute);
        filter.addRightValue(testLiteralUpper);
        
        // Test for false negative.
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));
        
        // Test for false positive.
        testLiteralLower = new ExpressionLiteral(new Integer(1));
        testLiteralUpper = new ExpressionLiteral(new Integer(1000));
        filter.addLeftValue(testLiteralLower);
        filter.addRightValue(testLiteralUpper);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
    }

    /** 
     * Test the geometry operators.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testGeometry()
        throws IllegalFilterException {

        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1,2);
        coords[1] = new Coordinate(3,4);
        coords[2] = new Coordinate(5,6);
        
        // Test Equals
        GeometryFilter filter = new GeometryFilter(AbstractFilter.GEOMETRY_EQUALS);
        Expression left = new ExpressionAttribute(testSchema, "testGeometry");
        filter.addLeftGeometry(left);
        
        Expression right = new ExpressionLiteral(new LineString(coords, new PrecisionModel(), 1));
        filter.addRightGeometry(right);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));
        
        coords[0] = new Coordinate(0,0);
        right = new ExpressionLiteral(new LineString(coords, new PrecisionModel(), 1));
        filter.addRightGeometry(right);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
        
        // Test Disjoint
        filter = new GeometryFilter(AbstractFilter.GEOMETRY_DISJOINT);
        left = new ExpressionAttribute(testSchema, "testGeometry");
        filter.addLeftGeometry(left);
        
        coords[0] = new Coordinate(0,0);
        coords[1] = new Coordinate(3,0);
        coords[2] = new Coordinate(6,0);
        right = new ExpressionLiteral(new LineString(coords, new PrecisionModel(), 1));
        filter.addRightGeometry(right);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));
        
        coords[0] = new Coordinate(1,2);
        coords[1] = new Coordinate(3,0);
        coords[2] = new Coordinate(6,0);
        right = new ExpressionLiteral(new LineString(coords, new PrecisionModel(), 1));
        filter.addRightGeometry(right);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
        
        // Test BBOX
        filter = new GeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        left = new ExpressionAttribute(testSchema, "testGeometry");
        filter.addLeftGeometry(left);

        Coordinate[] coords2 = new Coordinate[5];
        coords2[0] = new Coordinate(0,0);
        coords2[1] = new Coordinate(10,0);
        coords2[2] = new Coordinate(10,10);
        coords2[3] = new Coordinate(0,10);
        coords2[4] = new Coordinate(0,0);
        right = new ExpressionLiteral(new Polygon(new LinearRing(coords2,new PrecisionModel(), 1),
                                                  null, new PrecisionModel(), 1));
        filter.addRightGeometry(right);
        _log.info( filter.toString());
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));
        
        coords2[0] = new Coordinate(0,0);
        coords2[1] = new Coordinate(1,0);
        coords2[2] = new Coordinate(1,1);
        coords2[3] = new Coordinate(0,1);
        coords2[4] = new Coordinate(0,0);
        right = new ExpressionLiteral(new Polygon(new LinearRing(coords2,new PrecisionModel(), 1),
                                                  null, new PrecisionModel(), 1));
        filter.addRightGeometry(right);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
    }
    

    /** 
     * Test the logic operators.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testLogic()
        throws IllegalFilterException {

        Expression testAttribute = null;

        // Set up true sub filter
        testAttribute = new ExpressionAttribute(testSchema, "testString");
        CompareFilter filterTrue = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression testLiteral;
        filterTrue.addLeftValue(testAttribute);
        testLiteral = new ExpressionLiteral("test string data");
        filterTrue.addRightValue(testLiteral);
        
        // Set up false sub filter
        CompareFilter filterFalse = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
        filterFalse.addLeftValue(testAttribute);
        testLiteral = new ExpressionLiteral("incorrect test string data");
        filterFalse.addRightValue(testLiteral);
        
        // Test OR for false negatives
        LogicFilter filter = new LogicFilter(filterFalse, filterTrue, AbstractFilter.LOGIC_OR);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));
        
        // Test OR for false negatives
        filter = new LogicFilter(filterTrue, filterTrue, AbstractFilter.LOGIC_OR);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));
        
        // Test OR for false positives
        filter = new LogicFilter(filterFalse, filterFalse, AbstractFilter.LOGIC_OR);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
        
        // Test AND for false positives
        filter = new LogicFilter(filterFalse, filterTrue, AbstractFilter.LOGIC_AND);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        // Test AND for false positives
        filter = new LogicFilter(filterTrue, filterFalse, AbstractFilter.LOGIC_AND);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        // Test AND for false positives
        filter = new LogicFilter(filterTrue, filterTrue, AbstractFilter.LOGIC_AND);
        _log.info( filter.toString());            
        _log.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

    }



}
