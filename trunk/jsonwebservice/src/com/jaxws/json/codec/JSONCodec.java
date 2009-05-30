package com.jaxws.json.codec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Element;

import com.googlecode.jsonplugin.JSONException;
import com.googlecode.jsonplugin.WSJSONReader;
import com.googlecode.jsonplugin.WSJSONWriter;
import com.jaxws.json.DateFormat;
import com.jaxws.json.codec.doc.JSONHttpMetadataPublisher;
import com.jaxws.json.feature.JSONWebService;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
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
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;

/**
 * @author Sundaramurthi
 * @version 1.0
 * @mail sundaramurthis@gmail.com
 */
public class JSONCodec implements EndpointAwareCodec, EndpointComponent {
	private static final ContentType 	jsonContentType 		= new JSONContentType();
	private final static String 		STATUS_STRING_RESERVED 	= "statusFlag";
	private final 	WSBinding 			binding;
	public final 	SOAPVersion 		soapVersion;
    private 		WSEndpoint<?> 		endpoint;
    private HttpMetadataPublisher 		metadataPublisher;
    static private SEIModel 			staticSeiModel;
    private static boolean				responsePayloadEnabled	= true;	
    private static boolean				excludeNullProperties	= false;
    private static Pattern 				pattern = null,valuePattern = null;
    private static boolean 				listWarperSkip = false;
    protected static DateFormat			dateFormatType = DateFormat.PLAIN;
    
    private static Logger LOG			= Logger.getLogger(JSONCodec.class.getName());
    
    public static Collection<Pattern> excludeProperties 	= new ArrayList<Pattern>();
    private static Collection<Pattern> includeProperties	= null;//new ArrayList<Pattern>();
	
    static{
    	Properties properties = new Properties();
    	URL serviceProperties = JSONCodec.class.getResource("/jsonservice.properties");
    	if(serviceProperties != null){
    		LOG.info("Using JSON service properties from "+serviceProperties);
    		try {
				properties.load(serviceProperties.openStream());
			} catch (Throwable thrown) {
				LOG.throwing(JSONCodec.class.getSimpleName(), "property load", thrown);
			}
    	}
    	properties.put("json.exclude.serialVersionUID", "serialVersionUID");
    	for(Object key:properties.keySet()){
    		if(key.toString().startsWith("json.exclude")){
    			excludeProperties.add(Pattern.compile(properties.getProperty(key.toString())));
    		}else if(key.toString().startsWith("json.include")){
    			includeProperties.add(Pattern.compile(properties.getProperty(key.toString())));
    		}else if(key.toString().equals("json.response.enable.payloadname")){
    			responsePayloadEnabled	= Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals("json.excludeNullProperties")){
    			excludeNullProperties	= Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals("json.list.map.key")){
    			pattern = Pattern.compile(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals("json.list.map.value")){
    			valuePattern = Pattern.compile(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals("json.response.list.wrapper.skip")){
    			listWarperSkip = Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals(com.jaxws.json.DateFormat.class.getName())){
    			dateFormatType	= Enum.valueOf(com.jaxws.json.DateFormat.class, properties.getProperty(key.toString()).trim());
    		}
    		
    	}
    }
    
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
			if(m.getOperationName().equals(payloadName) 
					|| ((!m.getMEP().isOneWay()) &&  m.getResponsePayloadName().getLocalPart().equals(payloadName))){
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
	
	/* (non-Javadoc)
	 * @see com.sun.xml.ws.api.pipe.Codec#decode(java.io.InputStream, java.lang.String, com.sun.xml.ws.api.message.Packet)
	 */
	public void decode(InputStream in, String contentType, Packet packet)
			throws IOException {
		Message message = null;
		Object inputJSON;
		try {
			SEIModel 			seiModel 	= getSEIModel(packet);
			JAXBContextImpl 	context 	= (JAXBContextImpl)seiModel.getJAXBContext();
			//read content
	        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(in));
	        String line = null;
	        StringBuilder buffer = new StringBuilder();
	        try {
	            while ((line = bufferReader.readLine()) != null) {
	                buffer.append(line);
	            }
	        } catch (IOException e) {
	            throw new JSONException(e);
	        }
	        inputJSON =  new WSJSONReader().read(buffer.toString());
			if(inputJSON != null && inputJSON instanceof Map){
				Map<String, Object> requestPayloadJSONMap = (Map<String, Object>) inputJSON;
				if(requestPayloadJSONMap.containsKey(STATUS_STRING_RESERVED)){
					if(!new Boolean(requestPayloadJSONMap.get(STATUS_STRING_RESERVED).toString())){
						packet.setMessage(new com.sun.xml.ws.message.FaultMessage(Messages.createEmpty(soapVersion),null));
						return;
					}
					// Remove codec set value WARN user should not used codec specific key
					requestPayloadJSONMap.remove(STATUS_STRING_RESERVED);
				}
				for(Object payload : requestPayloadJSONMap.keySet()){
					JavaMethodImpl methodImpl = getJavaMethodUsingPayloadName(seiModel,payload.toString());
					if(methodImpl != null){
						if(methodImpl.getOperationName().equals(payload)){
							// TEST HIT 2
							// PRODUCTION HIT 1
							// Decode as Request
							JSONRequestBodyBuilder	jsonRequestBodyBuilder = new JSONRequestBodyBuilder(soapVersion);
							message = jsonRequestBodyBuilder.createMessage(methodImpl,requestPayloadJSONMap,context);
						}else{
							// TEST HIT 4 END
							//Decode as Response
							// Should happen only in TEST decoder
							JSONResponseBodyBuilder jsonResponseBodyBuilder = new JSONResponseBodyBuilder(soapVersion);
							message = jsonResponseBodyBuilder.createMessage(methodImpl,requestPayloadJSONMap,context);
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
				Pattern listMapKey		= null;
				Pattern listMapValue	= null;
				boolean listWrapperSkip = false;
				Collection<Pattern> excludeProperties 	= this.excludeProperties;
			    Collection<Pattern> includeProperties	= this.includeProperties;
				
				sw = new OutputStreamWriter(out, "UTF-8");
				HashMap<String, Object> result = new HashMap<String, Object>();
				for (Iterator iterator = packet.invocationProperties.keySet().iterator(); iterator
						.hasNext();) {
					Object type = iterator.next();
					result.put(type.toString(),packet.invocationProperties.get(type));
				}
				if (message.isFault()) {
					result.put(STATUS_STRING_RESERVED, false);
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
				} else {// Not fault
					result.put(STATUS_STRING_RESERVED, true);
					JavaMethodImpl methodImpl = (JavaMethodImpl)message.getMethod(seiModel);
					if(methodImpl == null)// in case response OUT
						methodImpl = getJavaMethodUsingPayloadName(seiModel, message.getPayloadLocalPart());
					
					if(methodImpl != null){
						String inEx[][] = getInExProperties(methodImpl);
						if(inEx[0].length > 0){
							if(includeProperties == null)
								includeProperties = new ArrayList<Pattern>();
							for(String include:inEx[0])
								includeProperties.add(Pattern.compile(include));
							
						}
						if(inEx[1].length > 0){
							if(excludeProperties == null)
								excludeProperties = new ArrayList<Pattern>();
							for(String exclude:inEx[1])
								excludeProperties.add(Pattern.compile(exclude));
						}
						
						listMapKey = getListMapKey(methodImpl);
						listMapValue = getListMapValue(methodImpl);
						listWrapperSkip	= isListWarperSkip(methodImpl);
						if(methodImpl.getOperationName().equals(message.getPayloadLocalPart())){
							// TEST HIT 1
							// Encode as Request For testing
							JSONRequestBodyBuilder	jsonRequestBodyBuilder = new JSONRequestBodyBuilder(soapVersion);
							Map<String, Object> parameters = jsonRequestBodyBuilder.createMap(methodImpl,message);
							//end remove holder
							// When request use "methodName":{"param1":{},"param2":{}}
							result.put(methodImpl.getOperationName(),parameters);
						}else{
							// TEST HIT 3
							// PRODUCTION HIT 2
							JSONResponseBodyBuilder jsonResponseBodyBuilder = new JSONResponseBodyBuilder(soapVersion);
							if(!methodImpl.getMEP().isOneWay()){
								Map<String, Object> parameters = jsonResponseBodyBuilder.createMap(methodImpl,message);
								if(responsePayloadEnabled){
									result.put(methodImpl.getResponsePayloadName().getLocalPart(),parameters);
								}else{
									result.putAll(parameters);
								}
							}else{
								 //result.put("void","");
							}
						}
					}else{
						throw new Error("Unknown payload "+message.getPayloadLocalPart());
					}
				}
				WSJSONWriter writer = new WSJSONWriter(listWrapperSkip,listMapKey,listMapValue,dateFormatType);
				sw.write(writer.write(result, excludeProperties, includeProperties, excludeNullProperties));
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
	
	public static boolean isListWarperSkip(JavaMethodImpl methodImpl){
		JSONWebService jsonService = methodImpl.getMethod().getAnnotation(JSONWebService.class);
		if(jsonService != null){
			return jsonService.skipListWrapper();
		}
		// default codec level
		return listWarperSkip;
	}
	
	public static Pattern getListMapKey(JavaMethodImpl methodImpl){
		JSONWebService jsonService = methodImpl.getMethod().getAnnotation(JSONWebService.class);
		if(jsonService != null){
			if(!jsonService.listMapKey().trim().equals("")){
				// Performance down, TODO via singleton
				return Pattern.compile(jsonService.listMapKey()); 
			}
		}
		// default codec level
		return pattern;
	}
	
	public static Pattern getListMapValue(JavaMethodImpl methodImpl){
		JSONWebService jsonService = methodImpl.getMethod().getAnnotation(JSONWebService.class);
		if(jsonService != null){
			if(!jsonService.listMapValue().trim().equals("")){
				// Performance down, TODO via singleton
				return Pattern.compile(jsonService.listMapValue()); 
			}
		}
		// default codec level
		return valuePattern;
	}
	
	public static String[][] getInExProperties(JavaMethodImpl methodImpl){
		JSONWebService jsonService = methodImpl.getMethod().getAnnotation(JSONWebService.class);
		if(jsonService != null){
			return new String[][]{jsonService.includeProperties(),jsonService.excludeProperties()};
		}
		// default codec level
		return new String[][]{{},{}};
	}

	public ContentType encode(Packet arg0, WritableByteChannel arg1) {
		throw new UnsupportedOperationException();
	}

	public String getMimeType() {
		return JSONContentType.JSON_MIME_TYPE;
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
}
