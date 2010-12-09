package com.jaxws.json.codec;

import javax.xml.namespace.QName;

import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;

/**
 * @author ssaminathan
 * Utility methods used in service.
 */
public final class JSONWebserviceUtil {

	/**
	 * Utility method to find java method.
	 * @param seiModel
	 * @param payloadName
	 * @return
	 */
	public static final JavaMethod getJavaMethod(SEIModel seiModel,String payloadName){
		// Find using QName.  IN operations get success here.
		JavaMethod methodImpl = seiModel.getJavaMethod(new QName(seiModel.getTargetNamespace(),payloadName));
		if(methodImpl != null){
			return methodImpl;
		}
		// Find using java method response.
		for(JavaMethod m : seiModel.getJavaMethods()){
			if(((!m.getMEP().isOneWay()) &&  m.getResponsePayloadName().getLocalPart().equals(payloadName)) ||
					m.getOperationName().equals(payloadName) ){
				return m;
			}
		}
		// Null response
		return methodImpl;
	}
}
