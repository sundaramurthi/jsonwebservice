package com.jaxws.json.codec;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jws.WebParam.Mode;
import javax.xml.ws.Holder;

import com.jaxws.json.DateFormat;
import com.jaxws.json.JaxWsJSONPopulator;
import com.jaxws.json.builder.BodyBuilder;
import com.jaxws.json.builder.ResponseBuilder;
import com.jaxws.json.builder.ValueGetterFactory;
import com.jaxws.json.builder.ValueSetter;
import com.jaxws.json.builder.ValueSetterFactory;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;

public class MessageBodyBuilder {
	final protected SOAPVersion 	soapVersion;
	private static Logger LOG	= Logger.getLogger(JSONCodec.class.getName());

	public MessageBodyBuilder(SOAPVersion soapVersion) {
		super();
		this.soapVersion = soapVersion;
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
								soapVersion, ValueGetterFactory.SYNC);
					else
						bodyBuilder = new BodyBuilder.DocLit(wrappedParam,
								soapVersion, ValueGetterFactory.SYNC);
				} else {
					bodyBuilder = new BodyBuilder.Bare(param, soapVersion,
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
			switch (soapVersion) {
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
				builders.add(new ResponseBuilder.Header(soapVersion, param,
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
	

	
	protected Map<String,Object> readParameterAsObjects(List<ParameterImpl> parameters,
			Object requestPayloadJSON,JAXBContextImpl context,boolean skipListWrapper,
			Pattern listMapKey,DateFormat dateFormat){
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
								 context,skipListWrapper,listMapKey,dateFormat
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
									new JaxWsJSONPopulator(context,skipListWrapper,listMapKey,dateFormat
											).populateObject(val,(Map<?, ?>)parameterValue	);
								}else if(skipListWrapper && parameterValue instanceof List){
									HashMap<String,Object> map = new HashMap<String, Object>();
									String warperName = getWarpedListName(val.getClass());
									if(warperName != null){
										map.put(warperName, parameterValue);
										new JaxWsJSONPopulator(context,skipListWrapper,listMapKey,dateFormat).populateObject(val,map);
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
