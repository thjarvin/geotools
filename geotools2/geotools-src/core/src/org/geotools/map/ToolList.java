/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 */

package org.geotools.map;

import org.geotools.gui.tools.Tool;
import org.geotools.map.events.SelectedToolListener;

/**
 * The tool which will process mouse events on a MapPane.
 * @version $Id: ToolList.java,v 1.2 2003/04/14 21:37:16 jmacgill Exp $
 * @author  Cameron Shorter
 */

public interface ToolList {

    /**
     * Register interest in being called when Tool changes.
     * @param listener The object to notify when tool changes.
     */
    public void addSelectedToolListener(
            SelectedToolListener listener);

    /**
     * Remove interest in bening notified when Tool changes.
     * @param listener The listener.
     */
    public void removeSelectedToolListener(SelectedToolListener listener);

    /**
     * Get the tool.
     * @return The selected tool.
     */
    public Tool getTool();

    /**
     * Set the tool.
     * @param tool The new tool.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public void setTool(Tool tool) throws IllegalArgumentException;
}
