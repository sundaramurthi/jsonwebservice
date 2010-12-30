package com.jaxws.json.codec;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.handler.MessageContext;

import org.jvnet.mimepull.MIMEPart;

import com.jaxws.json.codec.decode.WSJSONPopulator;
import com.jaxws.json.feature.JSONWebService;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPart;
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

	@SuppressWarnings("unchecked")
	public Message handleMessage(Packet packet,String payloadName) throws Exception{
		Map<String, Object> invocationProperties = packet.invocationProperties;
		boolean OUT_BOUND = invocationProperties.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY) != null && 
							(Boolean)invocationProperties.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		SEIModel 			seiModel 	= this.codec.getSEIModel(packet);
		JAXBContextImpl 	context 	= (JAXBContextImpl)seiModel.getJAXBContext();
		Style style = seiModel.getPort().getBinding().getStyle();
		if(!OUT_BOUND){
			// Request message
			WSDLBoundOperation operation 	= seiModel.getPort().getBinding().getOperation(seiModel.getTargetNamespace(),payloadName);
			if(operation == null || !packet.invocationProperties.containsKey(JSONCodec.JSON_MAP_KEY)){
				throw new RuntimeException("Operation %s input parameter(s) not found or invalid.");
			}
			JavaMethod 			javaMethod 	= seiModel.getJavaMethod(operation.getName());
			Method 				seiMethod 	= javaMethod.getSEIMethod();
			JSONWebService	jsonwebService	= javaMethod.getMethod().getAnnotation(JSONWebService.class);
			// Put codec specific properties in invoke
			invocationProperties.put(JSONCodec.globalMapKeyPattern_KEY, (jsonwebService == null || jsonwebService.listMapKey().isEmpty())?
					JSONCodec.globalMapKeyPattern : Pattern.compile(jsonwebService.listMapKey()));
			//
			
			Map<String,Object> 	operationParameters = (Map<String, Object>) invocationProperties.remove(JSONCodec.JSON_MAP_KEY);
			
			WSJSONPopulator 	jsonPopulator 		= new WSJSONPopulator((Pattern)invocationProperties.get(JSONCodec.globalMapKeyPattern_KEY),
					JSONCodec.globalMapValuePattern,JSONCodec.dateFormat,
					codec.getCustomSerializer()
					,(DebugTrace) packet.invocationProperties.get(JSONCodec.TRACE));
			
			Object[]			parameterObjects	= new Object[operation.getInParts().size()];
			for(Map.Entry<String, WSDLPart> part : operation.getInParts().entrySet()){
				Class<?> 		parameterType 	= context.getGlobalType(part.getValue().getDescriptor().name()).jaxbType;
				if(!operationParameters.containsKey(part.getKey())){
	            	throw new RuntimeException(String.format("Request parameter %s can't be null. B.P 1.1 vilation", part.getKey()));
	            }
				
				Object val = null;
	            if(!WSJSONPopulator.isJSONPrimitive(parameterType)){
		            val = parameterType.newInstance();
		            jsonPopulator.populateObject(val,
		            		(Map<String,Object>)operationParameters.get(part.getKey()),jsonwebService, 
		            		(List<MIMEPart>) packet.invocationProperties.get(JSONCodec.MIME_ATTACHMENTS));
	            } else {
	            	val	= jsonPopulator.convert(parameterType, null, operationParameters.get(part.getKey()),
	            			seiMethod != null ? seiMethod.getAnnotation(JSONWebService.class) : null, null);
	            }
	            parameterObjects[part.getValue().getIndex()] = val;
			}
			
			// TODO find better way with out using JavaMethodImpl
			List<ParameterImpl> requestParameters = ((JavaMethodImpl)javaMethod).getRequestParameters();
			if(requestParameters != null && requestParameters.size() == 1){
				ParameterImpl parameter = requestParameters.get(0);
				if(parameter.isWrapperStyle()){
					// RPC literal
					List<ParameterImpl> childParameters = ((WrapperParameter)parameter).getWrapperChildren();
					if(parameterObjects.length != childParameters.size())
						throw new RuntimeException("Invalid count of parameters");
					Object	obj	= null;
					if(style == Style.RPC){
						CompositeStructure cs = new CompositeStructure();
						cs.values	= parameterObjects;
						cs.bridges	= new Bridge[childParameters.size()];
						for(ParameterImpl parameterChild : childParameters){
							cs.bridges[parameterChild.getIndex()] = parameterChild.getBridge();
						}
						obj	= cs;
					}else{
						Class<?> type = (Class<?>)parameter.getBridge().getTypeReference().type;
						obj	 = type.newInstance();
						for(ParameterImpl parameterChild : childParameters){
							type.getField(parameterChild.getPartName()).set(obj,
									parameterObjects[parameterChild.getIndex()]);
						}
					}
					return JAXBMessage.create(parameter.getBridge(), obj, this.codec.soapVersion);
				}else{
					// BARE
					assert(parameterObjects.length == 1);
					return JAXBMessage.create(parameter.getBridge(), parameterObjects[0], this.codec.soapVersion);
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
			if(style == Style.RPC ){
				com.sun.xml.bind.api.CompositeStructure responseWraper = (com.sun.xml.bind.api.CompositeStructure)jaxbObjectAccessor.get(message);
				if(responseWraper != null){
					for(int index = 0; index < responseWraper.bridges.length; index++){
						responseParameters.put(responseWraper.bridges[index].getTypeReference().tagName.getLocalPart(),
							responseWraper.values[index]);
					}
				}
			}else{
				// Docunemnt
				Object object = jaxbObjectAccessor.get(message);
				for(Field field : object.getClass().getFields()){
					responseParameters.put(field.getName(), field.get(object));
				}
			}
			packet.invocationProperties.put(JSONCodec.JSON_MAP_KEY, responseParameters);
			return message;
		}
	}
			
}
