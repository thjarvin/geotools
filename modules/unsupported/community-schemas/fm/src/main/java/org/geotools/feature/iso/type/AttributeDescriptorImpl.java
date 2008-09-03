package org.geotools.feature.iso.type;

import java.util.List;

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;

public class AttributeDescriptorImpl extends StructuralDescriptorImpl 
	implements AttributeDescriptor {
	
	protected final AttributeType type;
	
	protected boolean isNillable;

	private Object defaultValue;
	
	public AttributeDescriptorImpl(
		AttributeType type, Name name, int min, int max, boolean isNillable, Object defaultValue
	) {
		super(name,min,max);
		
		if (type == null) {
			throw new NullPointerException();
		}
		if ((min < 0) || (max < 0) || (max < min))
			throw new IllegalArgumentException(
					"min("
							+ min
							+ ") and max("
							+ max
							+ ") must be positive integers and max must be greater than or equal to min");

		this.type = type;
		this.isNillable = isNillable;
		this.defaultValue = defaultValue;
	}
	
	public boolean isNillable() {
		return isNillable;
	}
	
	public AttributeType/*<?>*/ getType() {
		return type;
	}
    
    public PropertyType type() {
        return getType();
    }
	
	public void validate(List/*<Attribute>*/ content) throws NullPointerException,
		IllegalArgumentException {
		DescriptorValidator.validate(this, content);
	}
	
	public int hashCode(){
		return (37 * minOccurs + 37 * maxOccurs ) ^ 
			(type.hashCode() * name.hashCode());
	}
	
	public boolean equals(Object o){
		if(!(o instanceof AttributeDescriptorImpl))
			return false;
		
		AttributeDescriptorImpl d = (AttributeDescriptorImpl)o;
		return minOccurs == d.minOccurs && 
			maxOccurs == d.maxOccurs && 
			name.equals(d.name) && 
			type.equals(d.type);
			
	}	
	
	public String toString(){
		StringBuffer sb = new StringBuffer("AttributeDescriptor ")
		.append(name.getLocalPart())
		.append(":")
		.append(type.getName().getLocalPart());
		return sb.toString();
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}
