
package com.googlecode.jsonwebservice.attachment;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DataSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataSettings">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataPart" type="{http://jsonwebservice.googlecode.com/attachment}DataPart"/>
 *         &lt;element name="rangeAxisIndex" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="domainAxisIndex" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="pieSectionLabel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSettings", propOrder = {
    "dataPart",
    "rangeAxisIndex",
    "domainAxisIndex",
    "pieSectionLabel"
})
public class DataSettings {

    @XmlElement(required = true, defaultValue = "DATA")
    protected DataPart dataPart;
    @XmlElement(nillable = true)
    protected List<Integer> rangeAxisIndex;
    @XmlElement(nillable = true)
    protected List<Integer> domainAxisIndex;
    @XmlElement(required = true, defaultValue = "{1} ({2}) {0}")
    protected String pieSectionLabel;

    /**
     * Gets the value of the dataPart property.
     * 
     * @return
     *     possible object is
     *     {@link DataPart }
     *     
     */
    public DataPart getDataPart() {
        return dataPart;
    }

    /**
     * Sets the value of the dataPart property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataPart }
     *     
     */
    public void setDataPart(DataPart value) {
        this.dataPart = value;
    }

    /**
     * Gets the value of the rangeAxisIndex property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rangeAxisIndex property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRangeAxisIndex().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getRangeAxisIndex() {
        if (rangeAxisIndex == null) {
            rangeAxisIndex = new ArrayList<Integer>();
        }
        return this.rangeAxisIndex;
    }

    /**
     * Gets the value of the domainAxisIndex property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the domainAxisIndex property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDomainAxisIndex().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getDomainAxisIndex() {
        if (domainAxisIndex == null) {
            domainAxisIndex = new ArrayList<Integer>();
        }
        return this.domainAxisIndex;
    }

    /**
     * Gets the value of the pieSectionLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPieSectionLabel() {
        return pieSectionLabel;
    }

    /**
     * Sets the value of the pieSectionLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPieSectionLabel(String value) {
        this.pieSectionLabel = value;
    }

}
