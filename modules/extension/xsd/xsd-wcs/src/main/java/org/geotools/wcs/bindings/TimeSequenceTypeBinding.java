package org.geotools.wcs.bindings;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.gml3.GML;
import org.geotools.temporal.object.DefaultInstant;
import org.geotools.temporal.object.DefaultPeriod;
import org.geotools.temporal.object.DefaultPosition;
import org.geotools.util.SimpleInternationalString;
import org.geotools.wcs.WCS;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.Position;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Binding object for the type http://www.opengis.net/wcs:TimeSequenceType.
 * 
 * <p>
 * 
 * <pre>
 *	 <code>
 *  &lt;complexType name=&quot;TimeSequenceType&quot;&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;An ordered sequence of time positions or intervals. The time positions and periods shall be ordered from the oldest to the newest. &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;choice maxOccurs=&quot;unbounded&quot;&gt;
 *          &lt;element ref=&quot;gml:timePosition&quot;/&gt;
 *          &lt;element ref=&quot;wcs:timePeriod&quot;/&gt;
 *      &lt;/choice&gt;
 *  &lt;/complexType&gt; 
 * 	
 * </code>
 *	 </pre>
 * 
 * </p>
 * 
 * @generated
 */
public class TimeSequenceTypeBinding extends AbstractComplexBinding {

    /**
     * @generated
     */
    public QName getTarget() {
        return WCS.TimeSequenceType;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Class getType() {
        return List.class;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
            throws Exception {
        List<Node> timePositions = node.getChildren("timePosition");

        if (timePositions != null && !timePositions.isEmpty()) {
            List<Position> results = new LinkedList<Position>();
            for (Node timePositionNode : timePositions) {
                results.add((Position) timePositionNode.getValue());
            }

            return results;
        } else {
            List<Node> timePeriods = node.getChildren("timePeriod");
            if (timePeriods != null && !timePeriods.isEmpty()) {
                List<Period> results = new LinkedList<Period>();
                for (Node timePeriodNode : timePeriods) {
                    Instant begining = new DefaultInstant((Position) timePeriodNode.getChild("beginPosition").getValue());
                    Instant ending = new DefaultInstant((Position) timePeriodNode.getChild("endPosition").getValue());

                    Period timePeriod = new DefaultPeriod(begining, ending);

                    results.add(timePeriod);
                }

                return results;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.xml.AbstractComplexBinding#encode(java.lang.Object,
     *      org.w3c.dom.Document, org.w3c.dom.Element)
     */
    @Override
    public Element encode(Object object, Document document, Element value)
            throws Exception {
        List timeSequence = (List) object;

        if (timeSequence == null) {
            value.appendChild(document.createElementNS(GML.NAMESPACE, GML.Null.getLocalPart()));
        }

        return null;
    }

    public Object getProperty(Object object, QName name) {
        List timeSequence = (List) object;

        if (timeSequence == null || timeSequence.isEmpty()) {
            return null;
        }

        if (name.getLocalPart().equals("timePeriod") && timeSequence.get(0) instanceof Period) {
            return timeSequence;
        }

        if (name.getLocalPart().equals("timePosition")
                && timeSequence.get(0) instanceof Position) {
            List<Position> result = new LinkedList<Position>();

            for (Position position : (List<Position>) timeSequence)
                result.add(position);

            return result;
        }

        return null;
    }
}