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
 * A Halo fills an extended area outside the glyphs of a rendered textlabel
 * to make it easier to read over a background.
 *
 * $Id: Halo.java,v 1.5 2003/08/01 16:54:12 ianturton Exp $ 
 * @author Ian Turton, CCG
 */
public interface Halo {
    /** Expression that represents the the distance the halo extends from the text
     */
    Expression getRadius();
    /** Expression that represents the the distance the halo extends from the text
     */
    void setRadius(Expression radius);
    /** The fill (color) of the halo
     */
    Fill getFill();
    /** The fill (color) of the halo
     */
    void setFill(Fill fill);

    void accept(StyleVisitor visitor);
}
