package com.jaxws.json.codec.decode;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.Holder;

import org.jvnet.mimepull.MIMEPart;

import com.jaxws.json.builder.BodyBuilder;
import com.jaxws.json.codec.DebugTrace;
import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.MessageBodyBuilder;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.model.JavaMethodImpl;

public class JSONRequestBodyBuilder extends MessageBodyBuilder{
	
	public JSONRequestBodyBuilder(JSONCodec codec) {
		super(codec);
	}
	
	/**
	 * @param javaMethod
	 * @param requestJSONObject
	 * @param context
	 * @param attachments
	 * @param traceEnabled
	 * @param traceLog
	 * @return
	 * @deprecated
	 */
	public Message createMessage(JavaMethod javaMethod, 
								Map<String, Object> requestJSONObject,
								JAXBContextImpl context,
								List<MIMEPart> attachments,
								boolean traceEnabled,DebugTrace traceLog) {
		Pattern listMapKey		= getListMapKey(javaMethod);
		Pattern listMapValue	= getListMapValue(javaMethod);
		
		Collection<Object> parameterObjects = readParameterAsObjects(
						((JavaMethodImpl)javaMethod).getRequestParameters(),
						requestJSONObject,
						context,listMapKey,listMapValue,attachments,traceEnabled,traceLog).values();
		BodyBuilder bodyBuilder = getRequestBodyBuilder(((JavaMethodImpl)javaMethod)
				.getRequestParameters());
		return bodyBuilder.createMessage(parameterObjects.toArray());
	}

	/**
	 * Called by easier testing application
	 * Response used back as Request
	 * @throws XMLStreamException 
	 * @throws JAXBException 
	 * @deprecated
	 */
	public Map<String,Object> createMap(JavaMethod methodImpl,Message message,List<MIMEPart> attachments,
			boolean traceEnabled,DebugTrace traceLog) throws JAXBException, XMLStreamException{
		
		Pattern listMapKey		= getListMapKey(methodImpl);
		Pattern listMapValue	= getListMapValue(methodImpl);
		
		Map<String,Object> parameterObjects = readParameterAsObjects(
				((JavaMethodImpl)methodImpl).getRequestParameters(),
				null,null,listMapKey,listMapValue,attachments, traceEnabled,traceLog);
		getResponseBuilder(((JavaMethodImpl)methodImpl).getRequestParameters()).readResponse(message, parameterObjects.values().toArray());
		HashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
		//Remove Holder objects
		for(String key :parameterObjects.keySet()){
			if(parameterObjects.get(key) instanceof Holder){
				parameters.put(key, ((Holder<?>)parameterObjects.get(key)).value);
			}else{
				parameters.put(key,parameterObjects.get(key));
			}
		}
		return parameters;
	}
}
