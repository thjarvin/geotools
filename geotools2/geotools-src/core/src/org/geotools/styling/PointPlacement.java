/*
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Center for Computational Geography
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
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */

package org.geotools.styling;
import org.geotools.filter.Expression;
/**
 * A PointPlacement specifies how a text label is positioned relative to a
 * geometric point.
 * $Id: PointPlacement.java,v 1.4 2003/08/01 16:54:21 ianturton Exp $
 * @author Ian Turton
 */
public interface PointPlacement extends LabelPlacement{
    /**
     * Returns the AnchorPoint which identifies the location inside a textlabel
     * to use as an "anchor" for positioning it relative to a point geometry.
     */
    AnchorPoint getAnchorPoint();
    
    /**
     * sets the AnchorPoint which identifies the location inside a textlabel
     * to use as an "anchor" for positioning it relative to a point geometry.
     */
    void setAnchorPoint(AnchorPoint anchorPoint);
    
    /**
     * Returns the Displacement which gives X and Y offset displacements to use
     * for rendering a text label near a point.
     */
    Displacement getDisplacement();
    
    /**
     * sets the Displacement which gives X and Y offset displacements to use
     * for rendering a text label near a point.
     */
    void setDisplacement(Displacement displacement);
    
    /** 
     * Returns the rotation of the label.
     */
    Expression getRotation();
    
    /** 
     * sets the rotation of the label.
     */
    void setRotation(Expression rotation);
    
    void accept(StyleVisitor visitor);
}