/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le D�veloppement
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
 *     FRANCE: Surveillance de l'Environnement Assist�e par Satellite
 *             Institut de Recherche pour le D�veloppement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.ct;

// J2SE dependencies
import java.text.*;
import java.awt.geom.*;
import javax.vecmath.*;

// Geotools dependencies
import org.geotools.pt.*;
import org.geotools.cs.*;
import org.geotools.ct.*;
import org.geotools.units.Unit;
import org.geotools.resources.XMath;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test the following transformation classes:
 *
 * <ul>
 *   <li>{@link CoordinateTransformation}</li>
 *   <li>{@link GeocentricTransform}</li>
 *   <li>{@link Ellipsoid}</li>
 * </ul>
 *
 * @version $Id: GeocentricTransformTest.java,v 1.3 2002/07/12 14:37:05 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class GeocentricTransformTest extends TransformationTest {
    /**
     * Returns the test suite.
     */
     public static Test suite() {
         return new TestSuite(GeocentricTransformTest.class);
     }
    
    /**
     * Constructs a test case with the given name.
     */
    public GeocentricTransformTest(final String name) {
        super(name);
    }
    
    /**
     * Test the orthodromic distance computed by {@link Ellipsoid}. There is actually two
     * algorithms used: one for the ellipsoidal model, and a simpler one for spherical model.
     * We test the ellipsoidal model using know values of nautical mile at different latitude.
     * Then, we test the spherical model with random values. If JDK 1.4 assertion is enabled,
     * the spherical model will compare its result with the ellipsoidal one.
     *
     * Note about nautical mile:
     *
     *    "Le mille marin �tait, en principe, la longueur de la minute sexag�simale du m�ridien
     *     � la latitude de 45�. Cette longueur d�pendait donc des valeurs adopt�es pour le rayon
     *     �quatorial de la terre et son aplatissement. En France, le d�cret du 3 mai 1961 sur les
     *     unit�s de mesure, fixe � 1852 m�tres la longueur du mille marin qui est �galement la
     *     valeur adopt�e pour le mille marin international."
     *
     *                                   Source: Office de la langue fran�aise, 1996
     *                                           http://www.granddictionnaire.com
     */
    public void testEllipsoid() throws FactoryException {
        final Ellipsoid  e = Ellipsoid.WGS84;
        final double    hm = 0.5/60; // Half of a minute of angle, in degrees.
        /*
         * Test the ellipsoidal model.
         */
        assertEquals("Nautical mile at equator",    1842.78, e.orthodromicDistance(0, 00-hm, 0, 00+hm), 0.2);
        assertEquals("Nautical mile at North pole", 1861.67, e.orthodromicDistance(0, 90-2*hm, 0,  90), 0.2);
        assertEquals("Nautical mile at South pole", 1861.67, e.orthodromicDistance(0, 2*hm-90, 0, -90), 0.2);
        assertEquals("International nautical mile", 1852.00, e.orthodromicDistance(0, 45-hm, 0, 45+hm), 0.2);
        for (double i=0.01; i<180; i+=1) {
            final double base = 180*random.nextDouble()-90;
            assertEquals(i+"� rotation", e.getSemiMajorAxis()*Math.toRadians(i),
                                         e.orthodromicDistance(base, 0, base+i, 0), 0.2);
        }
        /*
         * Test the spherical model. The factory method should create
         * a specialized class, which is not the usual Ellipsoid class.
         */
        final double radius = e.getSemiMajorAxis();
        final double circumference = (radius*1.00000001) * (2*Math.PI);
        final Ellipsoid s = csFactory.createEllipsoid("Sphere", radius, radius, e.getAxisUnit());
        assertTrue("Spheroid class", !Ellipsoid.class.equals(s.getClass()));
        for (double i=0; i<=180; i+=1) {
            final double base = 360*random.nextDouble()-180;
            assertEquals(i+"� rotation", s.getSemiMajorAxis()*Math.toRadians(i),
                                         s.orthodromicDistance(base, 0, base+i, 0), 0.001);
        }
        for (double i=-90; i<=+90; i+=1) {
            final double meridian = 360*random.nextDouble()-180;
            assertEquals(i+"� rotation", s.getSemiMajorAxis()*Math.toRadians(Math.abs(i)),
                                         s.orthodromicDistance(meridian, 0, meridian, i), 0.001);
        }
        for (int i=0; i<100; i++) {
            final double y1 =  -90 + 180*random.nextDouble();
            final double y2 =  -90 + 180*random.nextDouble();
            final double x1 = -180 + 360*random.nextDouble();
            final double x2 = -180 + 360*random.nextDouble();
            final double distance = s.orthodromicDistance(x1, y1, x2, y2);
            assertTrue("Range of legal values", distance>=0 && distance<=circumference);
        }
    }

    /**
     * Tests the {@link GeocentricTransform} class.
     */
    public void testGeocentricTransform() throws TransformException {
        /*
         * Gets the math transform from WGS84 to a geocentric transform.
         */
        final Ellipsoid       ellipsoid =                  Ellipsoid.WGS84;
        final CoordinateSystem sourceCS =   CompoundCoordinateSystem.WGS84;
        final CoordinateSystem targetCS = GeocentricCoordinateSystem.DEFAULT;
        final CoordinateTransformation transformation;
        final MathTransform            transform;
        final int                      dimension;
        transformation = trFactory.createFromCoordinateSystems(sourceCS, targetCS);
        transform      = transformation.getMathTransform();
        dimension      = transform.getDimSource();
        assertEquals("Source dimension", 3, dimension);
        assertEquals("Target dimension", 3, transform.getDimTarget());
        assertSame("Inverse transform", transform, transform.inverse().inverse());
        assertInterfaced(transform);
        /*
         * Construct an array of 850 random points. The first 8 points
         * are initialized to know values. Other points are left random.
         */
        final double   cartesianDistance[] = new double[4];
        final double orthodromicDistance[] = new double[4];
        final double[]              array0 = new double[2550]; // 255 is divisible by 3.
        for (int i=0; i<array0.length; i++) {
            final int range;
            switch (i % 3) {
                case 0:  range =   360; break; // Longitude
                case 1:  range =   180; break; // Latitidue
                case 2:  range = 10000; break; // Altitude
                default: range =     0; break; // Should not happen
            }
            array0[i] = range*random.nextDouble()-(range/2);
        }
        array0[0]=35.0; array0[1]=24.0; array0[2]=8000; // 24�N 35�E 8km
        array0[3]=34.8; array0[4]=24.7; array0[5]=5000; // ... about 80 km away
        cartesianDistance  [0] = 80284.00;
        orthodromicDistance[0] = 80302.99; // Not really exact.

        array0[6]=  0; array0[ 7]=0.0; array0[ 8]=0;
        array0[9]=180; array0[10]=0.0; array0[11]=0; // Antipodes; distance should be 2*6378.137 km
        cartesianDistance  [1] = ellipsoid.getSemiMajorAxis() * 2;
        orthodromicDistance[1] = ellipsoid.getSemiMajorAxis() * Math.PI;

        array0[12]=  0; array0[13]=-90; array0[14]=0;
        array0[15]=180; array0[16]=+90; array0[17]=0; // Antipodes; distance should be 2*6356.752 km
        cartesianDistance  [2] = ellipsoid.getSemiMinorAxis() * 2;
        orthodromicDistance[2] = 20003931.46;

        array0[18]= 95; array0[19]=-38; array0[20]=0;
        array0[21]=-85; array0[22]=+38; array0[23]=0; // Antipodes
        cartesianDistance  [3] = 12740147.19;
        orthodromicDistance[3] = 20003867.86;
        /*
         * Transform all points, and then inverse transform then. The resulting
         * <code>array2</code> array should be equals to <code>array0</code>
         * except for rounding errors. We tolerate maximal error of 0.1 second
         * in longitude or latitude and 1 cm in height.
         */
        final double[] array1 = new double[array0.length];
        final double[] array2 = new double[array0.length];
        transform          .transform(array0, 0, array1, 0, array0.length/dimension);
        transform.inverse().transform(array1, 0, array2, 0, array1.length/dimension);
        assertPointsEqual("transform(Geographic --> Geocentric --> Geographic)", array0, array2,
                          new double[] {0.1/3600, 0.1/3600, 0.01});
        /*
         * Compare the distances between "special" points with expected distances.
         * This test the ellipsoid orthodromic distance computation as well.
         * We require a precision of 10 centimeters.
         */
        for (int i=0; i<array0.length/6; i++) {
            final int base = i*6;
            final Point3d  pt1 = new Point3d(array1[base+0], array1[base+1], array1[base+2]);
            final Point3d  pt2 = new Point3d(array1[base+3], array1[base+4], array1[base+5]);
            final double cartesian = pt1.distance(pt2);
            if (i<cartesianDistance.length) {
                assertEquals("Cartesian distance["+i+']', cartesianDistance[i], cartesian, 0.1);
            }
            /*
             * Compare with orthodromic distance.  Distance is computed using an ellipsoid
             * at the maximal altitude (i.e. the length of semi-major axis is increased to
             * fit the maximal altitude).
             */
            try {
                final double altitude = Math.max(array0[base+2], array0[base+5]);
                final Ellipsoid ellip = Ellipsoid.createFlattenedSphere("Temporary",
                                        ellipsoid.getSemiMajorAxis()+altitude,
                                        ellipsoid.getInverseFlattening(),
                                        ellipsoid.getAxisUnit());
                double orthodromic = ellip.orthodromicDistance(array0[base+0], array0[base+1],
                                                               array0[base+3], array0[base+4]);
                orthodromic = XMath.hypot(orthodromic, array0[base+2]-array0[base+5]);
                if (i<orthodromicDistance.length) {
                    assertEquals("Orthodromic distance["+i+']', orthodromicDistance[i], orthodromic, 0.1);
                }
                assertTrue("Distance consistency["+i+']', cartesian <= orthodromic);
            } catch (ArithmeticException exception) {
                // Orthodromic distance computation didn't converge. Ignore...
            }
        }
    }
}
