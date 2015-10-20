
package com.googlecode.jsonwebservice.attachment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Source;


/**
 * <p>Java class for ChartInput complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChartInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="chartConfig" type="{http://jsonwebservice.googlecode.com/attachment}ChartConfig"/>
 *         &lt;element name="xmlData" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChartInput", propOrder = {
    "chartConfig",
    "xmlData"
})
public class ChartInput {

    @XmlElement(required = true)
    protected ChartConfig chartConfig;
    @XmlElement(required = true)
    @XmlMimeType("text/xml")
    protected Source xmlData;

    /**
     * Gets the value of the chartConfig property.
     * 
     * @return
     *     possible object is
     *     {@link ChartConfig }
     *     
     */
    public ChartConfig getChartConfig() {
        return chartConfig;
    }

    /**
     * Sets the value of the chartConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChartConfig }
     *     
     */
    public void setChartConfig(ChartConfig value) {
        this.chartConfig = value;
    }

    /**
     * Gets the value of the xmlData property.
     * 
     * @return
     *     possible object is
     *     {@link Source }
     *     
     */
    public Source getXmlData() {
        return xmlData;
    }

    /**
     * Sets the value of the xmlData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Source }
     *     
     */
    public void setXmlData(Source value) {
        this.xmlData = value;
    }

}
