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

import java.util.Locale;
import java.util.logging.Logger;
import java.text.NumberFormat;
import java.text.FieldPosition;
import java.beans.PropertyChangeEvent;  // For javadoc

import org.opengis.display.canvas.Canvas;
import org.opengis.display.primitive.Graphic;

import org.geotools.resources.Classes;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.display.canvas.AbstractCanvas;
import org.geotools.display.canvas.DisplayObject;

/**
 * The root abstraction of a graphic object taxonomy, specifying the methods common to a
 * lightweight set of graphic objects.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractGraphic extends DisplayObject implements Graphic {
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractGraphic#getName graphic name} changed.
     */
    public static final String NAME_PROPERTY = "name";
    
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractGraphic#getParent graphic parent} changed.
     */
    public static final String PARENT_PROPERTY = "parent";
    
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractGraphic#getZOrderHint z order hint} changed.
     */
    public static final String Z_ORDER_HINT_PROPERTY = "zOrderHint";
    
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractGraphic#getVisible graphic visibility} changed.
     */
    public static final String VISIBLE_PROPERTY = "visible";
        
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractGraphic#getCanvas graphic canvas} changed.
     */
    public static final String CANVAS_PROPERTY = "canvas";
    /**
     * The default {@linkplain #getZOrderHint z-order}.
     */
    private static final double DEFAULT_Z_ORDER = Double.POSITIVE_INFINITY;

    /**
     * The format used during the last call to {@link #getName}. We use only one instance for
     * all graphics, since an application is likely to use only one locale. However, more locales
     * are allowed; it will just be slower.
     */
    private static Format format;
    
    /**
     * Convenience class for {@link RenderedLayer#getName}.
     * This class should be immutable and thread-safe.
     */
    private static final class Format {
        /** The locale of the {@link #format}. */
        public final Locale locale;
        
        /** The format in the {@link #locale}. */
        public final NumberFormat format;

        /** Construct a format for the given locale. */
        public Format(final Locale locale) {
            this.locale = locale;
            this.format = NumberFormat.getNumberInstance(locale);
        }
    }
    
    /**
     * The canvas that own this graphic, or {@code null} if none.
     */
    protected Canvas canvas;
    
    /**
     * The name assigned to this graphic.
     */
    protected String name;
    
    /**
     * The parent of this graphic, or {@code null} if none.
     */
    protected Graphic parent;
    
    /**
     * Tells if this graphic is visible.
     *
     * @see #setVisible
     */
    protected boolean visible = true;
    
    /**
     * The z value for this graphic.
     *
     * @see #getZOrderHint
     * @see #setZOrderHint
     */
    protected double zOrder = DEFAULT_Z_ORDER;

    /**
     * Creates a new graphic. The {@linkplain #getZOrderHint z-order} default to positive infinity
     * (i.e. this graphic is drawn on top of everything else). Subclasses should invokes setters
     * methods in order to define properly this graphic properties.
     */
    protected AbstractGraphic() {
    }

    /**
     * If this display object is contained in a canvas, returns the canvas that own it.
     * Otherwise, returns {@code null}.
     * 
     * @return Canvas, The canvas that this graphic listen to.
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Set the canvas to the specified value.
     * 
     * @param canvas The new canvas that this graphic listen to.
     */
    public void setCanvas(final Canvas canvas) {
        final Canvas old;
        synchronized (this) {
            old = this.canvas;
            this.canvas = canvas;
        }
        propertyListeners.firePropertyChange(CANVAS_PROPERTY, old, canvas);
    }

    /**
     * Returns the name assigned to this {@code Graphic}. If no name were
     * {@linkplain #setName explicitly set}, then this method returns a default
     * name built from the {@linkplain #getZOrderHint z order}.
     */
    public String getName() {
        final String name = this.name;  // Avoid the need for synchronization.

        if (name != null) {
            return name;
        }
        final Locale locale = getLocale();
        Format f = format; // Avoid the need for synchronization.

        if (f == null || !f.locale.equals(locale)) {
            format = f = new Format(locale);
        }
        final StringBuffer buffer = new StringBuffer("z=");
        return f.format.format(getZOrderHint(), buffer, new FieldPosition(0)).toString();
    }

    /**
     * Sets the name of this {@code Graphic} to the given value.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#NAME_PROPERTY}
     * property change event.
     */
    public void setName(final String name) {
        final String old;
        synchronized (getTreeLock()) {
            old = this.name;
            this.name = name;
        }
        propertyListeners.firePropertyChange(NAME_PROPERTY, old, name);
    }

    /**
     * Returns the parent of this {@code Graphic}, or {@code null} if none. Usually, only
     * {@link org.opengis.go.display.primitive.AggregateGraphic}s have {@code Graphic} children.
     */
    public Graphic getParent() {
        return parent;
    }

    /**
     * Sets the parent of this {@code Graphic}.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#PARENT_PROPERTY}
     * property change event.
     */
    public void setParent(final Graphic parent) {
        final Graphic old;
        synchronized (getTreeLock()) {
            old = this.parent;
            this.parent = parent;
        }
        propertyListeners.firePropertyChange(PARENT_PROPERTY, old, parent);
    }

    /**
     * Returns the <var>z</var> order hint value for this graphic. Graphics with highest
     * <var>z</var> order will be painted on top of graphics with lowest <var>z</var> order.
     * The default value is {@link Double#POSITIVE_INFINITY}.
     */
    public double getZOrderHint() {
        synchronized (getTreeLock()) {
            return zOrder;
        }
    }

    /**
     * Sets the <var>z</var> order hint value for this graphic. Graphics with highest
     * <var>z</var> order will be painted on top of graphics with lowest <var>z</var> order.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#Z_ORDER_HINT_PROPERTY}
     * property change event.
     */
    public void setZOrderHint(final double zOrderHint) {
        if (Double.isNaN(zOrderHint)) {
            throw new IllegalArgumentException(Errors.getResources(getLocale()).getString(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "zOrderHint", zOrderHint));
        }
        final double oldZOrder;
        synchronized (getTreeLock()) {
            oldZOrder = this.zOrder;
            if (zOrderHint == oldZOrder) {
                return;
            }
            this.zOrder = zOrderHint;
        }
        propertyListeners.firePropertyChange(Z_ORDER_HINT_PROPERTY, oldZOrder, zOrderHint);
    }

    /**
     * Determines whether this graphic should be visible when its {@linkplain #getCanvas canvas}
     * is visible. The default value is {@code true}.
     *
     * @return {@code true} if the graphic is visible, {@code false} otherwise.
     */
    public boolean getVisible() {
        return visible;
    }

    /**
     * Sets the visible value. This method may be invoked when the user wants to hide momentarily
     * this graphic.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#VISIBLE_PROPERTY}
     * property change event.
     */
    public void setVisible(final boolean visible) {
        synchronized (getTreeLock()) {
            if (visible == this.visible) {
                return;
            }
            this.visible = visible;
        }
        propertyListeners.firePropertyChange(VISIBLE_PROPERTY, !visible, visible);
    }

    /**
     * Creates a new {@code Graphic} of the same type as this object. The default implementation
     * invokes {@link #clone}. If the later throws a {@link CloneNotSupportedException}, then the
     * checked exception is wrapped in an unchecked one.
     * <p>
     * By default, {@code AbstractGraphic} are not cloneable. Subclasses need to implements the
     * {@link Cloneable} interface if they support cloning.
     *
     * @return The cloned graphic.
     * @throws IllegalStateException if this graphic is not cloneable.
     */
    public final Graphic cloneGraphic() throws IllegalStateException {
        synchronized (getTreeLock()) {
            try {
                return clone();
            } catch (CloneNotSupportedException exception) {
                throw new IllegalStateException(exception.getLocalizedMessage(), exception);
            }
        }
    }

    /**
     * Creates a new {@code Graphic} of the same type as this object. The resulting object should be
     * identical in all respects to the original, except the {@linkplain #getParent parent attribute}
     * which is set to {@code null}.
     * <p>
     * By default, {@code AbstractGraphic} are not cloneable. Subclasses need to implement the
     * {@link Cloneable} interface if they support cloning.
     *
     * @return The cloned graphic.
     * @throws CloneNotSupportedException if this graphic is not cloneable.
     */
    @Override
    public AbstractGraphic clone() throws CloneNotSupportedException {
        assert Thread.holdsLock(getTreeLock());
        final AbstractGraphic clone = (AbstractGraphic) super.clone();
        clone.canvas = null;
        clone.parent = null;
        return clone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        synchronized (getTreeLock()) {
            super.dispose();
        }
    }
    
    /**
     * Returns the locale for this object. If this graphic is contained in a
     * {@linkplain AbstractCanvas canvas}, then the default implementation returns the canvas
     * locale. Otherwise, this method returns the {@linkplain Locale#getDefault system locale}.
     */
    @Override
    public Locale getLocale() {
        final Canvas canvas = getCanvas();
        if (canvas instanceof DisplayObject) {
            return ((DisplayObject) canvas).getLocale();
        }
        return super.getLocale();
    }
    
    /**
     * Returns the logger for all messages to be logged by the Geotools implementation of GO-1. If
     * this object is a {@linkplain Graphic graphic} which is contained in a {@linkplain Canvas
     * canvas}, then the default implementation returns the canvas logger. Otherwise, this method
     * returns a default one.
     */
    @Override
    public Logger getLogger() {
        final Canvas canvas = getCanvas();
        if (canvas instanceof DisplayObject) {
            return ((DisplayObject) canvas).getLogger();
        }
        return super.getLogger();
    }

    /**
     * Returns the lock for synchronisation. If this object is contained in a canvas,
     * then this method returns the same lock than the canvas.
     */
    public final Object getTreeLock() {
        final Canvas canvas = this.canvas;
        return (canvas != null) ? (Object) canvas : (Object) this;
    }

    /**
     * Returns a string representation of this graphic. This method is for debugging purpose
     * only and may changes in any future version.
     */
    @Override
    public String toString() {
        return Classes.getShortClassName(this) + '[' + getName() + ']';
    }
}
