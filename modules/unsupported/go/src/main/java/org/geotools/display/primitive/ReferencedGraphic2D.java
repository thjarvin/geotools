/*
 *    GeoTools - An Open Source Java GIS Tookit
 *    http://geotools.org
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
package org.geotools.display.primitive;

import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent; // For javadoc

import org.opengis.display.canvas.Canvas;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import org.geotools.resources.CRSUtilities;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.display.canvas.ReferencedCanvas2D;


/**
 * A graphic implementation with specialized support for two-dimensional CRS. This
 * default implementation uses <cite>Java2D</cite> geometry objects like {@link Shape},
 * which are somewhat lightweight objects. There is no dependency toward AWT toolkit in
 * this class, which means that this class can be used as a basis for SWT renderer as well.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 */
public abstract class ReferencedGraphic2D extends ReferencedGraphic {
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas2D#getDisplayBounds display bounds} changed.
     */
    public static final String DISPLAY_BOUNDS_PROPERTY = "displayBounds";
    
    /**
     * A geometric shape that fully contains the area painted during the last
     * {@linkplain GraphicPrimitive2D#paint rendering}. This shape must be in terms of the
     * {@linkplain ReferencedCanvas2D#getDisplayCRS display CRS}. A {@link XRectangle2D#INFINITY}
     * value means that the whole canvas area may have been affected. This field should never
     * be null.
     */
    private transient Shape displayBounds = XRectangle2D.INFINITY;

    /**
     * {@code true} if this canvas or graphic has
     * {@value org.geotools.display.canvas.DisplayObject#DISPLAY_BOUNDS_PROPERTY} properties
     * listeners. Used in order to reduce the amount of {@link PropertyChangeEvent} objects created
     * in the common case where no listener have interest in this property. This optimisation may
     * be worth since a those change event may be sent every time a graphic is painted.
     *
     * @see #listenersChanged
     */
    private boolean hasBoundsListeners;

    /**
     * Constructs a new graphic with a default {@linkplain DefaultEngineeringCRS#GENERIC_2D
     * generic CRS}.
     *
     * @see #setObjectiveCRS
     * @see #setEnvelope
     * @see #setTypicalCellDimension
     * @see #setZOrderHint
     */
    protected ReferencedGraphic2D() {
        super(DefaultEngineeringCRS.GENERIC_2D);
    }

    /**
     * Constructs a new graphic using the specified objective CRS.
     *
     * @param  crs The objective coordinate reference system.
     * @throws IllegalArgumentException if {@code crs} is null or has an incompatible number of
     *         dimensions.
     *
     * @see #setObjectiveCRS
     * @see #setEnvelope
     * @see #setTypicalCellDimension
     * @see #setZOrderHint
     */
    protected ReferencedGraphic2D(final CoordinateReferenceSystem crs)
            throws IllegalArgumentException
    {
        super(to2D(crs));
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static CoordinateReferenceSystem to2D(final CoordinateReferenceSystem crs) {
        try {
            return CRSUtilities.getCRS2D(crs);
        } catch (TransformException e) {
            throw new IllegalArgumentException(e.getLocalizedMessage());
        }
    }

    /**
     * Sets the objective coordinate refernece system for this graphic.
     * If the specified CRS has more than two dimensions, then it must be a
     * {@linkplain org.opengis.referencing.crs.CompoundCRS compound CRS} with
     * a two dimensional head.
     * @throws TransformException 
     */
    @Override
    public void setObjectiveCRS(final CoordinateReferenceSystem crs) throws TransformException {
        super.setObjectiveCRS(CRSUtilities.getCRS2D(crs));
    }

    /**
     * Set the envelope for this graphic. Subclasses should invokes this method as soon as they
     * known their envelope.
     */
    @Override
    protected void setEnvelope(final Envelope envelope) throws TransformException {
        synchronized (getTreeLock()) {
            super.setEnvelope(envelope);
            displayBounds = XRectangle2D.INFINITY;
        }
    }

    /**
     * Returns a geometric shape that fully contains the display area painted during the last
     * {@linkplain GraphicPrimitive2D#paint rendering}. This shape must be in terms of the
     * {@linkplain ReferencedCanvas2D#getDisplayCRS display CRS}. It may be clipped to the
     * {@linkplain ReferencedCanvas2D#getDisplayBounds canvas display bounds}.
     * <p>
     * Note that there is no guarantee that the returned shape is the smallest shape that encloses
     * the graphic display area, only that the graphic display area (possibly clipped to the canvas
     * display area) lies entirely within the indicated shape. More specifically, the returned shape
     * may have infinite extends if the actual graphic bounds are unknown.
     * <p>
     * This method never returns {@code null}.
     */
    public final Shape getDisplayBounds() {
        return displayBounds;
    }

    /**
     * Sets the display bounds in terms of {@linkplain ReferencedCanvas2D#getDisplayCRS display CRS}.
     * The display may be approximative, as long as it completely encloses the display area (possibly
     * clipped to the {@linkplain ReferencedCanvas2D#getDisplayBounds canvas display bounds}.
     * Simple shapes with fast {@code contains(...)} and {@code intersects(...)} methods are
     * encouraged.
     * <p>
     * Some canvas implementations will invoke this method automatically in their
     * {@linkplain org.geotools.display.canvas.BufferedCanvas2D rendering method}.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#DISPLAY_BOUNDS_PROPERTY}
     * property change event.
     */
    public final void setDisplayBounds(Shape bounds) {
        if (bounds == null) {
            bounds = XRectangle2D.INFINITY;
        }
        final Shape old;
        synchronized (getTreeLock()) {
            old = displayBounds;
            displayBounds = bounds;
            if (hasBoundsListeners) {
                propertyListeners.firePropertyChange(DISPLAY_BOUNDS_PROPERTY, old, bounds);
            }
        }
    }

    /**
     * Advises that this graphic need to be repainted. The graphic will not be repainted
     * immediately, but at some later time depending on the widget implementation (e.g.
     * <cite>Swing</cite>). This {@code refresh()} method can be invoked from any thread;
     * it doesn't need to be the <cite>Swing</cite> thread.
     * <p>
     * Note that this method repaint only the area painted during the last {@linkplain
     * GraphicPrimitive2D#paint rendering}. If this graphic now cover a wider area, then the
     * area to repaint must be specified with a call to {@link #refresh(Rectangle2D)} instead.
     */
    public void refresh() {
        synchronized (getTreeLock()) {
            if (displayBounds.equals(XRectangle2D.INFINITY)) {
                refresh(null, displayBounds.getBounds());
            } else {
                refresh(XRectangle2D.INFINITY, null);
            }
        }
    }

    /**
     * Advises that some region need to be repainted. This graphic will not be repainted
     * immediately, but at some later time depending on the widget implementation (e.g.
     * <cite>Swing</cite>). This {@code refrech(...)} method can be invoked from any thread;
     * it doesn't need to be the <cite>Swing</cite> thread.
     *
     * @param bounds The dirty region to refreshed, in the "real world" {@linkplain #getObjectiveCRS
     *        objective coordinate reference system}. A {@code null} value refresh everything.
     */
    public void refresh(final Rectangle2D bounds) {
        synchronized (getTreeLock()) {
            refresh(bounds!=null ? bounds : XRectangle2D.INFINITY, null);
        }
    }

    /**
     * Advises that at least a portion of this graphic need to be repainted. This method
     * can been invoked from any thread (may or may not be the <cite>Swing</cite> thread).
     *
     * @param objectiveArea The dirty region to repaint in terms of
     *        {@linkplain #getObjectiveCRS objective CRS}, or {@code null}.
     * @paral displayArea The dirty region to repaint in terms of
     *        {@linkplain #getDisplayCRS display CRS}, or {@code null}.
     */
    private void refresh(final Rectangle2D objectiveArea, final Rectangle displayArea) {
        final Canvas owner = getCanvas();
        if (owner instanceof ReferencedCanvas2D) {
            final ReferencedCanvas2D canvas = (ReferencedCanvas2D) owner;
            canvas.repaint(this, objectiveArea, displayArea);
        }
    }

//    /**
//     * Invoked every time the {@linkplain ReferencedCanvas2D#getScale canvas scale} changed.
//     * The default implementation updates the {@linkplain #getDisplayBounds display bounds}
//     * as below:
//     *
//     * <ul>
//     *   <li><p>Since the {@linkplain #getDisplayBounds display bounds} is express in terms of
//     *       {@linkplain ReferencedCanvas2D#getDisplayCRS display CRS} and since the display CRS
//     *       is scale-dependent, a scale change implies a {@linkplain #getDisplayBounds display
//     *       bounds} change as well.</p></li>
//     *   <li><p>Since the {@linkplain #getDisplayBounds display bounds} may be clipped to the
//     *       {@linkplain ReferencedCanvas2D#getDisplayBounds canvas bounds}, a scale change may
//     *       bring some new area inside the canvas bounds. This new area may need to be rendered,
//     *       so we need to conservatively add it to this graphic {@linkplain #getDisplayBounds
//     *       display bounds}.</p></li>
//     * </ul>
//     *
//     * @param change The zoom <strong>change</strong> in <strong>Java2D</strong> coordinate
//     *        reference system ({@linkplain ReferencedCanvas2D#getDisplayCRS display CRS}),
//     *        or {@code null} if unknow. If {@code null}, then this graphic will be fully redrawn
//     *        during the next rendering.
//     */
//    public void zoomChanged(final AffineTransform change) {
//        synchronized (getTreeLock()) {
//            final Shape displayBounds = getDisplayBounds();
//            if (displayBounds.equals(XRectangle2D.INFINITY)) {
//                return;
//            }
//            if (change != null) {
//                final Canvas owner = getCanvas();
//                if (owner instanceof ReferencedCanvas2D) {
//                    final ReferencedCanvas2D canvas = (ReferencedCanvas2D) owner;
//                    final Shape canvasBounds = canvas.getDisplayBounds();
//                    if (!canvasBounds.equals(XRectangle2D.INFINITY)) {
//                        final Area newArea = new Area(canvasBounds);
//                        newArea.subtract(newArea.createTransformedArea(change));
//                        final Area area = (displayBounds instanceof Area) ?
//                                          (Area)displayBounds : new Area(displayBounds);
//                        area.transform(change);
//                        area.add(newArea);
//                        setDisplayBounds(area);
//                        return;
//                    }
//                }
//            }
//            setDisplayBounds(XRectangle2D.INFINITY);
//        }
//    }

    /**
     * Invoked when a property change listener has been {@linkplain #addPropertyChangeListener
     * added} or {@linkplain #removePropertyChangeListener removed}.
     */
    @Override
    protected void listenersChanged() {
        super.listenersChanged();
        hasBoundsListeners = propertyListeners.hasListeners(DISPLAY_BOUNDS_PROPERTY);
    }

    /**
     * Clears all cached data.
     */
    @Override
    public void clearCache() {
        assert Thread.holdsLock(getTreeLock());
        displayBounds = XRectangle2D.INFINITY;
        super.clearCache();
    }
}
