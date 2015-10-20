
package com.googlecode.jsonwebservice.attachment;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ChartConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChartConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="size" type="{http://jsonwebservice.googlecode.com/attachment}Size"/>
 *         &lt;element name="colors" type="{http://jsonwebservice.googlecode.com/attachment}Colors"/>
 *         &lt;element name="visibility" type="{http://jsonwebservice.googlecode.com/attachment}Visibility"/>
 *         &lt;element name="dataSettings" type="{http://jsonwebservice.googlecode.com/attachment}DataSettings"/>
 *         &lt;element name="preferedCharts" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChartConfig", propOrder = {
    "size",
    "colors",
    "visibility",
    "dataSettings",
    "preferedCharts"
})
public class ChartConfig {

    @XmlElement(required = true)
    protected Size size;
    @XmlElement(required = true)
    protected Colors colors;
    @XmlElement(required = true)
    protected Visibility visibility;
    @XmlElement(required = true)
    protected DataSettings dataSettings;
    @XmlElement(required = true, defaultValue = "BAR,PIE")
    protected List<String> preferedCharts;

    /**
     * Gets the value of the size property.
     * 
     * @return
     *     possible object is
     *     {@link Size }
     *     
     */
    public Size getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     * @param value
     *     allowed object is
     *     {@link Size }
     *     
     */
    public void setSize(Size value) {
        this.size = value;
    }

    /**
     * Gets the value of the colors property.
     * 
     * @return
     *     possible object is
     *     {@link Colors }
     *     
     */
    public Colors getColors() {
        return colors;
    }

    /**
     * Sets the value of the colors property.
     * 
     * @param value
     *     allowed object is
     *     {@link Colors }
     *     
     */
    public void setColors(Colors value) {
        this.colors = value;
    }

    /**
     * Gets the value of the visibility property.
     * 
     * @return
     *     possible object is
     *     {@link Visibility }
     *     
     */
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Sets the value of the visibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link Visibility }
     *     
     */
    public void setVisibility(Visibility value) {
        this.visibility = value;
    }

    /**
     * Gets the value of the dataSettings property.
     * 
     * @return
     *     possible object is
     *     {@link DataSettings }
     *     
     */
    public DataSettings getDataSettings() {
        return dataSettings;
    }

    /**
     * Sets the value of the dataSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSettings }
     *     
     */
    public void setDataSettings(DataSettings value) {
        this.dataSettings = value;
    }

    /**
     * Gets the value of the preferedCharts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the preferedCharts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPreferedCharts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getPreferedCharts() {
        if (preferedCharts == null) {
            preferedCharts = new ArrayList<String>();
        }
        return this.preferedCharts;
    }

}
