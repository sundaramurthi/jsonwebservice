package com.jaxws.json.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jws.WebResult;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

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
import com.sun.istack.FinalArrayList;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.DistributedPropertySet;
import com.sun.xml.ws.api.PropertySet;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.server.EndpointAwareCodec;
import com.sun.xml.ws.api.server.EndpointComponent;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.client.sei.SEIStub;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.SOAPSEIModel;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;

/**
 * @author Sundaramurthi
 * @version 1.0
 * @mail sundaramurthis@gmail.com
 */
public class JSONCodec implements EndpointAwareCodec, EndpointComponent {
	private static final String 		JSON_MIME_TYPE 	= "application/json";
	private static final ContentType 	jsonContentType = new JSONContentType();
	private final static String STATUS_STRING_RESERVED = "statusFlag";
	private final 	WSBinding 		binding;
	public final 	SOAPVersion 	soapVersion;
    private 		WSEndpoint<?> 		endpoint;
    private HttpMetadataPublisher 	metadataPublisher;
    static private SEIModel staticSeiModel;

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

	public void decode(InputStream in, String contentType, Packet packet)
			throws IOException {
		Message message = null;
		Object requestMethodJSON;
		try {
			SEIModel seiModel;
			if(this.endpoint != null){
				seiModel	= this.endpoint.getSEIModel();
			}else if(packet.proxy != null && packet.proxy instanceof SEIStub){
				seiModel	= ((SEIStub) packet.proxy).seiModel;
			}else{
				seiModel = staticSeiModel;
			}
			if(seiModel == null){
				throw new Error("Unsuported packet");
			}
			staticSeiModel	= seiModel;
			JAXBContextImpl context = (JAXBContextImpl)seiModel.getJAXBContext();

			requestMethodJSON = JSONUtil.deserialize(new InputStreamReader(in));
			if(requestMethodJSON != null && requestMethodJSON instanceof Map){
				Map<?,?> requestMethodJSONMap = (Map<?,?>) requestMethodJSON;
				//TODO right now handle only last method, change this to handle multiple batch request
				for(Object method : requestMethodJSONMap.keySet()){
					String methodName = method.toString();
					if(methodName.equals(STATUS_STRING_RESERVED)){
						continue;
					}
					Class<?> bean = null;
					JavaMethodImpl methodImpl = null;
					for(JavaMethod m:seiModel.getJavaMethods()){
						QName methodQName = m.getRequestPayloadName();
						if(methodQName.getLocalPart().equals(methodName)){
							if(m instanceof JavaMethodImpl){
								methodImpl = (JavaMethodImpl)m;
							}
							if(context.getGlobalType(methodQName) != null )
								bean = context.getGlobalType(methodQName).jaxbType;
							break;
						}
					}
					
					ParameterImpl parameterResponse = null;
					if(methodImpl == null)
					for(JavaMethod m:seiModel.getJavaMethods()){
						QName methodQName = m.getResponsePayloadName();
						//WebResult result = m.getSEIMethod().getAnnotation(WebResult.class);
						if(methodName.equals(methodQName.getLocalPart())){
							methodImpl = (JavaMethodImpl) m;
							parameterResponse = methodImpl.getResponseParameters().get(0);
							bean = (Class<?>) m.getSEIMethod().getGenericReturnType();
							break;
						}
					}
					
					if(bean != null){
						Object methodParameter = requestMethodJSONMap.get(methodName);
						//Strip out first element
						if(methodParameter instanceof Map){
							methodParameter = ((Map)methodParameter).values().toArray()[0];
						}
						if(JaxWsJSONPopulator.isJSONPrimitive(bean) || bean.isEnum()){
							Bridge bridgeRoot = context.createBridge(parameterResponse.getTypeReference());
							if(parameterResponse instanceof WrapperParameter){
								parameterResponse = ((WrapperParameter)parameterResponse).getWrapperChildren().get(0);
							}
							Bridge bridge = context.createBridge(parameterResponse.getTypeReference());
							CompositeStructure str = new CompositeStructure();
							str.bridges = new Bridge[1];
							str.bridges[0] = bridge;
							str.values = new Object[1];
							if(bean.isEnum()){
								methodParameter = Enum.valueOf((Class<Enum>)bean, methodParameter.toString());
							}
							str.values[0] = methodParameter;
							message = JAXBMessage.create(bridgeRoot, str, soapVersion);
						}else if(methodParameter instanceof Map){
							Object object = bean.newInstance();
							Map<?,?> methodParameterMap = (Map<?,?>) methodParameter;
							for(Field field:bean.getFields()){
								if(field.getType() instanceof Class){//TODO check accessablity
									Object val = field.getType().newInstance();
									Object jsonVal = methodParameterMap.get(field.getName());
									if(jsonVal instanceof Map){
										new JaxWsJSONPopulator(context).populateObject(val, (Map<?,?>) jsonVal);
									}else{// WARN other than primitive type may end with error
										val = jsonVal;
									}
									field.set(object, val);
								}else{
									throw new Exception("TODO JSON Codec , Non object method parameter");
								}
							}
							message = JAXBMessage.create(context, object, soapVersion);
						}else{
							throw new Exception("Methods parameter without map not implemented");
						}
					}else if(methodImpl != null){
						
						 {// prepare objects for creating messages
					            List<ParameterImpl> rp = methodImpl.getRequestParameters();
					            ArrayList<Object> parameterObjects = new ArrayList<Object>();
					            BodyBuilder bodyBuilder = null;
					            
					            Map<?,?>  methodParameterMap = null;
								if(requestMethodJSONMap.get(methodName) instanceof Map){
									methodParameterMap = (Map<?,?>) requestMethodJSONMap.get(methodName);
								}
					            
					            for (ParameterImpl param : rp) {
					                com.jaxws.json.builder.ValueGetter getter = com.jaxws.json.builder.ValueGetterFactory.SYNC.get(param);

					                switch(param.getInBinding().kind) {
					                case BODY:
					                    if(param.isWrapperStyle()) {
					                    	WrapperParameter wrappedParam = (WrapperParameter)param;
					                        if(param.getParent().getBinding().isRpcLit())
					                            bodyBuilder = new BodyBuilder.RpcLit(wrappedParam, soapVersion, ValueGetterFactory.SYNC);
					                        else
					                            bodyBuilder = new BodyBuilder.DocLit(wrappedParam, soapVersion, ValueGetterFactory.SYNC);
					                        for(ParameterImpl paramChild:wrappedParam.getWrapperChildren()){
					                        	Object val = ((Class<?>)paramChild.getTypeReference().type).newInstance();
												Object jsonVal = methodParameterMap.get(	paramChild.getName().getLocalPart());
												if(jsonVal instanceof Map){
													new JaxWsJSONPopulator(context).populateObject(val, (Map<?,?>) jsonVal);
												}else{// WARN other than primitive type may end with error
													val = jsonVal;
												}
												parameterObjects.add(val);
					                        }
					                    } else {
					                        bodyBuilder = new BodyBuilder.Bare(param, soapVersion, getter);
					                    }
					                    break;
					                /*case HEADER:
					                    fillers.add(new MessageFiller.Header(
					                        param.getIndex(),
					                        param.getBridge(),
					                        getter ));
					                    break;
					                case ATTACHMENT:
					                    fillers.add(MessageFiller.AttachmentFiller.createAttachmentFiller(param, getter));
					                    break;*/
					                case UNBOUND:
					                    break;
					                default:
					                    throw new AssertionError(); // impossible
					                }
					            }

					            if(bodyBuilder==null) {
					                // no parameter binds to body. we create an empty message
					                switch(soapVersion) {
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
					            message =  bodyBuilder.createMessage(parameterObjects.toArray());
					        }
					}
				}
			}else{
				throw new JSONException("No method name found");
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
			SEIModel seiModel;
			if(this.endpoint != null){
				seiModel	= this.endpoint.getSEIModel();
			}else if(packet.proxy != null && packet.proxy instanceof SEIStub){
				seiModel	= ((SEIStub) packet.proxy).seiModel;
			}else{
				throw new Error("Unsuported packet");
			}
			staticSeiModel	= seiModel;	
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
					JavaMethod javaMethod = message.getMethod(seiModel);
					boolean requestMessage = true;
					for(JavaMethod m:seiModel.getJavaMethods()){
						QName methodQName = m.getResponsePayloadName();
						if(message.getPayloadLocalPart().equals(methodQName.getLocalPart())){
							javaMethod =m;
							requestMessage = false;
							break;
						}
					}
					if(!requestMessage){
						result.put(STATUS_STRING_RESERVED, "true");
					}
					if(javaMethod != null){
						JavaMethodImpl javaMethodImpl = (JavaMethodImpl)javaMethod;
						 // prepare objects for processing response
				        List<ParameterImpl> rp = null;
				        if(!requestMessage)
				        	rp = javaMethodImpl.getResponseParameters();
				        else	
				        	rp = javaMethodImpl.getRequestParameters();
				        List<ResponseBuilder> builders = new ArrayList<ResponseBuilder>();
				        ArrayList<Object> parameterObjects = new ArrayList<Object>();
				        ArrayList<Object> parameterNames = new ArrayList<Object>();
				        
				     //   rp.addAll(javaMethodImpl.getRequestParameters());
				        for( ParameterImpl param : rp ) {
				            ValueSetter setter;
				            switch(param.getOutBinding().kind) {
				            case BODY:
				                if(param.isWrapperStyle()) {
				                	WrapperParameter wParam = (WrapperParameter)param;
				                    if(param.getParent().getBinding().isRpcLit())
				                        builders.add(new ResponseBuilder.RpcLit(wParam, ValueSetterFactory.SYNC));
				                    else
				                        builders.add(new ResponseBuilder.DocLit(wParam, ValueSetterFactory.SYNC));
				                    for(ParameterImpl paramChild:wParam.getWrapperChildren()){
				                    	Object val;
				                    	if(((Class<?>)paramChild.getTypeReference().type).isEnum()){
				                    		val = paramChild.getTypeReference().type;//Enum.valueOf((Class<Enum>)paramChild.getTypeReference().type, "success".toUpperCase());
				                    	}else{
				                    		val = ((Class<?>)paramChild.getTypeReference().type).newInstance();
				                    	}
			                        	if(!requestMessage)
			                        		parameterObjects.add(val);
			                        	else{
			                        		parameterObjects.add(new Holder(val));
			                        		parameterNames.add(paramChild.getName());
			                        	}
			                        }
				                } else {
				                    setter = ValueSetterFactory.SYNC.get(param);
				                    builders.add(new ResponseBuilder.Body(param.getBridge(),setter));
				                }
				                
				                break;
				            case HEADER:
				                setter = ValueSetterFactory.SYNC.get(param);
				                builders.add(new ResponseBuilder.Header(soapVersion, param, setter));
				                break;
				            case ATTACHMENT:
				                setter = ValueSetterFactory.SYNC.get(param);
				                builders.add(ResponseBuilder.AttachmentBuilder.createAttachmentBuilder(param, setter));
				                break;
				            case UNBOUND:
				                setter = ValueSetterFactory.SYNC.get(param);
				                builders.add(new ResponseBuilder.NullSetter(setter,
				                    ResponseBuilder.getVMUninitializedValue(param.getTypeReference().type)));
				                break;
				            default:
				                throw new AssertionError();
				            }
				        }
				        ResponseBuilder rb;
				        switch(builders.size()) {
				        case 0:
				            rb = ResponseBuilder.NONE;
				            break;
				        case 1:
				            rb = builders.get(0);
				            break;
				        default:
				            rb = new ResponseBuilder.Composite(builders);
				        }
				        
				        Object object = rb.readResponse(message, parameterObjects.toArray());
				        if(requestMessage){
				        	Map<String,Object> params = new HashMap<String, Object>(); 
				        	assert parameterObjects.size() == parameterNames.size();
				        	int pCount = 0;
				        	for(Object param:parameterObjects){
				        		params.put(((QName)parameterNames.get(pCount++)).getLocalPart(), (param instanceof Holder ? ((Holder)param).value:param));
				        	}
				        	result.put(javaMethodImpl.getOperationName(), params);
				        }else{
				        	WebResult resultAnnot =    javaMethodImpl.getMethod().getAnnotation(WebResult.class) ;
					        String responseName = javaMethodImpl.getResponsePayloadName().getLocalPart();
					        if(resultAnnot != null && resultAnnot.name() != null){
					        	HashMap<String,Object> resultObject = new HashMap<String,Object>();
					        	resultObject.put(resultAnnot.name(), object);
					        	object = resultObject;
					        }
				        	result.put(responseName, object);
						}
					}else{
						Object obj = message.readPayloadAsJAXB(seiModel.getJAXBContext().createUnmarshaller());
						for (int i = 0; i < obj.getClass().getDeclaredFields().length; i++) {
							Field field = obj.getClass().getDeclaredFields()[i];
							try {
								result.put(field.getName(), field.get(obj));
							} catch (Throwable th) {
							}
						}
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
