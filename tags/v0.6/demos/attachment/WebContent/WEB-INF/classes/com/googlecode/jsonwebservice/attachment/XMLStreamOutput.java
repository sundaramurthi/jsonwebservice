
package com.googlecode.jsonwebservice.attachment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Source;


/**
 * <p>Java class for XMLStreamOutput complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XMLStreamOutput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="outputFormates" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="xml" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XMLStreamOutput", propOrder = {
    "outputFormates",
    "xml"
})
public class XMLStreamOutput {

    @XmlElement(required = true)
    protected String outputFormates;
    @XmlElement(required = true)
    @XmlMimeType("application/xml")
    protected Source xml;

    /**
     * Gets the value of the outputFormates property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputFormates() {
        return outputFormates;
    }

    /**
     * Sets the value of the outputFormates property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputFormates(String value) {
        this.outputFormates = value;
    }

    /**
     * Gets the value of the xml property.
     * 
     * @return
     *     possible object is
     *     {@link Source }
     *     
     */
    public Source getXml() {
        return xml;
    }

    /**
     * Sets the value of the xml property.
     * 
     * @param value
     *     allowed object is
     *     {@link Source }
     *     
     */
    public void setXml(Source value) {
        this.xml = value;
    }

}
