/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.complex;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureImpl;
import org.geotools.feature.Types;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.FunctionExpressionImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.xml.sax.Attributes;

/**
 * This is the tests for feature chaining; nesting complex attributes (feature and non-feature)
 * inside another complex attribute.
 * 
 * @author Rini Angreani, Curtin University of Technology
 */
public class FeatureChainingTest extends TestCase {
    static final String GSMLNS = "http://www.cgi-iugs.org/xml/GeoSciML/2";

    static final String GMLNS = "http://www.opengis.net/gml";

    static final Name MAPPED_FEATURE_TYPE = Types.typeName(GSMLNS, "MappedFeatureType");

    static final Name MAPPED_FEATURE = Types.typeName(GSMLNS, "MappedFeature");

    static final Name GEOLOGIC_UNIT_TYPE = Types.typeName(GSMLNS, "GeologicUnitType");

    static final Name GEOLOGIC_UNIT = Types.typeName(GSMLNS, "GeologicUnit");

    static final Name COMPOSITION_PART_TYPE = Types.typeName(GSMLNS, "CompositionPartType");

    static final Name COMPOSITION_PART = Types.typeName(GSMLNS, "CompositionPart");

    static final Name CGI_TERM_VALUE = Types.typeName(GSMLNS, "CGI_TermValue");

    static final Name CGI_TERM_VALUE_TYPE = Types.typeName(GSMLNS, "CGI_TermValueType");

    static final Name CONTROLLED_CONCEPT = Types.typeName(GSMLNS, "ControlledConcept");

    static FilterFactory ff = new FilterFactoryImpl(null);

    /**
     * Map of geological unit values to mapped feature objects based on
     * mappedFeaturePropertyFile.properties
     */
    final static Map<String, String> mfToGuMap = new HashMap<String, String>() {
        {
            put("mf1", "gu.25699");
            put("mf2", "gu.25678");
            put("mf3", "gu.25678");
            put("mf4", "gu.25682");
        }
    };

    /**
     * Map of compositional part values to geological unit objects based on geologicUnit.properties
     */
    final static Map<String, String> guToCpMap = new HashMap<String, String>() {
        {
            put("gu.25699", "cp.167775491936278844");
            put("gu.25678", "cp.167775491936278844;cp.167775491936278856");
            put("gu.25682", "cp.167775491936278812");
        }
    };

    /**
     * Map of exposure colour values to geological unit objects based on geologicUnit.properties
     */
    final static Map<String, String> guToExposureColorMap = new HashMap<String, String>() {
        {
            put("gu.25699", "Blue");
            put("gu.25678", "Yellow;Blue");
            put("gu.25682", "Red");
        }
    };

    public static final String CONTAINS_TEXT = "contains_text";

    /**
     * Map of out crop character values to geological unit objects based on geologicUnit.properties
     */
    static Map<String, String> guToOutcropCharacterMap = new HashMap<String, String>() {
        {
            put("gu.25699", "x");
            put("gu.25678", "x;y");
            put("gu.25682", "z");

        }
    };

    final String schemaBase = "/test-data/";

    private FeatureSource<FeatureType, Feature> mfSource;

    /**
     * Generated mapped features
     */
    private FeatureCollection<FeatureType, Feature> mfFeatures;

    /**
     * Generated geological unit features
     */
    private FeatureCollection<FeatureType, Feature> guFeatures;

    /**
     * Generated compositional part fake "features"
     */
    private FeatureCollection<FeatureType, Feature> cpFeatures;

    /**
     * Generated controlled concept fake "features"
     */
    private FeatureCollection<FeatureType, Feature> ccFeatures;

    /**
     * Test that chaining works
     * 
     * @throws Exception
     */
    public void testFeatureChaining() throws Exception {
        this.loadDataAccesses();

        Iterator<Feature> mfIterator = mfFeatures.iterator();

        Iterator<Feature> guIterator = guFeatures.iterator();

        // Extract all geological unit features into a map by id
        Map<String, Feature> guMap = new HashMap<String, Feature>();
        Feature guFeature;
        while (guIterator.hasNext()) {
            guFeature = (Feature) guIterator.next();
            String guId = guFeature.getIdentifier().getID();
            if (!guMap.containsKey(guId)) {
                guMap.put(guId, guFeature);
            }
        }

        // Extract all compositional part "features" into a map by id
        Iterator<Feature> cpIterator = cpFeatures.iterator();
        Map<String, Feature> cpMap = new HashMap<String, Feature>();
        Feature cpFeature;
        while (cpIterator.hasNext()) {
            cpFeature = (Feature) cpIterator.next();
            String cpId = cpFeature.getIdentifier().getID();
            if (!cpMap.containsKey(cpId)) {
                cpMap.put(cpId, cpFeature);
            }
        }

        Feature mfFeature;
        Collection<Property> nestedGuFeatures;
        String guId;
        final String NESTED_LINK = "specification";
        Collection<Property> nestedCpFeatures;
        String cpId;
        while (mfIterator.hasNext()) {
            mfFeature = (Feature) mfIterator.next();
            String mfId = mfFeature.getIdentifier().toString();
            String[] guIds = this.mfToGuMap.get(mfId).split(";");

            // make sure we have the right number of nested features
            nestedGuFeatures = (Collection<Property>) mfFeature.getProperties(NESTED_LINK);
            assertEquals(guIds.length, nestedGuFeatures.size());

            ArrayList<String> nestedGuIds = new ArrayList<String>();

            for (Property property : nestedGuFeatures) {
                Object value = property.getValue();
                assertNotNull(value);
                assertEquals(value instanceof Collection, true);
                assertEquals(((Collection) value).size(), 1);

                Feature nestedGuFeature = (Feature) ((Collection) value).iterator().next();
                /**
                 * Test geological unit
                 */
                // make sure each of the nested geologic unit is valid
                guId = nestedGuFeature.getIdentifier().toString();
                assertEquals(true, guMap.containsKey(guId));

                nestedGuIds.add(guId);

                // make sure the nested geologic unit feature has the right properties
                guFeature = guMap.get(guId.toString());
                Collection<Property> guProperties = guFeature.getProperties();
                assertEquals(guProperties, nestedGuFeature.getProperties());

                /**
                 * Test compositional part
                 */
                // make sure the right number of nested features are there
                String[] cpIds = this.guToCpMap.get(guId).split(";");
                nestedCpFeatures = (Collection<Property>) guFeature.getProperties("composition");
                assertEquals(cpIds.length, nestedCpFeatures.size());

                ArrayList<String> nestedCpIds = new ArrayList<String>();
                for (Property cpProperty : nestedCpFeatures) {
                    Object cpPropertyValue = cpProperty.getValue();
                    assertNotNull(cpPropertyValue);
                    assertEquals(cpPropertyValue instanceof Collection, true);
                    assertEquals(((Collection) cpPropertyValue).size(), 1);

                    Feature nestedCpFeature = (Feature) ((Collection) cpPropertyValue).iterator()
                            .next();
                    // make sure each of the nested compositional part feature is valid
                    cpId = nestedCpFeature.getIdentifier().toString();
                    assertEquals(true, cpMap.containsKey(cpId));

                    nestedCpIds.add(cpId);

                    // make sure each of the nested compositional part has the right properties
                    cpFeature = cpMap.get(cpId.toString());
                    Collection<Property> cpProperties = cpFeature.getProperties();
                    assertEquals(cpProperties, nestedCpFeature.getProperties());

                }
                // make sure all the nested compositional part features are there
                assertEquals(nestedCpIds.containsAll(Arrays.asList(cpIds)), true);
            }
            // make sure all the nested geological unit features are there
            assertEquals(nestedGuIds.containsAll(Arrays.asList(guIds)), true);

        }
        mfFeatures.close(mfIterator);
        guFeatures.close(guIterator);
        cpFeatures.close(cpIterator);

        DataAccessRegistry.unregisterAll();
    }

    /**
     * testFeatureChaining() tests one to many relationship, but the many side was on the chaining
     * side ie. geologic unit side (with many composition parts). This is to test that configuring
     * many on the the chained works. We're using composition part -> lithology here.
     * 
     * @throws Exception
     */
    public void testManyOnChainedSide() throws Exception {

        this.loadDataAccesses();

        final String LITHOLOGY = "lithology";
        final int EXPECTED_RESULT_COUNT = 2;
        // get controlled concept features on their own
        MappingFeatureIterator iterator = (MappingFeatureIterator) ccFeatures.iterator();
        int count = 0;
        Map<String, Feature> featureList = new HashMap<String, Feature>();
        try {
            while (iterator.hasNext()) {
                Feature f = iterator.next();
                featureList.put(f.getIdentifier().getID(), f);
                count++;
            }
        } finally {
            ccFeatures.close((Iterator<Feature>) iterator);
        }
        assertEquals(EXPECTED_RESULT_COUNT, count);

        Iterator<Feature> cpIterator = cpFeatures.iterator();
        while (cpIterator.hasNext()) {
            Feature cpFeature = (Feature) cpIterator.next();
            Collection<Property> lithologies = cpFeature.getProperties(LITHOLOGY);
            if (cpFeature.getIdentifier().toString().equals("cp.167775491936278812")) {
                // see ControlledConcept.properties file:
                // _=NAME:String,COMPOSITION_ID:String
                // 1=name_a|cp.167775491936278812
                // 1=name_b|cp.167775491936278812
                // 1=name_c|cp.167775491936278812
                // 2=name_2|cp.167775491936278812
                assertEquals(((Collection) lithologies).size(), EXPECTED_RESULT_COUNT);
                Collection<String> lithologyIds = new ArrayList<String>();
                for (Property lithologyProperty : lithologies) {
                    Feature nestedFeature = (Feature) ((Collection) lithologyProperty.getValue())
                            .iterator().next();
                    String fId = nestedFeature.getIdentifier().getID();
                    lithologyIds.add(fId);
                    Feature lithology = featureList.get(fId);
                    assertEquals(lithology.getProperties(), nestedFeature.getProperties());
                }
                assertEquals(featureList.keySet().containsAll(lithologyIds), true);
            } else {
                assertEquals(lithologies.isEmpty(), true);
            }
        }
        DataAccessRegistry.unregisterAll();
    }

    /**
     * Test nesting multiple multi valued properties. Both exposure color and outcrop character are
     * multi valued. By making sure that both are nested inside geological unit feature, it's
     * verified that nesting multiple multi valued properties is possible.
     * 
     * @throws Exception
     */
    public void testMultipleMultiValuedProperties() throws Exception {
        this.loadDataAccesses();
        Iterator guIterator = guFeatures.iterator();

        Feature guFeature;
        final String EXPOSURE_COLOR = "exposureColor";
        final String OUTCROP_CHARACTER = "outcropCharacter";
        while (guIterator.hasNext()) {
            guFeature = (Feature) guIterator.next();
            String guId = guFeature.getIdentifier().toString();
            ArrayList realValues = new ArrayList();

            /**
             * Test exposure color
             */
            Collection<Property> nestedTermValues = (Collection<Property>) guFeature
                    .getProperties(EXPOSURE_COLOR);
            // get exposure color property values from geological unit feature
            for (Property property : nestedTermValues) {
                Object value = property.getValue();
                assertNotNull(value);
                assertEquals(value instanceof Collection, true);
                assertEquals(((Collection) value).size(), 1);

                Feature feature = (Feature) ((Collection) value).iterator().next();
                for (Property nestedProperty : feature.getProperties("value")) {
                    realValues.add(nestedProperty.getValue());
                }
            }

            // compares the values from the property file
            String[] values = this.guToExposureColorMap.get(guId).split(";");
            assertEquals(realValues.size(), values.length);
            assertEquals(realValues.containsAll(Arrays.asList(values)), true);

            /**
             * Test outcrop character
             */
            nestedTermValues = (Collection<Property>) guFeature.getProperties(OUTCROP_CHARACTER);
            realValues.clear();
            // get nested outcrop character values from geological unit feature
            for (Property property : nestedTermValues) {
                Object value = property.getValue();
                assertNotNull(value);
                assertEquals(value instanceof Collection, true);
                assertEquals(((Collection) value).size(), 1);

                Feature feature = (Feature) ((Collection) value).iterator().next();
                for (Property nestedProperty : feature.getProperties("value")) {
                    realValues.add(nestedProperty.getValue());
                }
            }
            // compare with values from property file
            values = this.guToOutcropCharacterMap.get(guId).split(";");
            assertEquals(realValues.size(), values.length);
            assertEquals(realValues.containsAll(Arrays.asList(values)), true);
        }
        DataAccessRegistry.unregisterAll();
        guFeatures.close(guIterator);
    }

    /**
     * Test mapping multi-valued simple properties still works.
     * 
     * @throws Exception
     */
    public void testMultiValuedSimpleProperties() throws Exception {
        // Controlled Concept can have many gml:name
        Map dsParams = new HashMap();
        URL url = getClass().getResource(schemaBase + "ControlledConcept.xml");
        assertNotNull(url);

        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        DataAccess<FeatureType, Feature> dataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(dataAccess);

        FeatureType featureType = dataAccess.getSchema(CONTROLLED_CONCEPT);
        assertNotNull(featureType);

        FeatureSource fSource = (FeatureSource) dataAccess.getFeatureSource(CONTROLLED_CONCEPT);
        FeatureCollection features = (FeatureCollection) fSource.getFeatures();

        final int EXPECTED_RESULTS = 2;
        assertEquals(getCount(features), EXPECTED_RESULTS);

        Iterator<Feature> iterator = features.iterator();
        while (iterator.hasNext()) {
            Feature next = iterator.next();
            Collection<Property> names = next.getProperties("name");
            if (next.getIdentifier().toString().equals("1")) {
                // see ControlledConcept.properties where id = 1
                assertEquals(names.size(), 3);
            } else {
                // see ControlledConcept.properties where id = 2
                assertEquals(names.size(), 1);
            }
        }

        dataAccess.dispose();
    }

    /**
     * Test filtering attributes on nested features.
     * 
     * @throws Exception
     */
    public void testFilters() throws Exception {
        this.loadDataAccesses();
        // make sure filter query can be made on MappedFeature based on GU properties
        //
        // <ogc:Filter>
        // <ogc:PropertyIsEqualTo>
        // <ogc:Function name="contains_text">
        // <ogc:PropertyName>
        // gsml:specification/gsml:GeologicUnit/gml:description
        // </ogc:PropertyName>
        // <ogc:Literal>Olivine basalt, tuff, microgabbro, minor sedimentary rocks</ogc:Literal>
        // </ogc:Function>
        // <ogc:Literal>1</ogc:Literal>
        // </ogc:PropertyIsEqualTo>
        // </ogc:Filter>

        // <ogc:PropertyName>
        // gsml:specification/gsml:GeologicUnit/gml:description
        Expression property = ff.property("gsml:specification/gsml:GeologicUnit/gml:description");
        // </ogc:PropertyName>
        // <ogc:Literal>Olivine basalt, tuff, microgabbro, minor sedimentary rocks</ogc:Literal>
        Expression string = ff
                .literal("Olivine basalt, tuff, microgabbro, minor sedimentary rocks");
        // <ogc:Function name="contains_text">
        Expression function = ff.function(CONTAINS_TEXT, property, string);

        // <ogc:PropertyIsEqualTo>
        // <ogc:Literal>1</ogc:Literal>
        // </ogc:PropertyIsEqualTo>
        Filter filter = ff.equals(function, ff.literal(1));

        FeatureCollection<FeatureType, Feature> filteredResults = mfSource.getFeatures(filter);

        assertEquals(getCount(filteredResults), 3);

        /**
         * Test filtering on multi valued properties
         */
        FeatureSource<FeatureType, Feature> guSource = DataAccessRegistry
                .getFeatureSource(GEOLOGIC_UNIT);
        // composition part is a multi valued property
        // we're testing that we can get a geologic unit which has a composition part with a
        // significant proportion value
        property = ff
                .property("gsml:composition/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value");
        string = ff.literal("significant");
        function = ff.function(CONTAINS_TEXT, property, string);
        filter = ff.equals(function, ff.literal(1));
        filteredResults = guSource.getFeatures(filter);
        assertEquals(getCount(filteredResults), 3);

        DataAccessRegistry.unregisterAll();
    }

    /**
     * Test nesting features of a complex type with simple content. Previously didn't get encoded.
     * Also making sure that a feature type can have multiple FEATURE_LINK to be referred by
     * different types.
     * 
     * @throws Exception
     */
    public void testComplexTypeWithSimpleContent() throws Exception {
        Map dsParams = new HashMap();
        URL url = getClass().getResource(schemaBase + "ComplexTypeWithSimpleContent.xml");
        assertNotNull(url);

        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        DataAccess<FeatureType, Feature> dataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(dataAccess);

        // <AttributeMapping>
        // <targetAttribute>FEATURE_LINK[1]</targetAttribute>
        // <sourceExpression>
        // <OCQL>LINK_ONE</OCQL>
        // </sourceExpression>
        // </AttributeMapping>

        Name typeName = Types.typeName("http://example.com", "FirstParentFeature");
        FeatureType featureType = dataAccess.getSchema(typeName);
        assertNotNull(featureType);

        FeatureSource fSource = (FeatureSource) dataAccess.getFeatureSource(typeName);
        FeatureCollection features = (FeatureCollection) fSource.getFeatures();

        final int EXPECTED_RESULTS = 2;
        assertEquals(getCount(features), EXPECTED_RESULTS);

        Iterator<Feature> iterator = features.iterator();
        while (iterator.hasNext()) {
            Feature next = iterator.next();
            Collection<Property> children = next.getProperties("nestedFeature");
            if (next.getIdentifier().toString().equals("1")) {
                // _=STRING:String,LINK_ONE:String,LINK_TWO:String
                // 1=string_one|1|2
                // 2=string_two|1|2
                // 3=string_three|NULL|2
                assertEquals(children.size(), 2);
            } else {
                assertEquals(children.size(), 0);
            }
            for (Property nestedFeature : children) {
                Object value = nestedFeature.getValue();
                assertNotNull(value);
                value = ((Collection) value).iterator().next();
                assertEquals(value instanceof FeatureImpl, true);
                Feature feature = (Feature) value;
                assertNotNull(feature.getProperty("someAttribute").getValue());
            }
        }

        // <AttributeMapping>
        // <targetAttribute>FEATURE_LINK[2]</targetAttribute>
        // <sourceExpression>
        // <OCQL>LINK_TWO</OCQL>
        // </sourceExpression>
        // </AttributeMapping>
        typeName = Types.typeName("http://example.com", "SecondParentFeature");
        featureType = dataAccess.getSchema(typeName);
        assertNotNull(featureType);

        fSource = (FeatureSource) dataAccess.getFeatureSource(typeName);
        features = (FeatureCollection) fSource.getFeatures();

        assertEquals(getCount(features), EXPECTED_RESULTS);

        iterator = features.iterator();
        while (iterator.hasNext()) {
            Feature next = iterator.next();
            Collection<Property> children = next.getProperties("nestedFeature");
            if (next.getIdentifier().toString().equals("2")) {
                // _=STRING:String,LINK_ONE:String,LINK_TWO:String
                // 1=string_one|1|2
                // 2=string_two|1|2
                // 3=string_three|NULL|2
                assertEquals(children.size(), 3);
            } else {
                assertEquals(children.size(), 0);
            }
            for (Property nestedFeature : children) {
                Object value = nestedFeature.getValue();
                assertNotNull(value);
                value = ((Collection) value).iterator().next();
                assertEquals(value instanceof FeatureImpl, true);
                Feature feature = (Feature) value;
                assertNotNull(feature.getProperty("someAttribute").getValue());
            }
        }

        dataAccess.dispose();
    }

    /**
     * Test chaining multi-valued by reference (xlink:href). It should result with multiple
     * attributes with no nested attributes, but only client property with xlink:href.
     * 
     * @throws Exception
     */
    public void testMultiValuedPropertiesByRef() throws Exception {
        final String MF_PREFIX = "urn:cgi:feature:MappedFeature:";
        final String OCCURENCE = "occurence";
        final Map<String, String> guToOccurenceMap = new HashMap<String, String>() {
            {
                put("gu.25699", "mf1");
                put("gu.25678", "mf2;mf3");
                put("gu.25682", "mf4");
            }
        };

        this.loadDataAccesses();

        ArrayList<String> processedFeatureIds = new ArrayList<String>();

        Iterator guIterator = guFeatures.iterator();
        while (guIterator.hasNext()) {
            Feature guFeature = (Feature) guIterator.next();
            String guId = guFeature.getIdentifier().toString();
            String[] mfIds = guToOccurenceMap.get(guId).split(";");
            Collection<Property> properties = guFeature.getProperties(OCCURENCE);

            assertEquals(properties.size(), mfIds.length);

            int propertyIndex = 0;
            for (Property property : properties) {
                Object clientProps = property.getUserData().get(Attributes.class);
                assertNotNull(clientProps);
                assertEquals(clientProps instanceof HashMap, true);
                Object hrefValue = ((Map) clientProps).get(MappingFeatureIterator.XLINK_HREF_NAME);

                // ensure the right href:xlink is there
                assertEquals(hrefValue, MF_PREFIX + mfIds[propertyIndex]);

                // ensure no attributes would be encoded
                assertEquals(((Collection) property.getValue()).isEmpty(), true);
                propertyIndex++;
            }
            processedFeatureIds.add(guId);
        }

        assertEquals(processedFeatureIds.size(), guToOccurenceMap.size());
        assertEquals(processedFeatureIds.containsAll(guToOccurenceMap.keySet()), true);

        // clean ups
        guFeatures.close(guIterator);
        DataAccessRegistry.unregisterAll();
    }

    /**
     * Load all the data accesses.
     * 
     * @return
     * @throws Exception
     */
    private void loadDataAccesses() throws Exception {
        /**
         * Load mapped feature data access
         */
        Map dsParams = new HashMap();
        URL url = getClass().getResource(schemaBase + "MappedFeaturePropertyfile.xml");
        assertNotNull(url);

        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        DataAccess<FeatureType, Feature> mfDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(mfDataAccess);

        FeatureType mappedFeatureType = mfDataAccess.getSchema(MAPPED_FEATURE);
        assertNotNull(mappedFeatureType);

        mfSource = (FeatureSource) mfDataAccess.getFeatureSource(MAPPED_FEATURE);
        mfFeatures = (FeatureCollection) mfSource.getFeatures();

        /**
         * Load geologic unit data access
         */
        url = getClass().getResource(schemaBase + "GeologicUnit.xml");
        assertNotNull(url);

        dsParams.put("url", url.toExternalForm());
        DataAccess<FeatureType, Feature> guDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(guDataAccess);

        FeatureType guType = guDataAccess.getSchema(GEOLOGIC_UNIT);
        assertNotNull(guType);

        FeatureSource<FeatureType, Feature> guSource = (FeatureSource<FeatureType, Feature>) guDataAccess
                .getFeatureSource(GEOLOGIC_UNIT);
        guFeatures = (FeatureCollection) guSource.getFeatures();

        /**
         * Non-feature types that are included in geologicUnit.xml should be loaded when geologic
         * unit data access is created
         */
        // Composition Part
        cpFeatures = DataAccessRegistry.getFeatureSource(COMPOSITION_PART).getFeatures();
        // CGI TermValue
        FeatureCollection<FeatureType, Feature> cgiFeatures = DataAccessRegistry.getFeatureSource(
                CGI_TERM_VALUE).getFeatures();
        // ControlledConcept
        ccFeatures = DataAccessRegistry.getFeatureSource(CONTROLLED_CONCEPT).getFeatures();

        int EXPECTED_RESULT_COUNT = 4;

        int resultCount = getCount(mfFeatures);
        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        EXPECTED_RESULT_COUNT = 3;
        resultCount = getCount(guFeatures);
        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        resultCount = getCount(cpFeatures);
        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        EXPECTED_RESULT_COUNT = 8;
        resultCount = getCount(cgiFeatures);
        assertEquals(EXPECTED_RESULT_COUNT, resultCount);
    }

    protected static int getCount(FeatureCollection<FeatureType, Feature> features) {
        MappingFeatureIterator iterator = (MappingFeatureIterator) features.iterator();
        int count = 0;
        try {
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }
        } finally {
            features.close((Iterator<Feature>) iterator);
        }
        return count;
    }

    /**
     * This is a mock contains_text function which is normally stored in the database. But we are
     * using properties files for our test, so this needs to be written.
     * 
     * @author ang05a
     */
    public static class ContainsTextFunctionExpression extends FunctionExpressionImpl {

        public ContainsTextFunctionExpression() {
            super("contains_text");
        }

        public int getArgCount() {
            return 2;
        }

        public Object evaluate(final Object obj) {
            assert obj instanceof Feature;
            final Feature feature = (Feature) obj;

            final List params = this.getParameters();
            assertEquals(params.size(), getArgCount());

            final Object arg1 = params.get(0);
            assertNotNull(arg1);
            final Object arg2 = params.get(1);
            assertNotNull(arg2);
            assertEquals(arg1 instanceof Expression, true);
            assertEquals(arg2 instanceof Expression, true);

            final Object val1 = ((Expression) arg1).evaluate(feature);

            if (val1 == null) {
                return false;
            }

            final Object val2 = ((Expression) arg2).evaluate(feature);

            if (val2 == null) {
                return false;
            }
            return val1.toString().contains(val2.toString());
        }
    }
}
