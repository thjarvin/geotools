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
package org.geotools.filter;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.expression.PropertyAccessor;
import org.geotools.filter.expression.PropertyAccessorFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.And;
import org.opengis.filter.Id;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * Unit test for filters.  Note that this unit test does not encompass all of
 * filter package, just the filters themselves.  There is a seperate unit test
 * for expressions.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 * @source $URL$
 */
public class FilterTest extends TestCase {
    /** The logger for the filter module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.filter");

    /** SimpleFeature on which to preform tests */
    private static SimpleFeature testFeature = null;

    /** Schema on which to preform tests */
    private static SimpleFeatureType testSchema = null;
    boolean set = false;
    
    org.opengis.filter.FilterFactory2 fac = CommonFactoryFinder.getFilterFactory2(null);

    /** Test suite for this test case */
    TestSuite suite = null;

    private Calendar calDateTime;
    private Calendar calTime;
    private Calendar calDate;

    /**
     * Constructor with test name.
     *
     * @param testName DOCUMENT ME!
     */
    public FilterTest(String testName) {
        super(testName);

        //BasicConfigurator.configure();
        //LOGGER = org.geotools.util.logging.Logging.getLogger(FilterTest.class);
        //LOGGER.getLoggerRepository().setThreshold(Level.INFO);
    }

    /**
     * Main for test runner.
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Required suite builder.
     *
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(FilterTest.class);

        return suite;
    }

    /**
     * Sets up a schema and a test feature.
     *
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalAttributeException If problem setting up the feature.
     */
    protected void setUp() throws SchemaException, IllegalAttributeException {
        if (set) {
            return;
        }

        set = true;
        
        fac = FilterFactoryFinder.createFilterFactory();
        
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName( "testFeatureType");
        ftb.add("testGeometry", LineString.class);
        ftb.add("testBoolean", Boolean.class);
        ftb.add("testCharacter", Character.class);
        ftb.add("testByte", Byte.class);
        ftb.add("testShort", Short.class);
        ftb.add("testInteger", Integer.class);
        ftb.add("testLong", Long.class);
        ftb.add("testFloat", Float.class);
        ftb.add("testDouble", Double.class);
        ftb.add("testString", String.class);
        ftb.add("testString2", String.class);
        ftb.add("date", java.sql.Date.class);
        ftb.add("time", java.sql.Time.class);
        ftb.add("datetime1", java.util.Date.class);
        ftb.add("datetime2", java.sql.Timestamp.class);
        testSchema = ftb.buildFeatureType();

        //LOGGER.finer("added string to feature type");
        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

        // Builds the test feature
        Object[] attributes = new Object[15];
        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        attributes[0] = gf.createLineString(coords);
        attributes[1] = new Boolean(true);
        attributes[2] = new Character('t');
        attributes[3] = new Byte("10");
        attributes[4] = new Short("101");
        attributes[5] = new Integer(1002);
        attributes[6] = new Long(10003);
        attributes[7] = new Float(10000.4);
        attributes[8] = new Double(100000.5);
        attributes[9] = "test string data";
        attributes[10] = "cow $10";
        
        // setup date ones
        calDate = Calendar.getInstance();
        calDate.clear();
        calDate.set(2007, 7, 15);
        calTime = Calendar.getInstance();
        calTime.clear();
        calTime.set(Calendar.HOUR_OF_DAY, 12);
        calDateTime = Calendar.getInstance();
        calDateTime.clear();
        calDateTime.set(2007, 7, 15, 12, 00, 00);
        attributes[11] = new java.sql.Date(calDate.getTimeInMillis());
        attributes[12] = new java.sql.Time(calTime.getTimeInMillis());
        attributes[13] = calDateTime.getTime();
        attributes[14] = new java.sql.Timestamp(calDateTime.getTimeInMillis());

        // Creates the feature itself
        //FlatFeatureFactory factory = new FlatFeatureFactory(testSchema);
        testFeature = SimpleFeatureBuilder.build(testSchema, attributes, null);
        //LOGGER.finer("...flat feature created");
    }
    
    public void testLikeToSQL()
    {
    	assertTrue("BroadWay%".equals( LikeFilterImpl.convertToSQL92('!','*','.',"BroadWay*")));
		assertTrue("broad#ay".equals(  LikeFilterImpl.convertToSQL92('!','*','.',"broad#ay")));
		assertTrue("broadway".equals(  LikeFilterImpl.convertToSQL92('!','*','.',"broadway")));
		 
		assertTrue("broad_ay".equals(LikeFilterImpl.convertToSQL92('!','*','.',"broad.ay")));
		assertTrue("broad.ay".equals(LikeFilterImpl.convertToSQL92('!','*','.',"broad!.ay")));
				 
		assertTrue("broa''dway".equals(LikeFilterImpl.convertToSQL92('!','*','.',"broa'dway")));
		assertTrue("broa''''dway".equals(LikeFilterImpl.convertToSQL92('!','*','.',"broa''dway")));
				 
		assertTrue("broadway_".equals(LikeFilterImpl.convertToSQL92('!','*','.',"broadway.")));
		assertTrue("broadway".equals(LikeFilterImpl.convertToSQL92('!','*','.',"broadway!")));
		assertTrue("broadway!".equals(LikeFilterImpl.convertToSQL92('!','*','.',"broadway!!")));
    }
    
    /**
     * Sets up a schema and a test feature.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testCompare() throws IllegalFilterException {
        // Test all integer permutations
        Expression testAttribute = new AttributeExpressionImpl(testSchema,
                "testInteger");
        compareNumberRunner(testAttribute, FilterType.COMPARE_EQUALS,
            false, true, false);
        compareNumberRunner(testAttribute, FilterType.COMPARE_GREATER_THAN,
            true, false, false);
        compareNumberRunner(testAttribute, FilterType.COMPARE_LESS_THAN,
            false, false, true);
        compareNumberRunner(testAttribute,
            FilterType.COMPARE_GREATER_THAN_EQUAL, true, true, false);
        compareNumberRunner(testAttribute,
            FilterType.COMPARE_LESS_THAN_EQUAL, false, true, true);
        
        // test all date permutations, with string/date conversion included
        testAttribute = new AttributeExpressionImpl(testSchema, "date");
        compareSqlDateRunner(testAttribute, FilterType.COMPARE_EQUALS,
                false, true, false);
        compareSqlDateRunner(testAttribute, FilterType.COMPARE_GREATER_THAN,
                true, false, false);
        compareSqlDateRunner(testAttribute, FilterType.COMPARE_LESS_THAN,
                false, false, true);
        compareSqlDateRunner(testAttribute, FilterType.COMPARE_GREATER_THAN_EQUAL,
                true, true, false);
        compareSqlDateRunner(testAttribute, FilterType.COMPARE_LESS_THAN_EQUAL,
                false, true, true);
        
        // test all date permutations, with string/date conversion included
        testAttribute = new AttributeExpressionImpl(testSchema, "time");
        compareSqlTimeRunner(testAttribute, FilterType.COMPARE_EQUALS,
                false, true, false);
        compareSqlTimeRunner(testAttribute, FilterType.COMPARE_GREATER_THAN,
                true, false, false);
        compareSqlTimeRunner(testAttribute, FilterType.COMPARE_LESS_THAN,
                false, false, true);
        compareSqlTimeRunner(testAttribute, FilterType.COMPARE_GREATER_THAN_EQUAL,
                true, true, false);
        compareSqlTimeRunner(testAttribute, FilterType.COMPARE_LESS_THAN_EQUAL,
                false, true, true);

        // Set up the string test.
        testAttribute = new AttributeExpressionImpl(testSchema, "testString");

        CompareFilter filter = FilterFactoryFinder.createFilterFactory()
        	.createCompareFilter(FilterType.COMPARE_EQUALS);
        Expression testLiteral;
        filter.addLeftValue(testAttribute);

        // Test for false positive.
        testLiteral = new LiteralExpressionImpl("test string data");
        filter.addRightValue(testLiteral);

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        // Test for false negative.
        testLiteral = new LiteralExpressionImpl("incorrect test string data");
        filter.addRightValue(testLiteral);

        assertTrue(!filter.contains(testFeature));

	filter = FilterFactoryFinder.createFilterFactory()
		.createCompareFilter(FilterType.COMPARE_LESS_THAN);
	filter.addLeftValue(testAttribute);

        // Test for false positive.
        testLiteral = new LiteralExpressionImpl("zebra");
        filter.addRightValue(testLiteral);
	assertTrue(filter.contains(testFeature));

	testLiteral = new LiteralExpressionImpl("blorg");
        filter.addRightValue(testLiteral);
	assertTrue(!filter.contains(testFeature));
    }
    
    
	

    /**
     * Helper class for the integer compare operators.
     *
     * @param testAttribute DOCUMENT ME!
     * @param filterType DOCUMENT ME!
     * @param test1 DOCUMENT ME!
     * @param test2 DOCUMENT ME!
     * @param test3 DOCUMENT ME!
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public static void compareNumberRunner(Expression testAttribute,
        short filterType, boolean test1, boolean test2, boolean test3)
        throws IllegalFilterException {
        CompareFilter filter = FilterFactoryFinder.createFilterFactory()
        	.createCompareFilter(filterType);
        Expression testLiteral;
        filter.addLeftValue(testAttribute);

        testLiteral = new LiteralExpressionImpl(new Integer(1001));
        filter.addRightValue(testLiteral);

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test1);

        testLiteral = new LiteralExpressionImpl(new Integer(1002));
        filter.addRightValue(testLiteral);

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test2);

        testLiteral = new LiteralExpressionImpl(new Integer(1003));
        filter.addRightValue(testLiteral);

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test3);
    }
    
    /**
     * Helper class for the integer compare operators.
     *
     * @param testAttribute DOCUMENT ME!
     * @param filterType DOCUMENT ME!
     * @param test1 DOCUMENT ME!
     * @param test2 DOCUMENT ME!
     * @param test3 DOCUMENT ME!
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void compareSqlDateRunner(Expression testAttribute,
        short filterType, boolean test1, boolean test2, boolean test3)
        throws IllegalFilterException {
        CompareFilter filter = FilterFactoryFinder.createFilterFactory()
                .createCompareFilter(filterType);
        Expression testLiteral;
        filter.addLeftValue(testAttribute);

        Calendar calLocal = Calendar.getInstance();
        calLocal.setTime(calDate.getTime());
        calLocal.set(Calendar.DAY_OF_MONTH, calDateTime.get(Calendar.DAY_OF_MONTH) - 1);
        testLiteral = new LiteralExpressionImpl(new java.sql.Date(calLocal.getTimeInMillis()).toString());
        filter.addRightValue(testLiteral);

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test1);

        testLiteral = new LiteralExpressionImpl(new java.sql.Date(calDate.getTimeInMillis()).toString());
        filter.addRightValue(testLiteral);

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test2);

        calLocal.set(Calendar.DAY_OF_MONTH, calDateTime.get(Calendar.DAY_OF_MONTH) + 1);
        testLiteral = new LiteralExpressionImpl(new java.sql.Date(calLocal.getTimeInMillis()).toString());
        filter.addRightValue(testLiteral);

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test3);
    }
    
    /**
     * Helper class for the integer compare operators.
     *
     * @param testAttribute DOCUMENT ME!
     * @param filterType DOCUMENT ME!
     * @param test1 DOCUMENT ME!
     * @param test2 DOCUMENT ME!
     * @param test3 DOCUMENT ME!
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void compareSqlTimeRunner(Expression testAttribute,
        short filterType, boolean test1, boolean test2, boolean test3)
        throws IllegalFilterException {
        CompareFilter filter = FilterFactoryFinder.createFilterFactory()
                .createCompareFilter(filterType);
        Expression testLiteral;
        filter.addLeftValue(testAttribute);

        Calendar calLocal = Calendar.getInstance();
        calLocal.setTime(calTime.getTime());
        calLocal.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY) - 1);
        testLiteral = new LiteralExpressionImpl(new java.sql.Time(calLocal.getTimeInMillis()).toString());
        filter.addRightValue(testLiteral);

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test1);

        testLiteral = new LiteralExpressionImpl(new java.sql.Time(calTime.getTimeInMillis()).toString());
        filter.addRightValue(testLiteral);

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test2);

        calLocal.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY) + 1);
        testLiteral = new LiteralExpressionImpl(new java.sql.Time(calLocal.getTimeInMillis()).toString());
        filter.addRightValue(testLiteral);

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test3);
    }

    /**
     * Tests the like operator.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testLike() throws IllegalFilterException {
        

        Pattern compPattern = java.util.regex.Pattern.compile("test.*");
        Matcher matcher = compPattern.matcher("test string");
        
        assertTrue(matcher.matches());
        
        Expression testAttribute = null;

        // Set up string
        testAttribute = new AttributeExpressionImpl(testSchema, "testString");

        PropertyIsLike filter = fac.like(testAttribute, "test*", "*", ".", "!");
        assertTrue(filter.evaluate(testFeature));

        // Test for false positive.
        filter = fac.like(testAttribute, "cows*", "*", ".", "!");
        assertFalse(filter.evaluate(testFeature));

        // Test we don't match if single character is missing
        filter = fac.like(testAttribute, "test*a.", "*", ".", "!");
        assertFalse(filter.evaluate(testFeature));
        
        // Test we do match if the single char is there
        filter = fac.like(testAttribute, "test*dat.", "*", ".", "!");
        assertTrue(filter.evaluate(testFeature));
    }

    /**
     * Test the null operator.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testNull() throws IllegalFilterException {
        // Test for false positive.
        PropertyName testAttribute = new AttributeExpressionImpl(testSchema, "testString");

        PropertyIsNull filter = fac.isNull(org.opengis.filter.expression.Expression.NIL);
        assertTrue(filter.evaluate(testFeature));
        
        filter = fac.isNull(testAttribute);
        assertFalse(filter.evaluate(testFeature));
    }
    
    
    /**
         * A filter is composed of a logic AND bettween a non null check and
         * a comparison filter, for an AttributeExpression. 
         * If the AttributeExpression evaluates to null, the short-circuit comparison
         * in the LogicFilter should return without throwing a NullPointerException.
         * If short-circuit evaluation would not be done in LogicFilter, then a NullPointerException
         * would be thrown.
         *
         * @throws IllegalFilterException If the constructed filter is not valid.
        */
        public void testCompareShortCircuit() throws IllegalFilterException, IllegalAttributeException {
           // Test all integer permutations
            Expression testAttribute = new AttributeExpressionImpl(testSchema,
                   "testInteger");
    
            NullFilterImpl nullFilter = new NullFilterImpl();
            nullFilter.nullCheckValue(testAttribute);
           
            Filter notNullFilter  = (Filter) fac.not(nullFilter);
            
            PropertyIsEqualTo compareFilter = fac.equals( testAttribute, fac.literal(10));
            
            
            testFeature.setAttribute("testInteger", null);
            assertEquals( false, compareFilter.evaluate( testFeature ) );
            
            assertTrue(nullFilter.contains(testFeature));
            assertFalse(notNullFilter.contains(testFeature));
            
            //test AND
            Filter finalFilter = notNullFilter.and(compareFilter);
            try{
               assertFalse(finalFilter.contains(testFeature));
            }catch(NullPointerException e){
               fail("Short-circuit evaluation was not performed by LogicFilter: " + e.getMessage());
            }
            
            //test OR
            finalFilter = nullFilter.or(compareFilter);
            try{
               assertTrue(finalFilter.contains(testFeature));
            }catch(NullPointerException e){
               fail("Short-circuit evaluation was not performed by LogicFilter: " + e.getMessage());
            }
        }


    /**
     * Test the between operator.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testBetween() throws IllegalFilterException {
        // Set up the integer
        Literal testLiteralLower = fac.literal(1001);
        PropertyName testAttribute = fac.property("testInteger");
        Literal testLiteralUpper = fac.literal(1003);

        // String tests
        PropertyIsBetween filter = fac.between(testAttribute, testLiteralLower, testLiteralUpper);
        assertTrue(filter.evaluate(testFeature));

        // Test for false positive.
        testLiteralLower = fac.literal(1);
        testLiteralUpper = fac.literal(1000);
        filter = fac.between(testAttribute, testLiteralLower, testLiteralUpper); 

        //LOGGER.finer( filter.toString());            
        //LOGGER.finer( "contains feature: " + filter.contains(testFeature));
        assertFalse(filter.evaluate(testFeature));
    }


    public void testBetweenStrings() throws IllegalFilterException {
        // Set up the integer
        Literal testLiteralLower = new LiteralExpressionImpl("blorg");
        PropertyName testAttribute = new AttributeExpressionImpl(testSchema,
                "testString");
        Literal testLiteralUpper = new LiteralExpressionImpl("tron");

        // String tests
        PropertyIsBetween filter = fac.between(testAttribute, testLiteralLower, testLiteralUpper);
        assertTrue(filter.evaluate(testFeature));

        // Test for false positive.
        testLiteralLower = new LiteralExpressionImpl("zebra");
        testLiteralUpper = new LiteralExpressionImpl("zikes");
        filter = fac.between(testAttribute, testLiteralLower, testLiteralUpper);
        assertFalse(filter.evaluate(testFeature));
    }


    /**
     * Test the geometry operators.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testGeometry() throws IllegalFilterException {
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

        // Test Equals
        PropertyName left = new AttributeExpressionImpl(testSchema, "testGeometry");
        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        Literal right = new LiteralExpressionImpl(gf.createLineString(coords));
        Equals filter = fac.equal(left, right);

        LOGGER.finer( filter.toString());            
        LOGGER.finer( "contains feature: " + filter.evaluate(testFeature));
        assertTrue(filter.evaluate(testFeature));

        coords[0] = new Coordinate(0, 0);
        right = new LiteralExpressionImpl(gf.createLineString(coords));
        filter = fac.equal(left, right); 

        LOGGER.finer( filter.toString());            
        LOGGER.finer( "contains feature: " + filter.evaluate(testFeature));
        assertTrue(!filter.evaluate(testFeature));

        // Test Disjoint
        left = new AttributeExpressionImpl(testSchema, "testGeometry");
        coords[0] = new Coordinate(0, 0);
        coords[1] = new Coordinate(3, 0);
        coords[2] = new Coordinate(6, 0);
        right = new LiteralExpressionImpl(gf.createLineString(coords));
        Disjoint disjoint = fac.disjoint(left, right);

        LOGGER.finer( disjoint.toString());            
        LOGGER.finer( "contains feature: " + disjoint.evaluate(testFeature));
        assertTrue(disjoint.evaluate(testFeature));

        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 0);
        coords[2] = new Coordinate(6, 0);
        right = new LiteralExpressionImpl(gf.createLineString(coords));
        disjoint = fac.disjoint(left, right);

        LOGGER.finer( filter.toString());            
        LOGGER.finer( "contains feature: " + disjoint.evaluate(testFeature));
        assertTrue(!disjoint.evaluate(testFeature));

        // Test BBOX
        left = new AttributeExpressionImpl(testSchema, "testGeometry");
        BBOX bbox = fac.bbox(left, 0, 0, 10, 10, null);

        LOGGER.finer( bbox.toString());
        LOGGER.finer( "contains feature: " + bbox.evaluate(testFeature));
        assertTrue(bbox.evaluate(testFeature));

        bbox = fac.bbox(left, 0, 0, 1, 1, null);

        LOGGER.finer( bbox.toString());            
        LOGGER.finer( "contains feature: " + bbox.evaluate(testFeature));
        assertTrue(!bbox.evaluate(testFeature));
    }

    public void testDistanceGeometry() throws Exception {
        // Test DWithin
        GeometryDistanceFilter filter = FilterFactoryFinder.createFilterFactory()
        	.createGeometryDistanceFilter(AbstractFilter.GEOMETRY_DWITHIN);
        	
        Expression left = new AttributeExpressionImpl(testSchema, "testGeometry");
        filter.addLeftGeometry(left);

        Coordinate[] coords2 = new Coordinate[5];
        coords2[0] = new Coordinate(10, 10);
        coords2[1] = new Coordinate(15, 10);
        coords2[2] = new Coordinate(15, 15);
        coords2[3] = new Coordinate(10, 15);
        coords2[4] = new Coordinate(10, 10);

        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        Expression right = new LiteralExpressionImpl(gf.createPolygon(gf.createLinearRing(
                coords2),null));
        filter.addRightGeometry(right);
        filter.setDistance(20);
        LOGGER.finer(filter.toString());
        LOGGER.finer("contains feature: " + filter.contains(testFeature));

        //assertTrue(filter.contains(testFeature));
        filter.setDistance(2);
        LOGGER.finer(filter.toString());
        LOGGER.finer("contains feature: " + filter.contains(testFeature));

        //Test Beyond
        GeometryDistanceFilter filterB = FilterFactoryFinder.createFilterFactory()
        	.createGeometryDistanceFilter(AbstractFilter.GEOMETRY_BEYOND);
        filterB.addLeftGeometry(left);
        filterB.addRightGeometry(right);
        filterB.setDistance(20);
        LOGGER.finer(filterB.toString());
        LOGGER.finer("contains feature: " + filterB.contains(testFeature));

        //assertTrue(filter.contains(testFeature));
        filterB.setDistance(2);
        LOGGER.finer(filterB.toString());
        LOGGER.finer("contains feature: " + filterB.contains(testFeature));

        /*coords2[0] = new Coordinate(20,20);
           /coords2[1] = new Coordinate(21,20);
           coords2[2] = new Coordinate(21,21);
           coords2[3] = new Coordinate(20,21);
           coords2[4] = new Coordinate(20,20);
           right = new LiteralExpressionImpl(new Polygon(new LinearRing(coords2,new PrecisionModel(), 1),
                                                     null, new PrecisionModel(), 1));
           filter.addRightGeometry(right);
           LOGGER.finer( filter.toString());
           LOGGER.finer( "contains feature: " + filter.contains(testFeature));
           assertTrue(!filter.contains(testFeature));
         */

        //Test Beyond
    }

    public void testFid() {
        Id ff = fac.id(new HashSet<FeatureId>());
        assertFalse(ff.evaluate(testFeature));
        ff = fac.id(Collections.singleton(fac.featureId(testFeature.getID())));
        assertTrue(ff.evaluate(testFeature));
        assertFalse(ff.evaluate(null));
    }
    
    
    /**
     * Test the logic operators.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testOrFilter() throws IllegalFilterException {
        // Set up true sub filter
        PropertyName testAttribute = new AttributeExpressionImpl(testSchema, "testString");
        Literal testLiteral = new LiteralExpressionImpl("test string data");
        
        PropertyIsEqualTo filterTrue = fac.equals(testAttribute, testLiteral);

        // Set up false sub filter
        testLiteral = new LiteralExpressionImpl("incorrect test string data");
        PropertyIsEqualTo filterFalse = fac.equals(testAttribute, testLiteral);

        // Test OR for false negatives
        Or filter = fac.or(filterFalse, filterTrue);

        LOGGER.finer( filter.toString());            
        LOGGER.finer( "contains feature: " + filter.evaluate(testFeature));
        assertTrue(filter.evaluate(testFeature));

        // Test OR for false negatives
        filter = fac.or(filterTrue, filterTrue);

        LOGGER.finer( filter.toString());            
        LOGGER.finer( "contains feature: " + filter.evaluate(testFeature));
        assertTrue(filter.evaluate(testFeature));

        // Test OR for false positives
        filter = fac.or(filterFalse, filterFalse);
        assertFalse(filter.evaluate(testFeature));

        LOGGER.finer( filter.toString());            
        LOGGER.finer( "contains feature: " + filter.evaluate(testFeature));
        assertFalse(filter.evaluate(testFeature));
    }
    
    /**
     * Test the logic operators.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testAndFilter() throws IllegalFilterException {
        // Set up true sub filter
        PropertyName testAttribute = new AttributeExpressionImpl(testSchema, "testString");
        Literal testLiteral = new LiteralExpressionImpl("test string data");
        
        PropertyIsEqualTo filterTrue = fac.equals(testAttribute, testLiteral);

        // Set up false sub filter
        testLiteral = new LiteralExpressionImpl("incorrect test string data");
        PropertyIsEqualTo filterFalse = fac.equals(testAttribute, testLiteral);

        // Test AND for false positives
        And filter = fac.and(filterFalse, filterTrue);

        LOGGER.finer( filter.toString());            
        LOGGER.finer( "contains feature: " + filter.evaluate(testFeature));
        assertTrue(!filter.evaluate(testFeature));

        // Test AND for false positives
        filter = fac.and(filterTrue, filterFalse);

        LOGGER.finer( filter.toString());            
        LOGGER.finer( "contains feature: " + filter.evaluate(testFeature));
        assertTrue(!filter.evaluate(testFeature));

        // Test AND for false positives
        filter = fac.and(filterTrue, filterTrue);
        LOGGER.finer( filter.toString());            
        LOGGER.finer( "contains feature: " + filter.evaluate(testFeature));
        assertTrue(filter.evaluate(testFeature));
        
        // finally test nothing shortcut
        assertFalse(fac.not(filter).evaluate(testFeature));
    }
    
    public void testLiteralExpression(){
    	LiteralExpressionImpl literal;
		literal = new LiteralExpressionImpl(1.0D);
		assertEquals(ExpressionType.LITERAL_DOUBLE, literal.getType());
		assertEquals(new Double(1.0D), literal.evaluate((Feature)null));

		GeometryFactory gf = new GeometryFactory();
		literal = new LiteralExpressionImpl(gf.createPoint(new Coordinate(0,0)));
		assertEquals(ExpressionType.LITERAL_GEOMETRY, literal.getType());
		Geometry value = (Geometry) literal.evaluate((Feature)null);
		assertTrue(gf.createPoint(new Coordinate(0,0)).equalsExact(value));
		
		literal = new LiteralExpressionImpl(1);
		assertEquals(ExpressionType.LITERAL_INTEGER, literal.getType());
		assertEquals(new Integer(1), literal.evaluate((Feature)null));

		literal = new LiteralExpressionImpl(1L);
		assertEquals(ExpressionType.LITERAL_LONG, literal.getType());
		assertEquals(new Long(1), literal.evaluate((Feature)null));

		literal = new LiteralExpressionImpl("string value");
		assertEquals(ExpressionType.LITERAL_STRING, literal.getType());
		assertEquals("string value", literal.evaluate((Feature)null));

		literal = new LiteralExpressionImpl(new Date(0));
		assertEquals(ExpressionType.LITERAL_UNDECLARED, literal.getType());
		assertEquals(new Date(0), literal.evaluate((Feature)null));

		literal = new LiteralExpressionImpl(null);
		assertEquals(ExpressionType.LITERAL_UNDECLARED, literal.getType());
		assertNull(literal.evaluate((Feature)null));
    }
    
    /**
     * Test that Filter works over Object as expected, provided there exists a
     * {@link PropertyAccessor} for the given kind of object. 
     *
     */
    public void testEvaluateNonFeatureObject(){
    	MockDataObject object = new MockDataObject();
    	object.intVal = 5;
    	object.stringVal = "cinco";
    	
    	org.opengis.filter.Filter f = fac.greater(fac.property("intVal"), fac.literal(3));
    	
    	assertTrue(f.evaluate(object));
    	
    	org.opengis.filter.Filter f2 = fac.and(f, fac.equals(fac.property("stringVal"), fac.literal("cinco")));
    	
    	assertTrue(f2.evaluate(object));

    	org.opengis.filter.Filter f3 = fac.and(f, fac.equals(fac.property("stringVal"), fac.literal("seis")));
    	
    	assertFalse(f3.evaluate(object));

    	org.opengis.filter.Filter f4 = fac.not(fac.and(f, fac.equals(fac.property("stringVal"), fac.literal("cinco"))));
    	
    	assertFalse(f4.evaluate(object));
    }
    
    /**
	 * A simple data object to be used on testing Filter.evaluate(Object)
	 * through {@link MockPropertyAccessorFactory}
	 * 
	 * @author Gabriel Roldan, Axios Engineering
	 */
	public static class MockDataObject {
		public int intVal;

		public String stringVal;
		
		public MockDataObject(){
			this(0, null);
		}
		
		public MockDataObject(int intVal, String stringVal){
			this.intVal = intVal;
			this.stringVal = stringVal;
		}
	}

	/**
	 * A {@link PropertyAccessorFactory} intended to be used on testing that the
	 * Filter implementation works over Object as expected, and not only over
	 * SimpleFeature
	 * 
	 * @author Gabriel Roldan, Axios Engineering
	 */
	public static class MockPropertyAccessorFactory implements
			PropertyAccessorFactory {

		public PropertyAccessor createPropertyAccessor(Class type,
				String xpath, Class target, Hints hints) {
			if (!MockDataObject.class.equals(type)) {
				return null;
			}
			return new PropertyAccessor() {
				public boolean canHandle(Object object, String xpath,
						Class target) {
					return object instanceof MockDataObject;
				}

				public Object get(Object object, String xpath, Class target)
						throws IllegalArgumentException {
					if (object == null)
						return null;

					try {
						Field field = MockDataObject.class.getField(xpath);
						Object value = field.get(object);
						return value;
					} catch (Exception e) {
						throw (IllegalArgumentException) new IllegalArgumentException(
								"Illegal property name: " + xpath).initCause(e);
					}
				}

				public void set(Object object, String xpath, Object value,
						Class target) throws IllegalAttributeException,
						IllegalArgumentException {
					throw new UnsupportedOperationException();
				}
			};
		}
    	
    }
}
