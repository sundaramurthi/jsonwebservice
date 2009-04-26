package com.jaxws.json.codec;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jws.WebParam.Mode;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.Holder;

import com.jaxws.json.JaxWsJSONPopulator;
import com.jaxws.json.feature.JSONWebService;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;

public class JSONResponseBodyBuilder extends MessageBodyBuilder{
	final private boolean 		skipListWrapper;
	final private Pattern 		listMapKey;
	private static Logger LOG	= Logger.getLogger(JSONResponseBodyBuilder.class.getName());
	
	public JSONResponseBodyBuilder(SOAPVersion soapVersion,
			boolean skipListWrapper,Pattern listMapKey) {// SHould be JSON version
		super(soapVersion);
		this.skipListWrapper 	= skipListWrapper;
		this.listMapKey			= listMapKey;
	}
	
	/**
	 * 	 * Response used back as Request
	 */
	public Message createMessage(JavaMethodImpl methodImpl, 
								Map<String, Object> responseJSONObject,
								JAXBContextImpl context) {
		JSONWebService jsonService = methodImpl.getMethod().getAnnotation(JSONWebService.class);
		boolean listWrapperSkip = jsonService == null ? skipListWrapper : jsonService.skipListWrapper();
		
		Collection<Object> parameterObjects = readResponsePayLoadAsObjects(
				methodImpl.getResponseParameters(),
				responseJSONObject,context,listWrapperSkip,
						listMapKey).values();
		assert parameterObjects.size() == 1;
		ParameterImpl responseParameter = methodImpl.getResponseParameters().get(0);
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
		JSONWebService jsonService = methodImpl.getMethod().getAnnotation(JSONWebService.class);
		boolean listWrapperSkip = jsonService == null ? skipListWrapper : jsonService.skipListWrapper();
		
		//Encode as Response
		Map<String,Object> parameterObjects = readResponsePayLoadAsObjects(
											methodImpl.getResponseParameters(),
											null,null,listWrapperSkip,
													listMapKey);
		assert parameterObjects.size() == 1;
		HashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
		
		parameters.put(parameterObjects.keySet().toArray()[0].toString(), 
				getResponseBuilder(methodImpl.getResponseParameters())
				.readResponse(message, parameterObjects.values().toArray()));
			
		return parameters;
	}
	
	
	
	
	protected Map<String,Object> readResponsePayLoadAsObjects(List<ParameterImpl> parameters,
			Object requestPayloadJSON,JAXBContextImpl context,boolean skipListWrapper,
			Pattern listMapKey){
		Map<String,Object> objects	= new LinkedHashMap<String,Object>();
		 for (ParameterImpl parameter : parameters) {
			 if(parameter.isWrapperStyle()) {
				 if(requestPayloadJSON != null){
					 assert requestPayloadJSON instanceof Map;
					 requestPayloadJSON = ((Map<?,?>)requestPayloadJSON).get(parameter.getName().getLocalPart());
				 }
				 objects.putAll(
						 readResponsePayLoadAsObjects(
								 ((WrapperParameter)parameter).getWrapperChildren(),
								 requestPayloadJSON,
								 context,skipListWrapper,listMapKey
						)
				);
			 }else{
				 Class<?> type = (Class<?>)parameter.getTypeReference().type;
				 Object val	= null;
				 if(type.isEnum()){
	         		val = parameter.getTypeReference().type;//Enum.valueOf((Class<Enum>)paramChild.getTypeReference().type, "success".toUpperCase());
	         	}else{
	         		try {
						val = type.newInstance();
					} catch (Exception e) {
						LOG.throwing(JSONCodec.class.getName(), "readRequestPayLoadAsObjects", e);
					}
	         	}
				if (val != null && requestPayloadJSON != null && context != null) {
					if(JaxWsJSONPopulator.isJSONPrimitive(type) || type.isEnum()){
						CompositeStructure str = new CompositeStructure();
						str.bridges = new Bridge[1];
						str.bridges[0] = context.createBridge(parameter.getTypeReference());
						str.values = new Object[1];
						if(requestPayloadJSON instanceof Map){
							requestPayloadJSON =((Map<?,?>)requestPayloadJSON).get(parameter.getName().getLocalPart());
						}
						if(type.isEnum()){
							requestPayloadJSON = Enum.valueOf((Class<Enum>)type, requestPayloadJSON.toString());
						}
						str.values[0] = requestPayloadJSON;
						val = str;
					}else if (requestPayloadJSON instanceof Map) {
						try {
							try{
								String parameterName = parameter.getName().getLocalPart();
								Object parameterValue = ((Map<?, ?>) requestPayloadJSON).get(parameterName);
								if(parameterValue instanceof Map){
									new JaxWsJSONPopulator(context,skipListWrapper,listMapKey).populateObject(val,(Map<?, ?>)parameterValue	);
								}else if(skipListWrapper && parameterValue instanceof List){
									HashMap<String,Object> map = new HashMap<String, Object>();
									String warperName = getWarpedListName(val.getClass());
									if(warperName != null){
										map.put(warperName, parameterValue);
										new JaxWsJSONPopulator(context,skipListWrapper,listMapKey).populateObject(val,map);
									}
								}
							}catch(Throwable th){
								th.printStackTrace();
							}
							if(parameter.getMode() == Mode.OUT){
								CompositeStructure str = new CompositeStructure();
								str.bridges = new Bridge[1];
								str.bridges[0] = context.createBridge(parameter.getTypeReference());
								str.values = new Object[1];
								str.values[0] = val;
								val = str;
							}
						} catch (Exception e) {
							LOG.throwing(JSONCodec.class.getName(), "Value population failed for "
									+ parameter.getPartName(), e);
						}
					} else{
						throw new Error("Unhandled type "+type);
					}
				}else{
					val = new Holder(val);
				}
				objects.put(parameter.getName().getLocalPart(),val);
			 }
		 }
		return objects; 
	}
	
	public static String getWarpedListName(Class<? extends Object> clazz){
    	// JSON web service strip List wrapper  parameter 
        //    IF number of properties == 1 and Its collection and wrapper disable
        //		then
        //		  pass on list vale
        try {
            Method[] methods = clazz.getDeclaredMethods();
    		if(methods.length == 1 && methods[0].getParameterTypes().length == 0 && 
    				methods[0].getReturnType().equals(List.class)){
    			if(methods[0].getName().startsWith("get")){
					String charStart = ""+methods[0].getName().charAt(3);
					return charStart.toLowerCase()+methods[0].getName().substring(4);
				}
    		}
        } catch (Throwable e) {/*Don't mind*/}
        return null;
        // End
    }
}
