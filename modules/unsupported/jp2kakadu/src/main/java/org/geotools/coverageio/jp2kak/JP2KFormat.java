/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverageio.jp2kak;

import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageReaderSpi;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.ImageReaderSpi;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;

/**
 * An implementation of {@link Format} for the JP2K format.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * @since 2.5.x
 */
@SuppressWarnings("deprecation")
public final class JP2KFormat extends AbstractGridFormat implements Format {
    
	 /** The inner {@code ImageReaderSpi} */
    private final ImageReaderSpi spi;
    
	/**
     * Logger.
     */
    private final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.coverageio.jp2kak");

    /**
     * The {@code String} representing the parameter to customize multithreading
     * use
     */
    private static final String USE_MT = "USE_MULTITHREADING";

    /**
     * This {@link GeneralParameterValue} can be provided to the
     * {@link GridCoverageReader}s through the
     * {@link GridCoverageReader#read(GeneralParameterValue[])} method in order
     * to specify to use multithreading when leveraging on a JAI ImageRead
     * operation. This will be achieved with the use of the ImageReadMT
     * operation of the ImageIO-Ext.
     */
    public static final DefaultParameterDescriptor<Boolean> USE_MULTITHREADING = new DefaultParameterDescriptor<Boolean>(
            USE_MT, Boolean.class,
            new Boolean[] { Boolean.TRUE, Boolean.FALSE }, Boolean.FALSE);

    /** The {@code String} representing the parameter to customize tile sizes */
    private static final String SUGGESTED_TILESIZE = "SUGGESTED_TILE_SIZE";

    /**
     * This {@link GeneralParameterValue} can be provided to the
     * {@link GridCoverageReader}s through the
     * {@link GridCoverageReader#read(GeneralParameterValue[])} method in order
     * to specify the suggested size of tiles to avoid long time reading
     * occurring with JAI ImageRead on striped images. (Images with tiles Nx1)
     * Value should be a String in the form of "W,H" (without quotes) where W is
     * a number representing the suggested tileWidth and H is a number
     * representing the suggested tileHeight.
     */
    public static final DefaultParameterDescriptor<String> SUGGESTED_TILE_SIZE = new DefaultParameterDescriptor<String>(
            SUGGESTED_TILESIZE, String.class, null, "512,512");

    public static final String TILE_SIZE_SEPARATOR = ",";

    /** Control the transparency of the input coverages. */
    public static final ParameterDescriptor<Color> INPUT_TRANSPARENT_COLOR = new DefaultParameterDescriptor<Color>(
            "InputTransparentColor", Color.class, null, null);

//    /** Control the threading behavior for this plugin. This parameter contains the number of thread that we should use to load the granules. Default value is 0 which means not additional thread, max value is 8.*/
//    public static final ParameterDescriptor<Boolean> ALLOW_MULTITHREADING = new DefaultParameterDescriptor<Boolean>(
//            "AllowMultithreading", Boolean.class, new Boolean[]{Boolean.TRUE,Boolean.FALSE}, Boolean.FALSE);
    
    /** Control the background values for the output coverage */
    public static final ParameterDescriptor<double[]> BACKGROUND_VALUES = new DefaultParameterDescriptor<double[]>(
            "BackgroundValues", double[].class, null, null);

    
    /**
     * Creates an instance and sets the metadata.
     */
    public JP2KFormat() {
    	this.spi = new JP2KKakaduImageReaderSpi();
        setInfo();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Creating a new JP2KFormat.");
        }
    }

    /**
     * Sets the metadata information.
     */
    protected void setInfo() {
        HashMap<String,String> info = new HashMap<String,String>();
        info.put("name", "JP2K (Direct Kakadu) ");
        info.put("description", "JP2K (Direct Kakadu) Coverage Format");
        info.put("vendor", "Geotools");
        info.put("docURL", ""); // TODO: set something
        info.put("version", "1.0");
        mInfo = info;

        // writing parameters
        writeParameters = null;
        readParameters = new ParameterGroup(new DefaultParameterDescriptorGroup(mInfo,
                new GeneralParameterDescriptor[]{
        		READ_GRIDGEOMETRY2D,
        		INPUT_TRANSPARENT_COLOR,
//                OUTPUT_TRANSPARENT_COLOR,
                USE_JAI_IMAGEREAD,
                USE_MULTITHREADING,
                BACKGROUND_VALUES,
                SUGGESTED_TILE_SIZE,
//                ALLOW_MULTITHREADING
                }));
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object, Hints)
     */
    public GridCoverageReader getReader(Object source, Hints hints) {
        try {
            return new JP2KReader(source, hints);
        } catch (MismatchedDimensionException e) {
        	if (LOGGER.isLoggable(Level.WARNING))
        		LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
        	return null;
        } catch (DataSourceException e) {
        	if (LOGGER.isLoggable(Level.WARNING))
        		LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
        	return null;
        } catch (IOException e) {
        	if (LOGGER.isLoggable(Level.WARNING))
        		LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
        	return null;
		} 
    }
    
    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object)
     */
    public GridCoverageReader getReader( Object source ) {
        return getReader(source, null);
    }
    
    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#createWriter(java.lang.Object
     *      destination)
     * 
     * Actually, the plugin does not support write capabilities. The method
     * throws an {@code UnsupportedOperationException}.
     */
    public GridCoverageWriter getWriter(Object destination) {
        throw new UnsupportedOperationException(
                "This plugin does not support writing at this time.");
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#getDefaultImageIOWriteParameters
     * 
     * Actually, the plugin does not support write capabilities. The method
     * throws an {@code UnsupportedOperationException}.
     */
    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        throw new UnsupportedOperationException(
                "This plugin does not support writing parameters");
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#createWriter(java.lang.Object
     *      destination,Hints hints)
     * 
     * Actually, the plugin does not support write capabilities. The method
     * throws an {@code UnsupportedOperationException}.
     */
    public GridCoverageWriter getWriter(Object destination, Hints hints) {
        throw new UnsupportedOperationException(
                "This plugin does not support writing at this time.");
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(java.lang.Object input)
     */
    @Override
    public boolean accepts(Object input) {
        try {
            return spi.canDecodeInput(input);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            }
            return false;
        }
    }
}