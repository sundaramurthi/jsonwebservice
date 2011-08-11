
package com.googlecode.jsonwebservice.attachment;

import java.awt.Image;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ChartOutput complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChartOutput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="outputFormates" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="svg" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="image" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChartOutput", propOrder = {
    "outputFormates",
    "svg",
    "image"
})
public class ChartOutput {

    @XmlElement(required = true)
    protected String outputFormates;
    @XmlElement(required = true)
    @XmlMimeType("image/svg+xml")
    protected Image svg;
    @XmlElement(required = true)
    @XmlMimeType("image/jpeg")
    protected Image image;

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
     * Gets the value of the svg property.
     * 
     * @return
     *     possible object is
     *     {@link Image }
     *     
     */
    public Image getSvg() {
        return svg;
    }

    /**
     * Sets the value of the svg property.
     * 
     * @param value
     *     allowed object is
     *     {@link Image }
     *     
     */
    public void setSvg(Image value) {
        this.svg = value;
    }

    /**
     * Gets the value of the image property.
     * 
     * @return
     *     possible object is
     *     {@link Image }
     *     
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the value of the image property.
     * 
     * @param value
     *     allowed object is
     *     {@link Image }
     *     
     */
    public void setImage(Image value) {
        this.image = value;
    }

}
