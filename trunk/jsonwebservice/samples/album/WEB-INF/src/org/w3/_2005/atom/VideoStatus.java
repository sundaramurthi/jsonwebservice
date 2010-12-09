
package org.w3._2005.atom;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VideoStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="VideoStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="pending"/>
 *     &lt;enumeration value="ready"/>
 *     &lt;enumeration value="final"/>
 *     &lt;enumeration value="failed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "VideoStatus")
@XmlEnum
public enum VideoStatus {

    @XmlEnumValue("pending")
    PENDING("pending"),
    @XmlEnumValue("ready")
    READY("ready"),
    @XmlEnumValue("final")
    FINAL("final"),
    @XmlEnumValue("failed")
    FAILED("failed");
    private final String value;

    VideoStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VideoStatus fromValue(String v) {
        for (VideoStatus c: VideoStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
