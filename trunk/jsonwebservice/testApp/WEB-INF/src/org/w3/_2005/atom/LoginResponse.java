
package org.w3._2005.atom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LoginResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LoginResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Url" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Error" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CaptchaToken" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CaptchaUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoginResponse", propOrder = {
    "url",
    "error",
    "captchaToken",
    "captchaUrl"
})
public class LoginResponse {

    @XmlElement(name = "Url", required = true)
    protected String url;
    @XmlElement(name = "Error", required = true)
    protected String error;
    @XmlElement(name = "CaptchaToken", required = true)
    protected String captchaToken;
    @XmlElement(name = "CaptchaUrl", required = true)
    protected String captchaUrl;

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the error property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setError(String value) {
        this.error = value;
    }

    /**
     * Gets the value of the captchaToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCaptchaToken() {
        return captchaToken;
    }

    /**
     * Sets the value of the captchaToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCaptchaToken(String value) {
        this.captchaToken = value;
    }

    /**
     * Gets the value of the captchaUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCaptchaUrl() {
        return captchaUrl;
    }

    /**
     * Sets the value of the captchaUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCaptchaUrl(String value) {
        this.captchaUrl = value;
    }

}
