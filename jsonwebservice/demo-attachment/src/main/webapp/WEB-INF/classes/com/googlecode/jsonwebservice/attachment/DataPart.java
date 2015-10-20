
package com.googlecode.jsonwebservice.attachment;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DataPart.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DataPart">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DATA"/>
 *     &lt;enumeration value="STAT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DataPart")
@XmlEnum
public enum DataPart {

    DATA,
    STAT;

    public String value() {
        return name();
    }

    public static DataPart fromValue(String v) {
        return valueOf(v);
    }

}
