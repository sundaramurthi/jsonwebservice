package com.jaxws.json.codec;

import javax.xml.namespace.QName;

import com.jaxws.json.feature.JSONWebServiceFeature;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.binding.WebServiceFeatureList;

/**
 * @author Sundaramurthi
 * @version 1.0
 * @mail sundaramurthis@gmail.com
 */
public class JSONBindingID extends BindingID {

    public static final String JSON_BINDING = "http://jsonplugin.googlecode.com/json/";
    public static final String NS_WSDL_JSON = "http://schemas.jsonsoap.org/wsdl/json/";
    public static final QName NS_JSON_BINDING 	= new QName(NS_WSDL_JSON,"binding");
    public static final QName QNAME_ADDRESS 	= new QName(NS_WSDL_JSON, "address");
    public static final QName QNAME_OPERATION 	= new QName(NS_WSDL_JSON, "operation");
    
    public static final JSONBindingID JSON_HTTP = new JSONBindingID();
    
    public SOAPVersion getSOAPVersion() {
        return SOAPVersion.SOAP_11;
    }

    public @NotNull Codec createEncoder(@NotNull WSBinding binding) {
        return new JSONCodec(binding);
    }

    public String toString() {
        return JSON_BINDING;
    }

    @Override
    public boolean canGenerateWSDL() {
        return true;
    }

	/*@Override
	public WebServiceFeatureList createBuiltinFeatureList() {
		WebServiceFeatureList featureList = super.createBuiltinFeatureList();
		featureList.add(new JSONWebServiceFeature());
		return featureList;
	}
*/
}
