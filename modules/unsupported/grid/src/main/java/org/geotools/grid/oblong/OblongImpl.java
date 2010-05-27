/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2010, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.grid.oblong;

import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import org.geotools.geometry.jts.JTSFactoryFinder;

/**
 * Default implementation of {@code Oblong}.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/unsupported/grid/src/main/java/org/geotools/grid/hexagon/HexagonImpl.java $
 * @version $Id: HexagonImpl.java 35566 2010-05-24 13:47:59Z mbedward $
 */
public class OblongImpl implements Oblong {

    private static final GeometryFactory geomFactory = JTSFactoryFinder.getGeometryFactory(null);
    private final Envelope envelope;

    /**
     * Creates a new oblong.
     */
    public OblongImpl(double minX, double minY, double width, double height) {
        if (width <=0 || height <= 0) {
            throw new IllegalArgumentException("width and height must both be positive");
        }
        envelope = new Envelope(minX, minX + width, minY, minY + height);
    }

    /**
     * {@inheritDoc}
     */
    public double getArea() {
        return envelope.getArea();
    }

    /**
     * {@inheritDoc}
     */
    public Envelope getBounds() {
        return new Envelope(envelope);
    }

    /**
     * {@inheritDoc}
     */
    public Coordinate getCenter() {
        return envelope.centre();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Vertex 0 is at the min X and Y coordinate (lower left) with the
     * subsequent vertices being indexed clockwise.
     */
    public Coordinate[] getVertices() {
        Coordinate[] vertices = new Coordinate[4];
        vertices[0] = new Coordinate(envelope.getMinX(), envelope.getMinY());
        vertices[1] = new Coordinate(envelope.getMinX(), envelope.getMaxY());
        vertices[2] = new Coordinate(envelope.getMaxX(), envelope.getMaxY());
        vertices[3] = new Coordinate(envelope.getMaxX(), envelope.getMinY());

        return vertices;
    }

    /**
     * {@inheritDoc}
     */
    public Polygon toPolygon() {
        return (Polygon) geomFactory.toGeometry(envelope);
    }

    /**
     * {@inheritDoc}
     */
    public Polygon toDensePolygon(double maxSpacing) {
        return (Polygon) Densifier.densify(this.toPolygon(), maxSpacing);
    }

}
