package com.jaxws.json.codec;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.handler.MessageContext;

import org.jvnet.mimepull.MIMEPart;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaxws.json.codec.decode.WSJSONPopulator;
import com.jaxws.json.codec.encode.JSONEncoder;
import com.jaxws.json.feature.JSONWebService;
import com.jaxws.json.packet.handler.Encoder;
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
import com.sun.xml.ws.model.wsdl.WSDLBoundOperationImpl;
import com.sun.xml.ws.util.ServiceFinder;

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
			invocationProperties.put(JSONCodec.globalMapValuePattern_KEY, (jsonwebService == null || jsonwebService.listMapValue().isEmpty())?
					JSONCodec.globalMapValuePattern : Pattern.compile(jsonwebService.listMapValue()));
			//
			
			Map<String,Object> 	operationParameters = (Map<String, Object>) invocationProperties.remove(JSONCodec.JSON_MAP_KEY);
			
			WSJSONPopulator 	jsonPopulator 		= new WSJSONPopulator((Pattern)invocationProperties.get(JSONCodec.globalMapKeyPattern_KEY),
					(Pattern)invocationProperties.get(JSONCodec.globalMapValuePattern_KEY),JSONCodec.dateFormat,
					codec.getCustomSerializer()
					,(DebugTrace)invocationProperties.get(JSONCodec.TRACE));
			
			Object[]			parameterObjects	= new Object[operation.getInParts().size()];
			Class<?>[] 			parameterTypes 		= seiMethod.getParameterTypes();// This parameter types not trustable in case of HOLDER
			for(Map.Entry<String, WSDLPart> part : operation.getInParts().entrySet()){
				Class<?> 		parameterType;
				if(context.getGlobalType(part.getValue().getDescriptor().name()) != null)
					parameterType = context.getGlobalType(part.getValue().getDescriptor().name()).jaxbType;
				else
					/*
					 * This parameter types not trustable in case of HOLDER
					 * We can't find it in global type once user extend simple type and use it as method parameter. 
					 * E.g String255 extended from String
					 */
					parameterType = parameterTypes[part.getValue().getIndex()];
				if(!operationParameters.containsKey(part.getKey())){
	            	throw new RuntimeException(String.format("Request parameter %s can't be null. B.P 1.1 vilation", part.getKey()));
	            }
				
				Object val = null;
	            if(!WSJSONPopulator.isJSONPrimitive(parameterType)){
	            	val = jsonPopulator.populateObject(jsonPopulator.getNewInstance(parameterType),
		            		(Map<String,Object>)operationParameters.get(part.getKey()),jsonwebService, 
		            		(List<MIMEPart>) invocationProperties.get(JSONCodec.MIME_ATTACHMENTS));
	            } else {
	            	val	= jsonPopulator.convert(parameterType, null, operationParameters.get(part.getKey()),
	            			seiMethod != null ? seiMethod.getAnnotation(JSONWebService.class) : null, null);
	            }
	            parameterObjects[part.getValue().getIndex()] = val;
			}
			
			// TODO find better way with out using JavaMethodImpl
			List<ParameterImpl> requestParameters = ((JavaMethodImpl)javaMethod).getRequestParameters();
			List<ParameterImpl> responseParameters = ((JavaMethodImpl)javaMethod).getResponseParameters();
			invocationProperties.put(JSONEncoder.RESPONSEPARAMETERS, responseParameters);
			
			if(operation instanceof WSDLBoundOperationImpl && ((WSDLBoundOperationImpl)operation).getOutputMimeTypes().size() > 0){
				// Use only one in case of multipart use attachment 
				String mimeType = String.valueOf(((WSDLBoundOperationImpl)operation).getOutputMimeTypes().values().toArray()[0]);
				for (Encoder handler : ServiceFinder.find(Encoder.class)) {
					if(mimeType.equals(handler.mimeContent())){
						invocationProperties.put(JSONCodec.ENCODER, handler);
						break;
					}
				}
			}
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
						obj	 = jsonPopulator.getNewInstance(type);
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
			List<ParameterImpl> responseParameters 	= (List<ParameterImpl>) invocationProperties.remove(JSONEncoder.RESPONSEPARAMETERS);
			Map<String,Object> 	responseParametersMap = new HashMap<String, Object>();
			if(CAN_HANDLE_RESPONE && message instanceof JAXBMessage){
				if(style == Style.RPC ){
					com.sun.xml.bind.api.CompositeStructure responseWraper = (com.sun.xml.bind.api.CompositeStructure)jaxbObjectAccessor.get(message);
					if(responseWraper != null){
						for(int index = 0; index < responseWraper.bridges.length; index++){
							responseParametersMap.put(responseWraper.bridges[index].getTypeReference().tagName.getLocalPart(),
								responseWraper.values[index]);
						}
					}
				}else{
					// Docunemnt
					Object object = jaxbObjectAccessor.get(message);
					for(Field field : object.getClass().getFields()){
						responseParametersMap.put(field.getName(), field.get(object));
					}
				}
			} else if(responseParameters != null && responseParameters.size() == 1){
				// object deserialized. might perform bad.
				ParameterImpl 		parameter 	= responseParameters.get(0);
				Iterator<Node> 		paramBody 	= message.readAsSOAPMessage().getSOAPBody().getChildElements(parameter.getName());
				if(paramBody.hasNext()){
					Element responseElm = (Element)paramBody.next();
					if(parameter.isWrapperStyle()){
						List<ParameterImpl> children = ((WrapperParameter)parameter).getWrapperChildren();
						for(ParameterImpl param : children){
							NodeList paramElm = responseElm.getElementsByTagNameNS(param.getName().getNamespaceURI(), 
									param.getPartName());
							if(paramElm.getLength() == 1){
								responseParametersMap.put(param.getPartName(),
										param.getBridge().unmarshal(paramElm.item(0)));
							}
						}
					}else{
						responseParametersMap.put(parameter.getPartName(),
								parameter.getBridge().unmarshal(responseElm));
					}
				}
			}
			invocationProperties.put(JSONCodec.JSON_MAP_KEY, responseParametersMap);
			return message;
		}
	}
			
}
