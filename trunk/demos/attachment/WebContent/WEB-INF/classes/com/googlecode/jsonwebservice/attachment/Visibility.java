
package com.googlecode.jsonwebservice.attachment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Visibility complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Visibility">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="border" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="outline" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="tickMarks" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="minerTickMarks" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="rangeGridLines" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="rangeZeroBaseline" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="rangeCrosshair" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="axisLine" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="pieOutlines" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="pieSimpleLabels" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="pieLabelLinks" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="urls" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="useCSS" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="legend" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="orientation" type="{http://jsonwebservice.googlecode.com/attachment}PlotOrientation"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Visibility", propOrder = {
    "border",
    "outline",
    "tickMarks",
    "minerTickMarks",
    "rangeGridLines",
    "rangeZeroBaseline",
    "rangeCrosshair",
    "axisLine",
    "pieOutlines",
    "pieSimpleLabels",
    "pieLabelLinks",
    "urls",
    "useCSS",
    "title",
    "legend",
    "orientation"
})
public class Visibility {

    @XmlElement(defaultValue = "false")
    protected boolean border;
    @XmlElement(defaultValue = "false")
    protected boolean outline;
    @XmlElement(defaultValue = "false")
    protected boolean tickMarks;
    @XmlElement(defaultValue = "false")
    protected boolean minerTickMarks;
    @XmlElement(defaultValue = "false")
    protected boolean rangeGridLines;
    @XmlElement(defaultValue = "true")
    protected boolean rangeZeroBaseline;
    @XmlElement(defaultValue = "false")
    protected boolean rangeCrosshair;
    @XmlElement(defaultValue = "false")
    protected boolean axisLine;
    @XmlElement(defaultValue = "false")
    protected boolean pieOutlines;
    @XmlElement(defaultValue = "true")
    protected boolean pieSimpleLabels;
    @XmlElement(defaultValue = "true")
    protected boolean pieLabelLinks;
    @XmlElement(defaultValue = "false")
    protected boolean urls;
    @XmlElement(defaultValue = "false")
    protected boolean useCSS;
    @XmlElement(defaultValue = "true")
    protected boolean title;
    @XmlElement(defaultValue = "true")
    protected boolean legend;
    @XmlElement(required = true, defaultValue = "VERTICAL")
    protected PlotOrientation orientation;

    /**
     * Gets the value of the border property.
     * 
     */
    public boolean isBorder() {
        return border;
    }

    /**
     * Sets the value of the border property.
     * 
     */
    public void setBorder(boolean value) {
        this.border = value;
    }

    /**
     * Gets the value of the outline property.
     * 
     */
    public boolean isOutline() {
        return outline;
    }

    /**
     * Sets the value of the outline property.
     * 
     */
    public void setOutline(boolean value) {
        this.outline = value;
    }

    /**
     * Gets the value of the tickMarks property.
     * 
     */
    public boolean isTickMarks() {
        return tickMarks;
    }

    /**
     * Sets the value of the tickMarks property.
     * 
     */
    public void setTickMarks(boolean value) {
        this.tickMarks = value;
    }

    /**
     * Gets the value of the minerTickMarks property.
     * 
     */
    public boolean isMinerTickMarks() {
        return minerTickMarks;
    }

    /**
     * Sets the value of the minerTickMarks property.
     * 
     */
    public void setMinerTickMarks(boolean value) {
        this.minerTickMarks = value;
    }

    /**
     * Gets the value of the rangeGridLines property.
     * 
     */
    public boolean isRangeGridLines() {
        return rangeGridLines;
    }

    /**
     * Sets the value of the rangeGridLines property.
     * 
     */
    public void setRangeGridLines(boolean value) {
        this.rangeGridLines = value;
    }

    /**
     * Gets the value of the rangeZeroBaseline property.
     * 
     */
    public boolean isRangeZeroBaseline() {
        return rangeZeroBaseline;
    }

    /**
     * Sets the value of the rangeZeroBaseline property.
     * 
     */
    public void setRangeZeroBaseline(boolean value) {
        this.rangeZeroBaseline = value;
    }

    /**
     * Gets the value of the rangeCrosshair property.
     * 
     */
    public boolean isRangeCrosshair() {
        return rangeCrosshair;
    }

    /**
     * Sets the value of the rangeCrosshair property.
     * 
     */
    public void setRangeCrosshair(boolean value) {
        this.rangeCrosshair = value;
    }

    /**
     * Gets the value of the axisLine property.
     * 
     */
    public boolean isAxisLine() {
        return axisLine;
    }

    /**
     * Sets the value of the axisLine property.
     * 
     */
    public void setAxisLine(boolean value) {
        this.axisLine = value;
    }

    /**
     * Gets the value of the pieOutlines property.
     * 
     */
    public boolean isPieOutlines() {
        return pieOutlines;
    }

    /**
     * Sets the value of the pieOutlines property.
     * 
     */
    public void setPieOutlines(boolean value) {
        this.pieOutlines = value;
    }

    /**
     * Gets the value of the pieSimpleLabels property.
     * 
     */
    public boolean isPieSimpleLabels() {
        return pieSimpleLabels;
    }

    /**
     * Sets the value of the pieSimpleLabels property.
     * 
     */
    public void setPieSimpleLabels(boolean value) {
        this.pieSimpleLabels = value;
    }

    /**
     * Gets the value of the pieLabelLinks property.
     * 
     */
    public boolean isPieLabelLinks() {
        return pieLabelLinks;
    }

    /**
     * Sets the value of the pieLabelLinks property.
     * 
     */
    public void setPieLabelLinks(boolean value) {
        this.pieLabelLinks = value;
    }

    /**
     * Gets the value of the urls property.
     * 
     */
    public boolean isUrls() {
        return urls;
    }

    /**
     * Sets the value of the urls property.
     * 
     */
    public void setUrls(boolean value) {
        this.urls = value;
    }

    /**
     * Gets the value of the useCSS property.
     * 
     */
    public boolean isUseCSS() {
        return useCSS;
    }

    /**
     * Sets the value of the useCSS property.
     * 
     */
    public void setUseCSS(boolean value) {
        this.useCSS = value;
    }

    /**
     * Gets the value of the title property.
     * 
     */
    public boolean isTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     */
    public void setTitle(boolean value) {
        this.title = value;
    }

    /**
     * Gets the value of the legend property.
     * 
     */
    public boolean isLegend() {
        return legend;
    }

    /**
     * Sets the value of the legend property.
     * 
     */
    public void setLegend(boolean value) {
        this.legend = value;
    }

    /**
     * Gets the value of the orientation property.
     * 
     * @return
     *     possible object is
     *     {@link PlotOrientation }
     *     
     */
    public PlotOrientation getOrientation() {
        return orientation;
    }

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlotOrientation }
     *     
     */
    public void setOrientation(PlotOrientation value) {
        this.orientation = value;
    }

}
