package com.jaxws.json.codec;

import javax.xml.namespace.QName;

public interface JSONConstants {

    public static final String NS_WSDL_JSON =
        "http://schemas.jsonsoap.org/wsdl/json/";

    
    public static final String NS_SOAP_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/";

    // other URIs
    public final String URI_JSON_TRANSPORT_HTTP =
        "http://schemas.xmlsoap.org/soap/http";

    // QNames
    public static final QName QNAME_ADDRESS =
        new QName(NS_WSDL_JSON, "address");
   
    public static final QName QNAME_BINDING =
        new QName(NS_WSDL_JSON, "binding");
    public static final QName QNAME_BODY = new QName(NS_WSDL_JSON, "body");
    public static final QName QNAME_FAULT = new QName(NS_WSDL_JSON, "fault");
    public static final QName QNAME_HEADER = new QName(NS_WSDL_JSON, "header");
   
    public static final QName QNAME_OPERATION =
        new QName(NS_WSDL_JSON, "operation");
    public static final QName QNAME_SOAP12OPERATION =
        new QName(NS_WSDL_JSON, "operation"); 
    public static final QName QNAME_MUSTUNDERSTAND =
        new QName(NS_WSDL_JSON, "mustUnderstand");


    // Copy from default package of MIMEContants
    
    // namespace URIs
    public static String NS_WSDL_MIME = "http://schemas.xmlsoap.org/wsdl/mime/";

    // QNames
    public static QName QNAME_CONTENT = new QName(NS_WSDL_MIME, "content");
    public static QName QNAME_MULTIPART_RELATED =
        new QName(NS_WSDL_MIME, "multipartRelated");
    public static QName QNAME_PART = new QName(NS_WSDL_MIME, "part");
    public static QName QNAME_MIME_XML = new QName(NS_WSDL_MIME, "mimeXml");
}

