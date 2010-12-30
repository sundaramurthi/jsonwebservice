
package com.googlecode.jsonwebservice.attachment;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Colors complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Colors">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bg" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="border" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="titleBg" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="axisLabel" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="tickLabel" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="series" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Colors", propOrder = {
    "bg",
    "border",
    "titleBg",
    "title",
    "value",
    "axisLabel",
    "tickLabel",
    "series"
})
public class Colors {

    @XmlElement(defaultValue = "0xFFFFFF")
    protected int bg;
    @XmlElement(defaultValue = "0xE0E0E0")
    protected int border;
    @XmlElement(defaultValue = "0xF4F4F4")
    protected int titleBg;
    @XmlElement(defaultValue = "0x647FB0")
    protected int title;
    @XmlElement(defaultValue = "0x6E6E6E")
    protected int value;
    @XmlElement(defaultValue = "0x696969")
    protected int axisLabel;
    @XmlElement(defaultValue = "0xC0C0C0")
    protected int tickLabel;
    @XmlElement(type = Integer.class, defaultValue = "0x6A89ED,0x6AED89")
    protected List<Integer> series;

    /**
     * Gets the value of the bg property.
     * 
     */
    public int getBg() {
        return bg;
    }

    /**
     * Sets the value of the bg property.
     * 
     */
    public void setBg(int value) {
        this.bg = value;
    }

    /**
     * Gets the value of the border property.
     * 
     */
    public int getBorder() {
        return border;
    }

    /**
     * Sets the value of the border property.
     * 
     */
    public void setBorder(int value) {
        this.border = value;
    }

    /**
     * Gets the value of the titleBg property.
     * 
     */
    public int getTitleBg() {
        return titleBg;
    }

    /**
     * Sets the value of the titleBg property.
     * 
     */
    public void setTitleBg(int value) {
        this.titleBg = value;
    }

    /**
     * Gets the value of the title property.
     * 
     */
    public int getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     */
    public void setTitle(int value) {
        this.title = value;
    }

    /**
     * Gets the value of the value property.
     * 
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Gets the value of the axisLabel property.
     * 
     */
    public int getAxisLabel() {
        return axisLabel;
    }

    /**
     * Sets the value of the axisLabel property.
     * 
     */
    public void setAxisLabel(int value) {
        this.axisLabel = value;
    }

    /**
     * Gets the value of the tickLabel property.
     * 
     */
    public int getTickLabel() {
        return tickLabel;
    }

    /**
     * Sets the value of the tickLabel property.
     * 
     */
    public void setTickLabel(int value) {
        this.tickLabel = value;
    }

    /**
     * Gets the value of the series property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the series property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSeries().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getSeries() {
        if (series == null) {
            series = new ArrayList<Integer>();
        }
        return this.series;
    }

}
