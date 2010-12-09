package com.jaxws.json.codec;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.jvnet.mimepull.MIMEPart;

import com.jaxws.json.codec.decode.WSJSONPopulator;
import com.jaxws.json.feature.JSONWebService;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;

public class MessageBodyBuilder {
	final protected JSONCodec 	codec;
	private static Logger LOG	= Logger.getLogger(JSONCodec.class.getName());

	static Field jaxbObjectAccessor = null;
	
	/**
	 * Since jaxb object is private access it via reflection way. If property name renamed or on error 
	 * go with old serialization way.
	 * TODO find better way in JAX_WS 2.x
	 */
	public static boolean CAN_HANDLE_RESPONE	= false;
	
	static{
		try {
			jaxbObjectAccessor	= JAXBMessage.class.getDeclaredField("jaxbObject");
			jaxbObjectAccessor.setAccessible(true);
			CAN_HANDLE_RESPONE = true;
		} catch (Throwable e) {
			LOG.log(Level.SEVERE,"JAXBMessage reading private field jaxbObject failed.",e);
		} 
	}
	
	public MessageBodyBuilder(JSONCodec codec) {
		super();
		this.codec = codec;
		
	}

	public Message handleMessage(Packet packet,String payloadName) throws Exception{
		boolean OUT_BOUND = packet.invocationProperties.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY) != null && 
							(Boolean)packet.invocationProperties.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		SEIModel seiModel = this.codec.getSEIModel(packet);
		if(!OUT_BOUND){
			// Request message
			JavaMethod requestMethod = seiModel.getJavaMethod(new QName(seiModel.getTargetNamespace(),payloadName));
			if(requestMethod == null || !packet.invocationProperties.containsKey(JSONCodec.JSON_MAP_KEY)){
				throw new RuntimeException("Operation %s input parameter(s) not found or invalid.");
			}
			Map<String,Object> operationParameters = (Map<String, Object>) packet.invocationProperties.remove(JSONCodec.JSON_MAP_KEY);
			/*
			 * requestMethod.getMethod() == Returns implementation method.
			 * requestMethod.getSEIMethod() == Returns interface method.
			 */
			WSJSONPopulator jsonPopulator = new WSJSONPopulator(JSONCodec.globalMapKeyPattern,
					JSONCodec.globalMapValuePattern,JSONCodec.dateFormat,
					codec.getCustomSerializer()
					,(DebugTrace) packet.invocationProperties.get(JSONCodec.TRACE));
			Collection<Object> parameterObjects = new ArrayList<Object>();
			int pos = 0;
			Method seiMethod 	= requestMethod.getSEIMethod();
			for(Class<?> parameterType : seiMethod.getParameterTypes()){
	            String paramName = "arg"+pos;// Copy from runtime model builder
	            for (Annotation annotation : seiMethod.getParameterAnnotations()[pos]) {
	                if (annotation.annotationType() == WebParam.class) {
	                    WebParam webParam = (WebParam) annotation;
	                    if (webParam.name().length() > 0){
	                        paramName = webParam.name();
	                    }
	                }
	            }
	            if(!operationParameters.containsKey(paramName)){
	            	throw new RuntimeException(String.format("Request parameter %s can't be null. B.P 1.1 vilation", paramName));
	            }
	            Object val = null;
	            if(!WSJSONPopulator.isJSONPrimitive(parameterType)){
		            val = parameterType.newInstance();
		            jsonPopulator.populateObject(val,
		            		(Map<?, ?>)operationParameters.get(paramName),seiMethod.getAnnotation(JSONWebService.class), 
		            		(List<MIMEPart>) packet.invocationProperties.get(JSONCodec.MIME_ATTACHMENTS));
	            } else {
	            	val	= jsonPopulator.convert(parameterType, null, operationParameters.get(paramName), seiMethod.getAnnotation(JSONWebService.class), null);
	            }
	            parameterObjects.add(val);
	            pos++;
			}
			// TODO find better way with out using JavaMethodImpl
			List<ParameterImpl> requestParameters = ((JavaMethodImpl)requestMethod).getRequestParameters();
			if(requestParameters != null && requestParameters.size() == 1){
				ParameterImpl parameter = requestParameters.get(0);
				if(parameter.isWrapperStyle()){
					// RPC literal
					List<ParameterImpl> childParameters = ((WrapperParameter)parameter).getWrapperChildren();
					if(parameterObjects.size() != childParameters.size())
						throw new RuntimeException("Invalid count of parameters");
					CompositeStructure cs = new CompositeStructure();
					cs.values	= parameterObjects.toArray();
					cs.bridges	= new Bridge[childParameters.size()];
					for(ParameterImpl parameterChild : childParameters){
						cs.bridges[parameterChild.getIndex()] = parameterChild.getBridge();
					}
					return JAXBMessage.create(parameter.getBridge(), cs, this.codec.soapVersion);
				}else{
					// BARE
					assert(parameterObjects.size() == 1);
					return JAXBMessage.create(parameter.getBridge(), parameterObjects.toArray()[0], this.codec.soapVersion);
				}
				// TODO how to do with doc/litral 
			}else{
				// IS it possible?
			}
			return Messages.createEmpty(this.codec.soapVersion);
		}else{
			Message message = packet.getMessage();
			if(message == null){
				throw new RuntimeException("Null response message");
			}
			if(!(CAN_HANDLE_RESPONE && message instanceof JAXBMessage)){
				throw new RuntimeException("Message is not JAXBMessage. JSONCodec at now only handle JAXB messages in response.");
			}
			Map<String,Object> responseParameters = new HashMap<String, Object>();
			com.sun.xml.bind.api.CompositeStructure responseWraper = (com.sun.xml.bind.api.CompositeStructure)jaxbObjectAccessor.get(message);
			if(responseWraper != null && responseWraper.bridges.length == 1){
				responseParameters.put(responseWraper.bridges[0].getTypeReference().tagName.getLocalPart(),
						responseWraper.values[0]);
			}else{
				//One way.
				//responseParameters.put("ONE_WAY","o");
			}
			packet.invocationProperties.put(JSONCodec.JSON_MAP_KEY,responseParameters);
			return message;
		}
	}
			
}
