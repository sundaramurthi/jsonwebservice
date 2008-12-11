package com.jaxws.json.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam.Mode;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.googlecode.jsonplugin.JSONException;
import com.googlecode.jsonplugin.JSONUtil;
import com.jaxws.json.JaxWsJSONPopulator;
import com.jaxws.json.builder.BodyBuilder;
import com.jaxws.json.builder.ResponseBuilder;
import com.jaxws.json.builder.ValueGetterFactory;
import com.jaxws.json.builder.ValueSetter;
import com.jaxws.json.builder.ValueSetterFactory;
import com.jaxws.json.codec.doc.JSONHttpMetadataPublisher;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.server.EndpointAwareCodec;
import com.sun.xml.ws.api.server.EndpointComponent;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.client.sei.SEIStub;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;

/**
 * @author Sundaramurthi
 * @version 1.0
 * @mail sundaramurthis@gmail.com
 */
public class JSONCodec implements EndpointAwareCodec, EndpointComponent {
	private static final String 		JSON_MIME_TYPE 			= "application/json";
	private static final ContentType 	jsonContentType 		= new JSONContentType();
	private final static String 		STATUS_STRING_RESERVED 	= "statusFlag";
	private final static String 		PAYLOAD_STRING_RESERVED = "payloadName";
	private final 	WSBinding 		binding;
	public final 	SOAPVersion 	soapVersion;
    private 		WSEndpoint<?> 		endpoint;
    private HttpMetadataPublisher 	metadataPublisher;
    static private SEIModel staticSeiModel;
    
    Log codecLog = LogFactory.getFactory().getInstance(JSONCodec.class);

	public JSONCodec(WSBinding binding) {
		this.binding = binding;
		this.soapVersion = binding.getSOAPVersion();
	}
	
	public JSONCodec(JSONCodec that) {
        this(that.binding);
        this.endpoint = that.endpoint;
    }

	public void setEndpoint(WSEndpoint endpoint) {
		this.endpoint = endpoint;
        endpoint.getComponentRegistry().add(this);
	}

	public Codec copy() {
		 return new JSONCodec(this);
	}
	
	private SEIModel getSEIModel(@NotNull final Packet packet){
		SEIModel seiModel;
		if(this.endpoint != null){
			seiModel	= this.endpoint.getSEIModel();
		}else if(packet.proxy != null && packet.proxy instanceof SEIStub){
			seiModel	= ((SEIStub) packet.proxy).seiModel;
		}else{
			seiModel = staticSeiModel;
		}
		if(seiModel == null){
			throw new Error("Packet or end point dont have SEI Model");
		}
		//Used in test cases while coding/decoding required in 2 way
		staticSeiModel	= seiModel;
		return seiModel;
	}
	
	private JavaMethodImpl getJavaMethodUsingPayloadName(SEIModel seiModel,String payloadName){
		JavaMethodImpl methodImpl = null;
		for(JavaMethod m:seiModel.getJavaMethods()){
			if(m.getRequestPayloadName().getLocalPart().equals(payloadName) 
					|| m.getResponsePayloadName().getLocalPart().equals(payloadName)){
				if(m instanceof JavaMethodImpl){
					methodImpl = (JavaMethodImpl)m;
				}else{
					throw new Error("JavaMethod implementation is not JavaMethodImpl, " +
							"May be NON JAX-WS implementaion");
				}
				break;
			}
		}
		return methodImpl;
	}
	
	private Map<String,Object> readRequestPayLoadAsObjects(List<ParameterImpl> parameters,Object requestPayloadJSON,JAXBContextImpl context){
		Map<String,Object> objects	= new LinkedHashMap<String,Object>();
		 for (ParameterImpl parameter : parameters) {
			 if(parameter.isWrapperStyle()) {
				 if(requestPayloadJSON != null){
					 assert requestPayloadJSON instanceof Map;
					 requestPayloadJSON = ((Map)requestPayloadJSON).get(parameter.getName().getLocalPart());
				 }
				 objects.putAll(
						 readRequestPayLoadAsObjects(
								 ((WrapperParameter)parameter).getWrapperChildren(),
								 requestPayloadJSON,
								 context
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
						codecLog.error(e);
						if(codecLog.isDebugEnabled()){
							e.printStackTrace();
						}
					}
             	}
				if (val != null && requestPayloadJSON != null && context != null) {
					if(JaxWsJSONPopulator.isJSONPrimitive(type) || type.isEnum()){
						CompositeStructure str = new CompositeStructure();
						str.bridges = new Bridge[1];
						str.bridges[0] = context.createBridge(parameter.getTypeReference());
						str.values = new Object[1];
						if(requestPayloadJSON instanceof Map){
							requestPayloadJSON =((Map)requestPayloadJSON).get(parameter.getName().getLocalPart());
						}
						if(type.isEnum()){
							requestPayloadJSON = Enum.valueOf((Class<Enum>)type, requestPayloadJSON.toString());
						}
						str.values[0] = requestPayloadJSON;
						val = str;
					}else if (requestPayloadJSON instanceof Map) {
						try {
							new JaxWsJSONPopulator(context).populateObject(val,(Map<?, ?>)
									((Map<?, ?>) requestPayloadJSON).get(parameter.getName().getLocalPart()));
							if(parameter.getMode() == Mode.OUT){
								CompositeStructure str = new CompositeStructure();
								str.bridges = new Bridge[1];
								str.bridges[0] = context.createBridge(parameter.getTypeReference());
								str.values = new Object[1];
								str.values[0] = val;
								val = str;
							}
						} catch (Exception e) {
							codecLog.error("Value population failed for "
									+ parameter.getPartName(), e);
							if (codecLog.isDebugEnabled()) {
								e.printStackTrace();
							}
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
	
	private BodyBuilder getBodyBuilder(List<ParameterImpl> parameters) {
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
	
	private ResponseBuilder getResponseBuilder(List<ParameterImpl> parameters) {
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

	public void decode(InputStream in, String contentType, Packet packet)
			throws IOException {
		Message message = null;
		Object inputJSON;
		try {
			SEIModel 			seiModel 	= getSEIModel(packet);
			JAXBContextImpl 	context 	= (JAXBContextImpl)seiModel.getJAXBContext();
			inputJSON 						= JSONUtil.deserialize(new InputStreamReader(in));
			if(inputJSON != null && inputJSON instanceof Map){
				Map<String, Object> requestPayloadJSONMap = (Map<String, Object>) inputJSON;
				if(requestPayloadJSONMap.containsKey(STATUS_STRING_RESERVED)){
					// Remove codec set value WARN user should not used condec specific key
					requestPayloadJSONMap.remove(STATUS_STRING_RESERVED);
				}
				if(requestPayloadJSONMap.containsKey(PAYLOAD_STRING_RESERVED)){
					String payloadname = requestPayloadJSONMap.get(PAYLOAD_STRING_RESERVED).toString();
					requestPayloadJSONMap.remove(PAYLOAD_STRING_RESERVED);
					Map<String, Object> requestPayloadJSONMapWithPayload = new HashMap<String, Object>();
					requestPayloadJSONMapWithPayload.putAll(requestPayloadJSONMap);
					requestPayloadJSONMap.clear();
					requestPayloadJSONMap.put(payloadname, requestPayloadJSONMapWithPayload);
				}
				//TODO right now handle only last method, change this to handle multiple batch request
				for(Object payload : requestPayloadJSONMap.keySet()){
					JavaMethodImpl methodImpl = getJavaMethodUsingPayloadName(seiModel,payload.toString());
					if(methodImpl != null){
						Class<?> bean = null;
						JaxWsJSONPopulator populator = new JaxWsJSONPopulator(context);
						if(methodImpl.getRequestPayloadName().getLocalPart().equals(payload)){
							// Decode as Request
							Collection<Object> parameterObjects = readRequestPayLoadAsObjects(
																methodImpl.getRequestParameters(),
																requestPayloadJSONMap,context).values();
							BodyBuilder bodyBuilder = getBodyBuilder(methodImpl.getRequestParameters());
					        message =  bodyBuilder.createMessage(parameterObjects.toArray());
						}else{
							//Decode as Response
							Collection<Object> parameterObjects = readRequestPayLoadAsObjects(
																methodImpl.getResponseParameters(),
																requestPayloadJSONMap,context).values();
							assert parameterObjects.size() == 1;
							message = JAXBMessage.create(methodImpl.getResponseParameters().get(0).getBridge(), parameterObjects.toArray()[0], soapVersion);
						}
					}else{
						throw new Error("Unknown payload "+payload);
					}
				}
			}else{
				throw new JSONException("No method/payload name found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			packet.put(MessageContext.HTTP_RESPONSE_CODE, new Integer(400)); //BAD request
			message = Messages.createEmpty(soapVersion);
		} 
		packet.setMessage(message);
	}

	public void decode(ReadableByteChannel arg0, String arg1, Packet arg2) {
		throw new UnsupportedOperationException();
	}

	public ContentType encode(Packet packet, OutputStream out) throws IOException {
		Message message = packet.getMessage();
		if (message != null) {
			SEIModel seiModel = getSEIModel(packet);
			OutputStreamWriter sw = null;
			try {
				sw = new OutputStreamWriter(out, "UTF-8");
				HashMap<String, Object> result = new HashMap<String, Object>();
				if (message.isFault()) {
					result.put(STATUS_STRING_RESERVED, "flase");
					result.put("message", message.readAsSOAPMessage().getSOAPBody().getFault().getFaultString());
					HashMap<String,String> detail = new HashMap<String, String>(); 
					try {
						for (Iterator<Element> iterator = message
								.readAsSOAPMessage().getSOAPBody().getFault()
								.getDetail().getChildElements(); iterator
								.hasNext();) {
							Element type = iterator.next();
							detail.put(type.getLocalName(), type.getTextContent());
						}
					} catch(Throwable th){/*Dont mind about custom message set fail*/}
					result.put("detail", detail);
				} else {
					result.put(STATUS_STRING_RESERVED, "true");
					JavaMethodImpl methodImpl = getJavaMethodUsingPayloadName(seiModel, message.getPayloadLocalPart());
					
					
					if(methodImpl != null){
						ResponseBuilder responseBuilder;
						Map<String,Object> parameterObjects;
						String payloadName;
						if(methodImpl.getRequestPayloadName().getLocalPart().equals(message.getPayloadLocalPart())){
							// Encode as Request
							parameterObjects = readRequestPayLoadAsObjects(
																methodImpl.getRequestParameters(),
																null,null);
							payloadName	= methodImpl.getRequestPayloadName().getLocalPart();
							
							responseBuilder = getResponseBuilder(methodImpl.getRequestParameters());
							responseBuilder.readResponse(message, parameterObjects.values().toArray());
							HashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
							//Remove Holder objects
							for(String key :parameterObjects.keySet()){
								if(parameterObjects.get(key) instanceof Holder){
									parameters.put(key, ((Holder)parameterObjects.get(key)).value);
								}else{
									parameters.put(key,parameterObjects.get(key));
								}
							}
							//end remove holder
							//result.put(payloadName,parameters);
							result.putAll(parameters);
						}else{
							//Encode as Response
							parameterObjects = readRequestPayLoadAsObjects(
																methodImpl.getResponseParameters(),
																null,null);
							assert parameterObjects.size() == 1;
							payloadName = methodImpl.getResponsePayloadName().getLocalPart();
							
				        	responseBuilder = getResponseBuilder(methodImpl.getResponseParameters());
				        	HashMap<String, Object> parameters = new HashMap<String, Object>();
							parameters.put(parameterObjects.keySet().toArray()[0].toString(), 
									responseBuilder.readResponse(message, parameterObjects.values().toArray()));
							//result.put(payloadName,parameters);
							result.putAll(parameters);
						}
						result.put(PAYLOAD_STRING_RESERVED, payloadName);
					}else{
						throw new Error("Unknown payload "+message.getPayloadLocalPart());
					}
				}
				JSONUtil.serialize(sw, result);
			} catch (Exception xe) {
				throw new WebServiceException(xe);
			} finally {
				if (sw != null) {
					try {
						sw.close();
					} catch (Exception xe) {
						// let the original exception get through
					}
				}
			}
		}
		return jsonContentType;
	}

	public ContentType encode(Packet arg0, WritableByteChannel arg1) {
		throw new UnsupportedOperationException();
	}

	public String getMimeType() {
		return JSON_MIME_TYPE;
	}

	public ContentType getStaticContentType(Packet arg0) {
		return jsonContentType;
	}

	public @Nullable <T> T getSPI(@NotNull Class<T> type) {
		if (type == HttpMetadataPublisher.class) {
			if (metadataPublisher == null)
				metadataPublisher = new JSONHttpMetadataPublisher(endpoint);
			return type.cast(metadataPublisher);
		}
		return null;
	}
	
	 private static final class  JSONContentType implements ContentType {

	        private static final String JSON_CONTENT_TYPE = JSON_MIME_TYPE;

	        public String getContentType() {
	            return JSON_CONTENT_TYPE;
	        }

	        public String getSOAPActionHeader() {
	            return null;
	        }

	        public String getAcceptHeader() {
	            return JSON_CONTENT_TYPE;
	        }
	    }
}
