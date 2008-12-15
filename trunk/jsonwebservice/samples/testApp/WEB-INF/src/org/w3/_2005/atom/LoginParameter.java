
package org.w3._2005.atom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LoginParameter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LoginParameter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="accountType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Passwd" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="service" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="logintoken" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="logincaptcha" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoginParameter", propOrder = {
    "accountType",
    "email",
    "passwd",
    "service",
    "source",
    "logintoken",
    "logincaptcha"
})
public class LoginParameter {

    @XmlElement(required = true)
    protected String accountType;
    @XmlElement(name = "Email", required = true)
    protected String email;
    @XmlElement(name = "Passwd", required = true)
    protected String passwd;
    @XmlElement(required = true)
    protected String service;
    @XmlElement(required = true)
    protected String source;
    @XmlElement(required = true)
    protected String logintoken;
    @XmlElement(required = true)
    protected String logincaptcha;

    /**
     * Gets the value of the accountType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * Sets the value of the accountType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccountType(String value) {
        this.accountType = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the passwd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPasswd() {
        return passwd;
    }

    /**
     * Sets the value of the passwd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPasswd(String value) {
        this.passwd = value;
    }

    /**
     * Gets the value of the service property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getService() {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setService(String value) {
        this.service = value;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Gets the value of the logintoken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogintoken() {
        return logintoken;
    }

    /**
     * Sets the value of the logintoken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogintoken(String value) {
        this.logintoken = value;
    }

    /**
     * Gets the value of the logincaptcha property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogincaptcha() {
        return logincaptcha;
    }

    /**
     * Sets the value of the logincaptcha property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogincaptcha(String value) {
        this.logincaptcha = value;
    }

}
