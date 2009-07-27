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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Element;

import com.googlecode.jsonplugin.JSONException;
import com.googlecode.jsonplugin.WSJSONReader;
import com.googlecode.jsonplugin.WSJSONWriter;
import com.jaxws.json.DateFormat;
import com.jaxws.json.codec.doc.JSONHttpMetadataPublisher;
import com.jaxws.json.feature.JSONWebService;
import com.jaxws.json.serializer.CustomSerializer;
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
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.ServiceFinder;

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
    private static boolean				requestPayloadEnabled	= true;	
    private static boolean				excludeNullProperties	= false;
    private static Pattern 				pattern = null,valuePattern = null;
    protected static DateFormat			dateFormat = DateFormat.PLAIN;
    
    public static Collection<Pattern> excludeProperties 	= new ArrayList<Pattern>();
    private static Collection<Pattern> includeProperties	= null;//new ArrayList<Pattern>();
    static SOAPFactory soapFactory = null ;
    
    private static Logger LOG			= Logger.getLogger(JSONCodec.class.getName());
    private Map<Class<? extends Object>,CustomSerializer> customCodecs;
	
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
    		if(key.toString().equals("json.excludeNullProperties")){
    			excludeNullProperties	= Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().startsWith("json.include")){
    			includeProperties.add(Pattern.compile(properties.getProperty(key.toString())));
    		}else if(key.toString().equals("json.response.enable.payloadname")){
    			responsePayloadEnabled	= Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals("json.request.enable.payloadname")){
    			requestPayloadEnabled	= Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().startsWith("json.exclude")){
    			excludeProperties.add(Pattern.compile(properties.getProperty(key.toString())));
    		}else if(key.toString().equals("json.list.map.key")){
    			pattern = Pattern.compile(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals("json.list.map.value")){
    			valuePattern = Pattern.compile(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals(com.jaxws.json.DateFormat.class.getName())){
    			dateFormat	= Enum.valueOf(com.jaxws.json.DateFormat.class, properties.getProperty(key.toString()).trim());
    		}
    	}
    	try {
			soapFactory = SOAPFactory.newInstance();
		} catch (SOAPException e) {}
    }
    
	public JSONCodec(WSBinding binding) {
		this.binding = binding;
		this.soapVersion = binding.getSOAPVersion();
		initCustom();
	}
	
	public JSONCodec(JSONCodec that) {
        this(that.binding);
        this.endpoint = that.endpoint;
        this.customCodecs = that.customCodecs;
    }
	
	private void initCustom() {
		customCodecs = new HashMap<Class<? extends Object>, CustomSerializer>();
		for (CustomSerializer serializer : ServiceFinder
				.find(CustomSerializer.class)) {
			customCodecs.put(serializer.getAcceptClass(), serializer);
		}
	}
	
	public Map<Class<? extends Object>, CustomSerializer> getCustomSerializer(){
		return customCodecs;
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
			String operationName = null;
			//TODO code clean
			if((!requestPayloadEnabled) && packet.webServiceContextDelegate != null && packet.webServiceContextDelegate instanceof WSHTTPConnection){
				operationName = ((WSHTTPConnection)packet.webServiceContextDelegate).getQueryString();
			}
			if((!requestPayloadEnabled) && operationName == null){
				throw new Exception("Invalid Operation name in query parameter.(Please make payload name enabled or pass valid opeartion name as query parameter)");
			}
			SEIModel 			seiModel 	= getSEIModel(packet);
			JAXBContextImpl 	context 	= (JAXBContextImpl)seiModel.getJAXBContext();
			//read content
	        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(in));
	        String line = null;
	        StringBuilder buffer = new StringBuilder();
	        if(!requestPayloadEnabled){
	        	buffer.append("{\""+operationName+"\":");
	        }
	        try {
	            while ((line = bufferReader.readLine()) != null) {
	                buffer.append(line);
	            }
	        } catch (IOException e) {
	            throw new JSONException(e);
	        }
	        if(!requestPayloadEnabled){
	        	buffer.append("}");
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
					if(payload.equals(MessageContext.MESSAGE_OUTBOUND_PROPERTY))
						continue;
					JavaMethodImpl methodImpl = getJavaMethodUsingPayloadName(seiModel,payload.toString());
					if(methodImpl != null){
						if(methodImpl.getOperationName().equals(payload)){
							// TEST HIT 2
							// PRODUCTION HIT 1
							// Decode as Request
							JSONRequestBodyBuilder	jsonRequestBodyBuilder = new JSONRequestBodyBuilder(this);
							message = jsonRequestBodyBuilder.createMessage(methodImpl,requestPayloadJSONMap,context);
						}else{
							// TEST HIT 4 END
							//Decode as Response
							// Should happen only in TEST decoder
							JSONResponseBodyBuilder jsonResponseBodyBuilder = new JSONResponseBodyBuilder(this);
							message = jsonResponseBodyBuilder.createMessage(methodImpl,requestPayloadJSONMap,context);
						}
					}else{
						throw new Error("Unknown payload "+payload);
					}
				}
			}else{
				throw new JSONException("No method/payload name found");
			}
		} catch (Throwable e) {
			packet.put(MessageContext.HTTP_RESPONSE_CODE, new Integer(400)); //BAD request
			SOAPFault faultOb;
			try {
				faultOb = soapFactory.createFault("Client",new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE));
				faultOb.setFaultActor(this.getClass().getName());
				
				if(e instanceof JSONException){
					faultOb.setFaultCode("Client.Invalid.structure");
					faultOb.setFaultString("Invalid json input");
				}else{
					faultOb.setFaultCode("Client");
					faultOb.setFaultString("Invalid input");
				}
				Detail detail = faultOb.addDetail();
				detail.addChildElement("exception").setTextContent(e.getMessage());
				packet.setMessage(Messages.create(faultOb));
				if (packet.webServiceContextDelegate != null
						&& packet.webServiceContextDelegate instanceof WSHTTPConnection) {
					WSHTTPConnection con = (WSHTTPConnection) packet.webServiceContextDelegate;
					ByteArrayBuffer buf = new ByteArrayBuffer();
					con.setContentTypeResponseHeader(encode(packet, buf)
							.getContentType());
					//TODO disable option
					dump(buf, "HTTP response " + con.getStatus(), con.getResponseHeaders());
					 
					OutputStream os = con.getOutput();
					buf.writeTo(os);
					os.close();
					con.close();
				}
			} catch (SOAPException e1) {/*Out of control go with empty message*/}
		} 
		if(message == null){
			//TODO log
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
					SOAPFault faultObj = message.readAsSOAPMessage().getSOAPBody().getFault();
					HashMap<String,String> detail = new HashMap<String, String>(); 
					try {
						for (Iterator<Element> iterator = faultObj
								.getDetail().getChildElements(); iterator
								.hasNext();) {
							Element type = iterator.next();
							detail.put(type.getLocalName(), type.getTextContent());
						}
					} catch(Throwable th){/*Dont mind about custom message set fail*/}
					HashMap<String,Object> exception = new HashMap<String, Object>(); 
					exception.put("code", faultObj.getFaultCodeAsQName().getLocalPart().toUpperCase());
					exception.put("message", faultObj.getFaultString());
					exception.put("actor", faultObj.getFaultActor());
					exception.put("cause", detail);
					result.put("exception", exception);
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
						if(methodImpl.getOperationName().equals(message.getPayloadLocalPart())){
							// TEST HIT 1
							// Encode as Request For testing
							JSONRequestBodyBuilder	jsonRequestBodyBuilder = new JSONRequestBodyBuilder(this);
							Map<String, Object> parameters = jsonRequestBodyBuilder.createMap(methodImpl,message);
							//end remove holder
							// When request use "methodName":{"param1":{},"param2":{}}
							result.put(methodImpl.getOperationName(),parameters);
						}else{
							// TEST HIT 3
							// PRODUCTION HIT 2
							JSONResponseBodyBuilder jsonResponseBodyBuilder = new JSONResponseBodyBuilder(this);
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
				WSJSONWriter writer = new WSJSONWriter(listWrapperSkip,
						listMapKey,
						listMapValue,
						dateFormat,
						customCodecs
						);
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
	
	/*public static boolean isListWarperSkip(JavaMethodImpl methodImpl){
		JSONWebService jsonService = methodImpl.getMethod().getAnnotation(JSONWebService.class);
		if(jsonService != null){
			return jsonService.skipListWrapper();
		}
		// default codec level
		return listWarperSkip;
	}*/
	
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
	
	public DateFormat getDateFormat() {
		return dateFormat;
	}

	private void dump(ByteArrayBuffer buf, String caption, Map<String, List<String>> headers) throws IOException {
	        System.out.println("---["+caption +"]---");
	        if (headers != null) {
	            for (Entry<String, List<String>> header : headers.entrySet()) {
	                if (header.getValue().isEmpty()) {
	                    // I don't think this is legal, but let's just dump it,
	                    // as the point of the dump is to uncover problems.
	                    System.out.println(header.getValue());
	                } else {
	                    for (String value : header.getValue()) {
	                        System.out.println(header.getKey() + ": " + value);
	                    }
	                }
	            }
	        }
	        buf.writeTo(System.out);
	        System.out.println("--------------------");
	 }
}
