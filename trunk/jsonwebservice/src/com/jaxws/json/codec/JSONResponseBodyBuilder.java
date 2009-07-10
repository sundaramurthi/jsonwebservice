package com.jaxws.json.codec;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;

public class JSONResponseBodyBuilder extends MessageBodyBuilder{
	public JSONResponseBodyBuilder(SOAPVersion soapVersion) {// SHould be JSON version
		super(soapVersion);
	}
	
	/**
	 * 	 * Response used back as Request
	 */
	public Message createMessage(JavaMethodImpl methodImpl, 
								Map<String, Object> responseJSONObject,
								JAXBContextImpl context) {
		Pattern listMapKey		= JSONCodec.getListMapKey(methodImpl);
		Pattern listMapValue	= JSONCodec.getListMapValue(methodImpl);
		boolean listWrapperSkip = JSONCodec.isListWarperSkip(methodImpl);
		
		Collection<Object> parameterObjects = readParameterAsObjects(
				methodImpl.getResponseParameters(),
				responseJSONObject,context,listWrapperSkip,
						listMapKey,listMapValue,JSONCodec.dateFormatType).values();
		ParameterImpl responseParameter = methodImpl.getResponseParameters().get(0);
		
		if(parameterObjects.size() ==0){
			//VOID response
			return JAXBMessage.create(responseParameter.getBridge(), null, soapVersion);
		}
		
		if(responseParameter instanceof WrapperParameter &&
					((WrapperParameter)responseParameter).getTypeReference().type!= com.sun.xml.bind.api.CompositeStructure.class){
			WrapperParameter responseWarper = (WrapperParameter)responseParameter;
			return JAXBMessage.create(responseWarper.getWrapperChildren().get(0).getBridge(),
					responseWarper.getWrapperChildren().get(0), soapVersion);
		}else{
			return JAXBMessage.create(responseParameter.getBridge(), parameterObjects.toArray()[0], soapVersion);
		}
	}

	/**
	 * This is normal standard call
	 * @throws XMLStreamException 
	 * @throws JAXBException 
	 */
	public Map<String,Object> createMap(JavaMethodImpl methodImpl,Message message) throws JAXBException, XMLStreamException{
		
		Pattern listMapKey		= JSONCodec.getListMapKey(methodImpl);
		Pattern listMapValue	= JSONCodec.getListMapValue(methodImpl);
		boolean listWrapperSkip = JSONCodec.isListWarperSkip(methodImpl);
		
		//Encode as Response
		
		Map<String,Object> parameterObjects = readParameterAsObjects(
											methodImpl.getResponseParameters(),
											null,null,listWrapperSkip,
													listMapKey,listMapValue,JSONCodec.dateFormatType);
		assert parameterObjects.size() <= 1;
		HashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
		if(!parameterObjects.keySet().isEmpty()){	
			parameters.put(parameterObjects.keySet().toArray()[0].toString(), 
					getResponseBuilder(methodImpl.getResponseParameters())
					.readResponse(message, parameterObjects.values().toArray()));
		}
		return parameters;
	}
	
}
