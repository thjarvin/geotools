package org.geotools.feature;

import java.util.*;

/**
 * Simple, immutable class to store attributes.  This class should be sufficient 
 * for all simple (ie. non-schema) attribute implementations of this interface.
 *
 * @author Rob Hranac, VFNY
 */
public class AttributeTypeDefault implements AttributeType {
    
    /** Name of this attribute. */
    private final String name;
        
    /** Class type of this attribute. */
    private final String type;
        
    /** Number of instances of this attribute in the schema. */
    private int occurrences = 1;
    
    /** Storage position of this attribute in the array. */
    private int position = -1;
    
    
    /**
     * Constructor with name and type.
     *
     * @param name Name of this attribute.
     * @param type Class type of this attribute.
     */
    public AttributeTypeDefault (String name, String type) {
        this.name = name;
        this.type = type;
    }
    
    /**
     * Constructor with geometry.
     *
     * @param name Name of this attribute.
     * @param type Class type of this attribute.
     * @param occurrences Number of instances of this attribute in the schema.
     */
    public AttributeTypeDefault (String name, String type, int occurrences) {
        this.name = name;
        this.type = type;
        this.occurrences = occurrences;
    }
    
    
    /**
     * Sets the position of this attribute in the schema.
     * 
     * @param position Position of attribute.
     * @return Copy of attribute with modified position.
     */
    public AttributeType setPosition(int position) {
        AttributeTypeDefault tempCopy = 
            new AttributeTypeDefault(this.name, this.type, this.occurrences);
        tempCopy.position = position;
        return tempCopy;
    } 

    /**
     * False, since it is not a schema.
     * 
     * @return False.
     */
    public boolean isNested() {
        return false;
    }
    
    /**
     * Gets the name of this attribute.
     * 
     * @return Name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the type of this attribute.
     * 
     * @return Type.
     */
    public String getType() {
        return type;
    }
    
    /**
     * Gets the occurences of this attribute.
     * 
     * @return Occurrences.
     */
    public int getOccurrences() {
        return occurrences;
    }
    
    /**
     * Gets the position of this attribute.
     * 
     * @return Position.
     */
    public int getPosition() {
        return position;
    }

    
    public Object clone()
        throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Gets the number of occurances of this attribute.
     *
     * @return Number of occurrences.
     * @throws SchemaException If the attribute does not exist.
     */
    public String toString() {
        StringBuffer returnString = new StringBuffer(this.position + ". ");
        returnString.append(this.name);
        returnString.append(" [" + this.type + "]");
        returnString.append(" - " + this.occurrences);
        return returnString.toString();
    }
}
