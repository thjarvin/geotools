/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
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
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.renderer.geom;

// J2SE dependencies
import java.util.List;
import java.util.ArrayList;

// JTS dependencies
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.sfs.SFSPoint;
import com.vividsolutions.jts.geom.sfs.SFSPolygon;
import com.vividsolutions.jts.geom.sfs.SFSGeometry;
import com.vividsolutions.jts.geom.sfs.SFSLineString;
import com.vividsolutions.jts.geom.sfs.SFSGeometryCollection;

// Geotools dependencies
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.renderer.array.JTSArray;


/**
 * An {@link Isoline} backed by one or many JTS {@link Geometry} objects.
 *
 * @version $Id: JTSIsoline.java,v 1.1 2003/02/11 16:02:31 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class JTSIsoline extends Isoline {
    /**
     * Numéro de version pour compatibilité avec des
     * bathymétries enregistrées sous d'anciennes versions.
     */
    private static final long serialVersionUID = 1313504311244991561L;

    /**
     * Construct an initialy empty isoline. Polygon may be added using one
     * of <code>add(...)</code> methods.
     *
     * @param value The value for this isoline. In the case
     *        of isobath, the value is the altitude.
     * @param coordinateSystem The coordinate system to use for all
     *        points in this isoline, or <code>null</code> if unknow.
     *
     * @see #add(SFSPoint)
     * @see #add(SFSLineString)
     * @see #add(SFSPolygon)
     * @see #add(SFSGeometry)
     * @see #add(SFSGeometryCollection)
     */
    public JTSIsoline(final float value, final CoordinateSystem cs) {
        super(value, cs);
    }

    /**
     * Returns the coordinate system for the specified JTS geometry.
     *
     * @task TODO: We should construct the coordinate system from SRID using
     *             {@link CoordinateSystemFactory}.
     */
    private CoordinateSystem getCoordinateSystem(final SFSGeometry geometry) {
        final int id = geometry.getSRID();
        // TODO: construct CS here.
        return getCoordinateSystem();
    }

    /**
     * Add the specified point to this isoline. This method should rarely be
     * used, since isoline are not designed for handling individual points.
     *
     * @param  geometry The point to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     */
    public void add(final SFSPoint geometry) throws TransformException {
        final Coordinate[] coords;
        if (geometry instanceof Point) {
            coords = ((Point) geometry).getCoordinates();
        } else {
            coords = new Coordinate[] {geometry.getCoordinate()};
        }
        add(new Polygon(new JTSArray(coords), getCoordinateSystem(geometry)));
    }

    /**
     * Add the specified line string to this isoline.
     *
     * @param  geometry The line string to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     */
    public void add(final SFSLineString geometry) throws TransformException {
        add(geometry, InteriorType.ELEVATION);
    }

    /**
     * Add the specified line string to this isoline. The shape will be closed using the
     * specified type (usually {@link InteriorType#ELEVATION}, except for holes in which
     * case it is {@link InteriorType#DEPRESSION}).
     *
     * @param  geometry The line string to add.
     * @param  type The type ({@link InteriorType#ELEVATION} or {@link InteriorType#DEPRESSION}).
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     */
    private void add(final SFSLineString geometry, final InteriorType type)
            throws TransformException
    {
        final Coordinate[] coords;
        if (geometry instanceof LineString) {
            coords = ((LineString) geometry).getCoordinates();
        } else {
            coords = new Coordinate[geometry.getNumPoints()];
            for (int i=0; i<coords.length; i++) {
                coords[i] = geometry.getCoordinateN(i);
            }
        }
        final Polygon polygon = new Polygon(new JTSArray(coords), getCoordinateSystem(geometry));
        if (geometry.isRing()) {
            polygon.close(type);
        }
        add(polygon);
    }

    /**
     * Add the specified polygon to this isoline.
     *
     * @param  geometry The polygon to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     */
    public void add(final SFSPolygon geometry) throws TransformException {
        add(geometry.getExteriorRing());
        final int n = geometry.getNumInteriorRing();
        for (int i=1; i<n; i++) {
            add(geometry.getInteriorRingN(i), InteriorType.DEPRESSION);
        }
    }

    /**
     * Add the specified geometry collection to this isoline.
     *
     * @param  geometry The geometry collection to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     */
    public void add(final SFSGeometryCollection geometry) throws TransformException {
        final int n = geometry.getNumGeometries();
        for (int i=0; i<n; i++) {
            add(geometry.getGeometryN(i));
        }
    }

    /**
     * Add the specified geometry to this isoline. The geometry must be one of the following
     * classes: {@link SFSPoint}, {@link SFSLineString}, {@link SFSPolygon} or
     * {@link SFSGeometryCollection}.
     *
     * @param  geometry The geometry to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     * @throws IllegalArgumentException if the geometry is not a a valid class.
     */
    public void add(final SFSGeometry geometry) throws TransformException, IllegalArgumentException
    {
        if (geometry instanceof SFSPoint) {
            add((SFSPoint) geometry);
            return;
        }
        if (geometry instanceof SFSLineString) {
            add((SFSLineString) geometry);
            return;
        }
        if (geometry instanceof SFSPolygon) {
            add((SFSPolygon) geometry);
            return;
        }
        if (geometry instanceof SFSGeometryCollection) {
            add((SFSGeometryCollection) geometry);
            return;
        }
        throw new IllegalArgumentException(Utilities.getShortClassName(geometry));
    }
}
