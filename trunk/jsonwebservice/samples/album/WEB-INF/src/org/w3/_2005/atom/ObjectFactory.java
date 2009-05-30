
package org.w3._2005.atom;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.w3._2005.atom package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Feed_QNAME = new QName("http://www.w3.org/2005/Atom", "feed");
    private final static QName _Fault_QNAME = new QName("http://www.w3.org/2005/Atom", "fault");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.w3._2005.atom
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link UIElement }
     * 
     */
    public UIElement createUIElement() {
        return new UIElement();
    }

    /**
     * Create an instance of {@link UIElements }
     * 
     */
    public UIElements createUIElements() {
        return new UIElements();
    }

    /**
     * Create an instance of {@link LinkType }
     * 
     */
    public LinkType createLinkType() {
        return new LinkType();
    }

    /**
     * Create an instance of {@link Crediential }
     * 
     */
    public Crediential createCrediential() {
        return new Crediential();
    }

    /**
     * Create an instance of {@link EntryType }
     * 
     */
    public EntryType createEntryType() {
        return new EntryType();
    }

    /**
     * Create an instance of {@link LoginParameter }
     * 
     */
    public LoginParameter createLoginParameter() {
        return new LoginParameter();
    }

    /**
     * Create an instance of {@link LoginFault }
     * 
     */
    public LoginFault createLoginFault() {
        return new LoginFault();
    }

    /**
     * Create an instance of {@link LoginResponse }
     * 
     */
    public LoginResponse createLoginResponse() {
        return new LoginResponse();
    }

    /**
     * Create an instance of {@link FeedType }
     * 
     */
    public FeedType createFeedType() {
        return new FeedType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FeedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2005/Atom", name = "feed")
    public JAXBElement<FeedType> createFeed(FeedType value) {
        return new JAXBElement<FeedType>(_Feed_QNAME, FeedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2005/Atom", name = "fault")
    public JAXBElement<LoginFault> createFault(LoginFault value) {
        return new JAXBElement<LoginFault>(_Fault_QNAME, LoginFault.class, null, value);
    }

}
