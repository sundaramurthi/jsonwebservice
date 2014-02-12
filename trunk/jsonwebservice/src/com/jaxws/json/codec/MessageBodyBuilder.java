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
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaxws.json.JSONMessage;
import com.jaxws.json.codec.decode.WSJSONPopulator;
import com.jaxws.json.codec.encode.JSONEncoder;
import com.jaxws.json.feature.JSONWebService;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;

public class MessageBodyBuilder {
	final protected JSONCodec 	codec;
	private static Logger LOG	= Logger.getLogger(JSONCodec.class.getName());

	static Field jaxbObjectAccessor = null;
	
	static Method javaMethodAccessor = null;
	
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
		
		try{
			javaMethodAccessor = SEIModel.class.getMethod("getJavaMethodForWsdlOperation",QName.class);
		}catch(Throwable th){
			LOG.log(Level.INFO,"Old version of metro used. java method accessed using \"getJavaMethod\" operation from sei model");
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
		Style style = seiModel.getPort().getBinding().getStyle();
		if(!OUT_BOUND){
			// Request message
			// TODO when operation <input><json:body contains different namespace than port level name space bellow call fails to identify operation. 
			WSDLBoundOperation operation 	= seiModel.getPort().getBinding().getOperation(seiModel.getTargetNamespace(),payloadName);
			if(operation == null || !packet.invocationProperties.containsKey(JSONCodec.JSON_MAP_KEY)){
				throw new RuntimeException("Operation %s input parameter(s) not found or invalid.");
			}
			JavaMethod 			javaMethod 	= seiModel.getJavaMethod(operation.getName());
			if(javaMethod == null && javaMethodAccessor != null){
				javaMethod = (JavaMethod) javaMethodAccessor.invoke(seiModel, operation.getName());
			}else{
				// TODO iterate all method and find
			}
			Method 				seiMethod 	= javaMethod.getSEIMethod();
			JSONWebService	jsonwebService	= javaMethod.getMethod().getAnnotation(JSONWebService.class);
			// Put codec specific properties in invoke
			invocationProperties.put(JSONCodec.globalMapKeyPattern_KEY, (jsonwebService == null || jsonwebService.listMapKey().isEmpty())?
					JSONCodec.globalMapKeyPattern : Pattern.compile(jsonwebService.listMapKey()));
			invocationProperties.put(JSONCodec.globalMapValuePattern_KEY, (jsonwebService == null || jsonwebService.listMapValue().isEmpty())?
					JSONCodec.globalMapValuePattern : Pattern.compile(jsonwebService.listMapValue()));
			
			return new JSONMessage(null, operation, (Map<String, Object>) invocationProperties.remove(JSONCodec.JSON_MAP_KEY),  new WSJSONPopulator((Pattern)invocationProperties.get(JSONCodec.globalMapKeyPattern_KEY),
					(Pattern)invocationProperties.get(JSONCodec.globalMapValuePattern_KEY),JSONCodec.dateFormat,
					codec.getCustomSerializer()
					,(DebugTrace)invocationProperties.get(JSONCodec.TRACE)));
		}else{
			Message message;
			try{
				//For old version compact
				message = (Message) packet.getClass().getMethod("getInternalMessage").invoke(packet);
			}catch(Throwable th){
				message = packet.getMessage();
			}
			if(message == null){
				throw new RuntimeException("Null response message");
			}
			List<ParameterImpl> responseParameters 	= (List<ParameterImpl>) invocationProperties.remove(JSONEncoder.RESPONSEPARAMETERS);
			Map<String,Object> 	responseParametersMap = new HashMap<String, Object>();
			if(CAN_HANDLE_RESPONE && message instanceof com.sun.xml.ws.message.jaxb.JAXBMessage){
				if(style == Style.RPC ){
					Object jaxbObject = jaxbObjectAccessor.get(message);
					try{
						Object bridges[] = (Object[]) jaxbObject.getClass().getDeclaredField("bridges").get(jaxbObject);
						Object values[]	=  (Object[]) jaxbObject.getClass().getDeclaredField("values").get(jaxbObject);
						
						for(int index = 0; index < bridges.length; index++){
							Object typeRef;
							try{
								typeRef = bridges[index].getClass().getMethod("getTypeInfo").invoke(bridges[index]);
							}catch(Throwable th){
								typeRef = bridges[index].getClass().getMethod("getTypeReference").invoke(bridges[index]);
							}
							responseParametersMap.put(((QName)typeRef.getClass().getDeclaredField("tagName").get(typeRef)).getLocalPart(),
									values[index]);
						}
						
					}catch(Throwable th){
						responseParametersMap.put(message.getPayloadLocalPart(), jaxbObject);
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
