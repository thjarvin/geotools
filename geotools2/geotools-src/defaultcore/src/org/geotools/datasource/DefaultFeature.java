/*
 * DefaultFeature.java
 *
 * Created on March 15, 2002, 3:46 PM
 */

package org.geotools.datasource;

import com.vividsolutions.jts.geom.*;

/**
 *
 * @author  jamesm
 */
public class DefaultFeature implements org.geotools.datasource.Feature {

    protected Object[] attributes;
    protected String[] colNames;
    protected int geomColumn = 0;//HACK: unsafe assumption?
    
    /** Creates a new instance of DefaultFeature */
    public DefaultFeature() {
    }
    
    public void setAttributes(Object[] attributes,String[] colNames){
        this.attributes = attributes;
        this.colNames = colNames;
    }
    
    public Object[] getAttributes() {
        return attributes;
    }
    
    public String[] getAttributeNames() {
        return colNames;
    }
    
    public Geometry getGeometry() {
        return (Geometry)attributes[geomColumn];
    }
    
}
