/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
// start source
package org.geotools.demo;

import java.io.File;

import javax.swing.JOptionPane;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;

public class StyleLab {

    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

    /**
     * Prompts the user for a shapefile (unless a filename is provided
     * on the command line; then creates an appropriate simple {@code Style}
     * and displays the shapefile using a {@code JMapFrame}.
     * 
     * @param args shapefile name; if not provided the user will be prompted
     *        for a file
     */
    public static void main(String[] args) throws Exception {
        File file = promptShapeFile(args);

        if (file == null) {
            return;
        }

        ShapefileDataStore shapefile = new ShapefileDataStore(file.toURI().toURL());
        String typeName = shapefile.getTypeNames()[0];
        FeatureSource featureSource = shapefile.getFeatureSource();
        FeatureType schema = featureSource.getSchema();

        // Get the coordinate system from the shapefile and create a 
        // MapContext
        CoordinateReferenceSystem crs = schema.getGeometryDescriptor()
                .getCoordinateReferenceSystem();

        MapContext map = new DefaultMapContext(crs);

        // Create a basic Style to render the features
        Style style = createStyle(file, schema);

        // Add the features and the associated Style object to
        // the MapContext as a new MapLayer
        map.addLayer(featureSource, style);

        // Now display the map
        JMapFrame.showMap(map);
    }
// end main method

// start promptShapeFile
    /**
     * Takes the command line arguments and examines the first argument
     * for an input filename. If no filename was provided, prompts user
     * for a shapefile using a {@code JFileDataStoreChooser} dialog.
     *
     * @param args command line args (only the first is examined)
     *
     * @return a File object for the shapefile or null if none is
     *         selected
     */
    private static File promptShapeFile(String[] args) {
        File file = null;

        // check if the filename was provided on the command line
        if (args.length > 0) {
            file = new File(args[0]);
            if (file.exists()) {
                return file;
            }

            // file didn't exist - see if the user wants to continue
            int rtnVal = JOptionPane.showConfirmDialog(null,
                    "Can't find " + file.getName() + ". Choose another ?",
                    "Input shapefile", JOptionPane.YES_NO_OPTION);
            if (rtnVal != JOptionPane.YES_OPTION) {
                return null;
            }
        }

        // display a data store file chooser dialog for shapefiles
        return JFileDataStoreChooser.showOpenFile("shp", null);
    }
// end promptShapeFile
    
// start createStyle
    private static Style createStyle(File file, FeatureType schema) {
        File sld = toSLDFile(file);
        if (sld.exists()) {
            return createFromSLD(sld);
        }
        Class geomType = schema.getGeometryDescriptor().getType().getBinding();

        if (Polygon.class.isAssignableFrom(geomType)
                || MultiPolygon.class.isAssignableFrom(geomType)) {
            return createPolygonStyle();

        } else if (LineString.class.isAssignableFrom(geomType)
                || MultiLineString.class.isAssignableFrom(geomType)) {
            return createLineStyle();

        } else {
            return createPointStyle();
        }
    }
// end createStyle

// start createFromSLD
    /**
     * Create a Style object from a definition in a SLD document
     *
     * @param sld path and filename of the SLD document
     * @return a new Style instance
     */
    private static Style createFromSLD(File sld) {
        SLDParser stylereader;
        try {
            stylereader = new SLDParser(styleFactory, sld.toURI().toURL());
            Style[] style = stylereader.readXML();
            return style[0];
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            System.exit(0);
        }
        return null;
    }
// end createFromSLD

// start createPolygonStyle
    /**
     * Create a Style to draw polygon features with a thin blue outline and
     * a cyan fill
     */
    private static Style createPolygonStyle() {

        // create a partially opaque outline stroke
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLUE),
                filterFactory.literal(1),
                filterFactory.literal(0.5));

        // create a partial opaque fill
        Fill fill = styleFactory.createFill(
                filterFactory.literal(Color.CYAN),
                filterFactory.literal(0.5));

// mid createPolygonStyle

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, fill, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }
    
// end createPolygonStyle

    /**
     * Create a Style to draw line features as thin blue lines
     */
    private static Style createLineStyle() {
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLUE),
                filterFactory.literal(1));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    /**
     * Create a Style to draw point features as circles with blue outlines
     * and cyan fill
     */
    private static Style createPointStyle() {
        Graphic gr = styleFactory.createDefaultGraphic();

        Mark mark = styleFactory.getCircleMark();

        mark.setStroke(styleFactory.createStroke(
                filterFactory.literal(Color.BLUE), filterFactory.literal(1)));

        mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));

        mark.setSize(filterFactory.literal(3));

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add(mark);

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }
// end createPointStyle

    /** Figure out the URL for the "sld" file */
    public static File toSLDFile(File file)  {
        String filename = file.getAbsolutePath();
        if (filename.endsWith(".shp") || filename.endsWith(".dbf")
                || filename.endsWith(".shx")) {
            filename = filename.substring(0, filename.length() - 4);
            filename += ".sld";
        } else if (filename.endsWith(".SLD") || filename.endsWith(".SLD")
                || filename.endsWith(".SLD")) {
            filename = filename.substring(0, filename.length() - 4);
            filename += ".SLD";
        }
        return new File(filename);
    }
}