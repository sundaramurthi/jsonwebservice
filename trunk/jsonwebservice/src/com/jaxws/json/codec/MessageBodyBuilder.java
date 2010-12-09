package com.jaxws.json.codec;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;

import org.jvnet.mimepull.MIMEPart;

import com.jaxws.json.builder.BodyBuilder;
import com.jaxws.json.builder.ResponseBuilder;
import com.jaxws.json.builder.ValueGetterFactory;
import com.jaxws.json.builder.ValueSetter;
import com.jaxws.json.builder.ValueSetterFactory;
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
			e.printStackTrace();
		} 
	}
	
	public MessageBodyBuilder(JSONCodec codec) {
		super();
		this.codec = codec;
		
	}

	protected final BodyBuilder getRequestBodyBuilder(List<ParameterImpl> parameters) {
		BodyBuilder bodyBuilder = null;
		for (ParameterImpl param : parameters) {
			com.jaxws.json.builder.ValueGetter getter = com.jaxws.json.builder.ValueGetterFactory.SYNC
					.get(param);
			switch (param.getInBinding().kind) {
			case BODY:
				if (param.isWrapperStyle()) {
					WrapperParameter wrappedParam = (WrapperParameter) param;
					if (param.getParent().getBinding().isRpcLit())
						bodyBuilder = new BodyBuilder.RpcLit(wrappedParam,
								codec.soapVersion, ValueGetterFactory.SYNC);
					else
						bodyBuilder = new BodyBuilder.DocLit(wrappedParam,
								codec.soapVersion, ValueGetterFactory.SYNC);
				} else {
					bodyBuilder = new BodyBuilder.Bare(param, codec.soapVersion,
							getter);
				}
				break;
			/*
			 * case HEADER: fillers.add(new MessageFiller.Header(
			 * param.getIndex(), param.getBridge(), getter )); break; case
			 * ATTACHMENT:
			 * fillers.add(MessageFiller.AttachmentFiller.createAttachmentFiller
			 * (param, getter)); break;
			 */
			case UNBOUND:
				break;
			default:
				throw new AssertionError(); // impossible
			}
		}

		if (bodyBuilder == null) {
			// no parameter binds to body. we create an empty message
			switch (codec.soapVersion) {
			case SOAP_11:
				bodyBuilder = BodyBuilder.EMPTY_SOAP11;
				break;
			case SOAP_12:
				bodyBuilder = BodyBuilder.EMPTY_SOAP12;
				break;
			default:
				throw new AssertionError();
			}
		}
		return bodyBuilder;
	}
	
	protected final ResponseBuilder getResponseBuilder(List<ParameterImpl> parameters) {
		List<ResponseBuilder> builders = new ArrayList<ResponseBuilder>();
		for (ParameterImpl param : parameters) {
			ValueSetter setter;
			switch (param.getOutBinding().kind) {
			case BODY:
				if (param.isWrapperStyle()) {
					WrapperParameter wParam = (WrapperParameter) param;
					if (param.getParent().getBinding().isRpcLit())
						builders.add(new ResponseBuilder.RpcLit(wParam,
								ValueSetterFactory.SYNC));
					else
						builders.add(new ResponseBuilder.DocLit(wParam,
								ValueSetterFactory.SYNC));
				} else {
					setter = ValueSetterFactory.SYNC.get(param);
					builders.add(new ResponseBuilder.Body(param.getBridge(),
							setter));
				}

				break;
			case HEADER:
				setter = ValueSetterFactory.SYNC.get(param);
				builders.add(new ResponseBuilder.Header(codec.soapVersion, param,
						setter));
				break;
			case ATTACHMENT:
				setter = ValueSetterFactory.SYNC.get(param);
				builders.add(ResponseBuilder.AttachmentBuilder
						.createAttachmentBuilder(param, setter));
				break;
			case UNBOUND:
				setter = ValueSetterFactory.SYNC.get(param);
				builders.add(new ResponseBuilder.NullSetter(setter,
						ResponseBuilder.getVMUninitializedValue(param
								.getTypeReference().type)));
				break;
			default:
				throw new AssertionError();
			}
		}
		ResponseBuilder rb;
		switch (builders.size()) {
		case 0:
			rb = ResponseBuilder.NONE;
			break;
		case 1:
			rb = builders.get(0);
			break;
		default:
			rb = new ResponseBuilder.Composite(builders);
		}
		return rb;
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
			
	/**
	 * @param parameters
	 * @param requestPayloadJSON
	 * @param context
	 * @param listMapKey
	 * @param listMapValue
	 * @param attachments
	 * @param traceEnabled
	 * @param traceLog
	 * @return
	 * {@link Deprecated}
	 * @deprecated
	 */
	protected Map<String,Object> readParameterAsObjects(List<ParameterImpl> parameters,
			Object requestPayloadJSON,JAXBContextImpl context,
			Pattern listMapKey,Pattern listMapValue,List<MIMEPart> attachments,boolean traceEnabled, DebugTrace traceLog){
		
		Map<String,Object> objects	= new LinkedHashMap<String,Object>();
		 for (ParameterImpl parameter : parameters) {
			 if(parameter.isWrapperStyle()) {
				 if(requestPayloadJSON != null){
					 assert requestPayloadJSON instanceof Map;
					 requestPayloadJSON = ((Map<?,?>)requestPayloadJSON).get(parameter.getName().getLocalPart());
				 }
				 objects.putAll(
						 readParameterAsObjects(
								 ((WrapperParameter)parameter).getWrapperChildren(),
								 requestPayloadJSON,
								 context,listMapKey,listMapValue, attachments, traceEnabled,traceLog
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
					if(WSJSONPopulator.isJSONPrimitive(type) ){
						if(parameters.size() == 1){
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
						}else{
							if(requestPayloadJSON instanceof Map){
								val =((Map<?,?>)requestPayloadJSON).get(parameter.getName().getLocalPart());
							}
						}
					}else if (requestPayloadJSON instanceof Map) {
						try {
							try{
								String parameterName = parameter.getName().getLocalPart();
								Object parameterValue = ((Map<?, ?>) requestPayloadJSON).get(parameterName);
								if(parameterValue instanceof Map){
									new WSJSONPopulator(listMapKey,listMapValue,JSONCodec.dateFormat,codec.getCustomSerializer()
											,traceLog).populateObject(val,(Map<?, ?>)parameterValue,null, attachments);
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
	
	
	/**
	 * @param methodImpl
	 * @return
	 */
	protected static Pattern getListMapKey(JavaMethod methodImpl){
		JSONWebService jsonService = methodImpl.getMethod().getAnnotation(JSONWebService.class);
		if(jsonService != null){
			if(!jsonService.listMapKey().trim().equals("")){
				// Performance down, TODO via singleton
				return Pattern.compile(jsonService.listMapKey()); 
			}
		}
		// default codec level
		return JSONCodec.globalMapKeyPattern;
	}
	
	/**
	 * @param methodImpl
	 * @return
	 */
	protected static Pattern getListMapValue(JavaMethod methodImpl){
		JSONWebService jsonService = methodImpl.getMethod().getAnnotation(JSONWebService.class);
		if(jsonService != null){
			if(!jsonService.listMapValue().trim().equals("")){
				// Performance down, TODO via singleton
				return Pattern.compile(jsonService.listMapValue()); 
			}
		}
		// default codec level
		return JSONCodec.globalMapValuePattern;
	}
}
