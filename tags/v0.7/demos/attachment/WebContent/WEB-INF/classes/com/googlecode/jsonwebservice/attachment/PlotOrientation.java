
package com.googlecode.jsonwebservice.attachment;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PlotOrientation.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PlotOrientation">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="HORIZONTAL"/>
 *     &lt;enumeration value="VERTICAL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PlotOrientation")
@XmlEnum
public enum PlotOrientation {

    HORIZONTAL,
    VERTICAL;

    public String value() {
        return name();
    }

    public static PlotOrientation fromValue(String v) {
        return valueOf(v);
    }

}
