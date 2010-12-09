package com.jaxws.json.codec.encode;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.jvnet.mimepull.MIMEPart;

import com.jaxws.json.codec.DebugTrace;
import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.MessageBodyBuilder;
import com.sun.istack.NotNull;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;

public class JSONResponseBodyBuilder extends MessageBodyBuilder{
	private JSONCodec codec;
	public JSONResponseBodyBuilder(@NotNull JSONCodec codec) {
		super(codec);
		this.codec = codec;
	}
	
	/**
	 * 	 * Response used back as Request
	 * @deprecated
	 */
	public Message createMessage(JavaMethod methodImpl, 
								Map<String, Object> responseJSONObject,
								JAXBContextImpl context,
								List<MIMEPart> attachments,
								boolean traceEnabled,DebugTrace traceLog) {
		Pattern listMapKey		= getListMapKey(methodImpl);
		Pattern listMapValue	= getListMapValue(methodImpl);
		
		Collection<Object> parameterObjects = readParameterAsObjects(
				((JavaMethodImpl)methodImpl).getResponseParameters(),
				responseJSONObject,context,listMapKey,listMapValue,attachments, traceEnabled,traceLog).values();
		ParameterImpl responseParameter = ((JavaMethodImpl)methodImpl).getResponseParameters().get(0);
		
		if(parameterObjects.size() ==0){
			//VOID response
			return JAXBMessage.create(responseParameter.getBridge(), null, codec.soapVersion);
		}
		
		if(responseParameter instanceof WrapperParameter &&
					((WrapperParameter)responseParameter).getTypeReference().type!= com.sun.xml.bind.api.CompositeStructure.class){
			WrapperParameter responseWarper = (WrapperParameter)responseParameter;
			return JAXBMessage.create(responseWarper.getWrapperChildren().get(0).getBridge(),
					responseWarper.getWrapperChildren().get(0), codec.soapVersion);
		}else{
			return JAXBMessage.create(responseParameter.getBridge(), parameterObjects.toArray()[0], codec.soapVersion);
		}
	}

	/**
	 * This is normal standard call
	 * @throws XMLStreamException 
	 * @throws JAXBException 
	 * @deprecated
	 */
	public Map<String,Object> createMap(JavaMethod methodImpl,Message message,
			List<MIMEPart> attachments,
			boolean traceEnabled,DebugTrace traceLog) throws JAXBException, XMLStreamException{
		
		Pattern listMapKey		= getListMapKey(methodImpl);
		Pattern listMapValue	= getListMapValue(methodImpl);
		//Encode as Response
		Map<String,Object> parameterObjects = readParameterAsObjects(
											((JavaMethodImpl)methodImpl).getResponseParameters(),
											null,null,listMapKey,listMapValue,attachments,traceEnabled,traceLog);
		assert parameterObjects.size() <= 1;
		HashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
		if(!parameterObjects.keySet().isEmpty()){	
			parameters.put(parameterObjects.keySet().toArray()[0].toString(), 
					getResponseBuilder(((JavaMethodImpl)methodImpl).getResponseParameters())
					.readResponse(message, parameterObjects.values().toArray()));
		}
		return parameters;
	}
	
}
