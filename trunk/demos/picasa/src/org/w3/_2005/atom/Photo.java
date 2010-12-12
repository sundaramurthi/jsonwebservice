
package org.w3._2005.atom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Photo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Photo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="checksum" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="commentCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="height" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="width" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="rotation" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="videostatus" type="{http://www.w3.org/2005/Atom}VideoStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Photo", propOrder = {
    "checksum",
    "commentCount",
    "height",
    "width",
    "rotation",
    "size",
    "timestamp",
    "videostatus"
})
public class Photo {

    @XmlElement(required = true)
    protected String checksum;
    protected int commentCount;
    protected int height;
    protected int width;
    protected int rotation;
    protected int size;
    protected int timestamp;
    @XmlElement(required = true)
    protected VideoStatus videostatus;

    /**
     * Gets the value of the checksum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets the value of the checksum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChecksum(String value) {
        this.checksum = value;
    }

    /**
     * Gets the value of the commentCount property.
     * 
     */
    public int getCommentCount() {
        return commentCount;
    }

    /**
     * Sets the value of the commentCount property.
     * 
     */
    public void setCommentCount(int value) {
        this.commentCount = value;
    }

    /**
     * Gets the value of the height property.
     * 
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     */
    public void setHeight(int value) {
        this.height = value;
    }

    /**
     * Gets the value of the width property.
     * 
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     */
    public void setWidth(int value) {
        this.width = value;
    }

    /**
     * Gets the value of the rotation property.
     * 
     */
    public int getRotation() {
        return rotation;
    }

    /**
     * Sets the value of the rotation property.
     * 
     */
    public void setRotation(int value) {
        this.rotation = value;
    }

    /**
     * Gets the value of the size property.
     * 
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     */
    public void setSize(int value) {
        this.size = value;
    }

    /**
     * Gets the value of the timestamp property.
     * 
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     */
    public void setTimestamp(int value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the videostatus property.
     * 
     * @return
     *     possible object is
     *     {@link VideoStatus }
     *     
     */
    public VideoStatus getVideostatus() {
        return videostatus;
    }

    /**
     * Sets the value of the videostatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link VideoStatus }
     *     
     */
    public void setVideostatus(VideoStatus value) {
        this.videostatus = value;
    }

}
