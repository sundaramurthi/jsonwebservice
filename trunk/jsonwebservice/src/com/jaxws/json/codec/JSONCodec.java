package com.jaxws.json.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.MessageContext;

import com.jaxws.json.codec.decode.FormDecoder;
import com.jaxws.json.codec.decode.JSONDecoder;
import com.jaxws.json.codec.doc.JSONHttpMetadataPublisher;
import com.jaxws.json.codec.encode.JSONEncoder;
import com.jaxws.json.feature.JSONWebService;
import com.jaxws.json.packet.handler.Encoder;
import com.jaxws.json.serializer.JSONObjectCustomizer;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.EndpointAwareCodec;
import com.sun.xml.ws.api.server.EndpointComponent;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.client.sei.SEIStub;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.protocol.soap.MessageCreationException;
import com.sun.xml.ws.server.UnsupportedMediaException;
import com.sun.xml.ws.transport.Headers;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.ServiceFinder;

/**
 * @model abstract="false" interface="false"
 * @author Sundaramurthi
 * @version 1.0
 * @mail sundaramurthis@gmail.com
 */
public class JSONCodec implements EndpointAwareCodec, EndpointComponent {
	private static Logger LOG			= Logger.getLogger(JSONCodec.class.getName());
	
	/**
	 * Static content type of json. used in request/response verification.
	 */
	public static final ContentType 	jsonContentType 		= new JSONContentType();
	
	/**
	 * Flag to enable/disable json request call status. 
	 */
	public static final String 		STATUS_STRING_RESERVED 	= "statusFlag";

	/**
	 * TRACE back JSON object name
	 */
	public static final String 		TRACE 					= "TRACE";

	/**
	 * http header name for trace log
	 */
	public static final String 		XDEBUG_HEADER			= "X-Debug";
	
	/**
	 * Http header for custom json parameter name
	 */
	public static final String		XJSONPARAM_HEADER		= "X-JSONParam";
	
	/**
	 * 
	 */
	public static final String 		CONTENT_DISPOSITION_HEADER = "Content-Disposition";

	public static final String 		JSON_MAP_KEY 			= "JSON_MAP_KEY";
	
	public static final String		FORCED_RESPONSE_CONTENT_TYPE	= "FORCED_RESPONSE_CONTENT_TYPE";
	
	/**
	 * Default json parameter name if XJSONPARAM_HEADER not present in http request
	 */
	public static String		XJSONPARAM_DEFAULT			= "JSON";
	
	/**
	 * Attachment key
	 */
	public static String		MIME_ATTACHMENTS			= "ATTACHMENTS";
	
	/**
	 * Java default ISO date format, timezone specified with out separator (E.g 2010-11-24T17:23:10+0100) last time zone part specified with out ':' separtor.
	 * To add separator between hour and minute part of timezone  useTimezoneSeparator set to true.
	 * If useTimezoneSeparator =true  data with ISO format written in json as 2010-11-24T17:23:10+01:00
	 * 
	 * FYI: both with separator (+01:00) or with out separator (+0100) are valid according to ISO date format.
	 * 
	 * <blockquote>
	 * Property name <i>(jsonservice.properties)</i>: <b>json.date.iso.useTimezoneSeparator</b>
	 * </blockquote>
	 * 
	 * Default: false
	 */
	public static boolean 				useTimezoneSeparator 	= false;
	
	/**
	 * Date format used in JSON input and output. All java.util.Date, Calender and Timestamp use specified format. 
	 * For available format look at com.jaxws.json.DateFormat
	 * 
	 * <blockquote>
	 * Property name <i>(jsonservice.properties)</i>: <b>com.jaxws.json.DateFormat</b>
	 * </blockquote>
	 * 
	 * Default: DateFormat.PLAIN
	 */
	public static DateFormat			dateFormat = DateFormat.PLAIN;
	
	/**
	 * Request Payload name (Operation Name) enabled in JSON request. E.g. Operation called "getVersion",  this request object warped with object named "getVersion". 
	 * 
	 * When set to json.request.enable.payloadname=true:
	 * 
	 * Request: 
	 * <code> {"getVersion":{"requestContext":{"token":""}}}</code>
	 * 
	 * When set to json.request.enable.payloadname=false
	 * 
	 * Request: 
	 * <code> {"requestContext":{"token":""}}</code>
	 * <b> When request payload set to false. End point should be appended with operation name.</b>
	 * 
	 * E.g: Endpoint when payload enabled.
	 * .../1_0/settings.json
	 * 
	 * E.g: Endpoint when payload disabled.
	 * .../1_0/settings.json?getVersion&...
	 * 
	 * <blockquote>
	 * Property name <i>(jsonservice.properties)</i>: <b>json.request.enable.payloadname</b>
	 * </blockquote>
	 * 
	 * Default: true
	 */
	//private static boolean				requestPayloadInJSON = true;	
	
	
	/**
	 * Response Payload name (Operation response Name) enabled in JSON response. E.g. Operation called "getVersion" which returns response object "versionInfo". 
	 * This response object warped with operation object. 
	 * 
	 * When set to json.response.enable.payloadname=true:
	 * 
	 * Request: 
	 * <code> {"getVersion":{"requestContext":{"token":""}}}</code>
	 * 
	 * Response:  
	 * <code>{"getVersionXXX":{"versionInfo":{"version":"1.0","buildDate":"1290612232525","releaseDate":"1290612232525"}}}</code>
	 * 
	 * "getVersionXXX" is a response element name defined in wsdl.
	 * 
	 * When set to json.response.enable.payloadname=false
	 * 
	 * Response:  <code>{"versionInfo":{"version":"1.0","buildDate":"1290612232525","releaseDate":"1290612232525"}}</code>
	 * <blockquote>
	 * Property name <i>(jsonservice.properties)</i>: <b>json.response.enable.payloadname</b>
	 * </blockquote>
	 * 
	 * Default: false
	 * 
	 * This property normally true for better automated testing. In application suggested to be false.
	 */
	public static boolean				responsePayloadEnabled	= false;	
    
    /**
     * Null values are ignored in JSON response when set to true.
     * 
     * E.g: when set to false: 	<code>{"versionInfo":{"version":"null","buildDate":"34234234"}}</code>
     *  
     * E.g: when set to true: 	<code>{"versionInfo":{"buildDate":"34234234"}}</code>
     * since value of version is null it is ignored in json output.
     * 
     * <blockquote>
	 * Property name <i>(jsonservice.properties)</i>: <b>json.excludeNullProperties</b>
	 * </blockquote>
	 * 
	 * Default: true
     */
    public static boolean				excludeNullProperties	= true;
    
    /**
     * JSON response written as gzip encoded format. It's dependent on Accept-content: type header in http request. 
     * When set to true response return as gzip if client send Accept-Encoding with gzip true.
     * 
     * <blockquote>
	 * Property name <i>(jsonservice.properties)</i>: <b>json.response.gzip</b>
	 * </blockquote>
	 * 
	 * Default: true
     */
    private static boolean				gzip					= true;	
    
    
    /**
     * List of excluded properties, User can add one or more with regex format.
     * 
     * E.e json.exclude.serialVersionUID=serialVersionUID
     *  
     * <blockquote>
	 * Property name <i>(jsonservice.properties)</i>: <b>json.exclude.xxx</b>
	 * </blockquote>
	 * 
	 * Default: empty. All are included 
     */
    public static Collection<Pattern> excludeProperties 	= new ArrayList<Pattern>();
    
    /**
     * List of included properties, User can add one with regex format. By specifying include property, properties which are not matching to include are ignored in json output.
     *  
     * <blockquote>
	 * Property name <i>(jsonservice.properties)</i>: <b>json.include</b>
	 * </blockquote>
	 * 
	 * Default: empty. 
     */
    public static Collection<Pattern> 	includeProperties	= null;//new ArrayList<Pattern>();
    
    /**
     * In JAXB generated object list sequence generated with warped object. Seralizing it to json end with unnessary objects.
     * To avoid it user can set listWrapperSkip=true.
     * 
     * Eg: 
     *  In XSF
     *  <code>
     *   <xsd:element name="items">
              <xsd:complexType>
                <xsd:sequence>
                  <xsd:element name="item" type="xxxx" maxOccurs="unbounded" minOccurs="1"/>
                </xsd:sequence>
              </xsd:complexType>
            </xsd:element>
     *  </code>
     * 
     * Above XSD lead to java object as
     * 
     * Items{
     * 		List<Item> getItems(){
     * 			return <List>
     * 		}
     * }
     *  
     * 
     * Result JSON representation of items looks like when listWrapperSkip set to false.
     * 
     * {"items":
     * 		{"item":[...]}
     * }
     * 
     * In most case java script developers don't prefer to have two object names. 
     * Result JSON representation of items looks like when listWrapperSkip set to true.
     * 
     * {"items":[..]}
     * 
     * 
     * <blockquote>
	 * Property name <i>(jsonservice.properties)</i>: <b>json.list.wrapperSkip</b>
	 * </blockquote>
	 * 
	 * Default: false. 
     */
    public static 	boolean 					listWrapperSkip 	= false;
    
    /**
     * List of custom encoder used to handle non JSON response.
     * User can register customized encoder like HTML, plain text etc output.
     */
    public static	Collection<Class<? extends Encoder>> customEncoder		= new ArrayList<Class<? extends Encoder>>();
    
    /**
     * List of custom JSON object customizer. Which can be used to customize JSON output of specific object. 
     * E.g an object/Class provided by third party library and its used in JSON encoding/decoding. 
     * And prefer to serialize in custom format.
     */
    private 		Map<Class<? extends Object>,JSONObjectCustomizer> 	jsonObjectCustomizer;
    
    
    /**
     * WS end point configuration object. 
     */
    private 		WSEndpoint<?> 		endpoint;
    
	/**
	 * Binding ID used for codec.
	 */
	private final 	WSBinding 			binding;
	
	/**
	 * Document and model publisher.
	 */
	private HttpMetadataPublisher 		metadataPublisher;
	
	public final 	SOAPVersion 		soapVersion;
    
    static private SEIModel 			staticSeiModel;
    
    public static Pattern 				globalMapKeyPattern = null,globalMapValuePattern = null;
    
    static{
    	LOG.info("Initalizing JSON codec static part.");
    	Properties 		properties 			= new Properties();
    	URL 			serviceProperties 	= JSONCodec.class.getResource("/jsonservice.properties");
    	if(serviceProperties != null){
    		LOG.info("Using JSON service properties from " + serviceProperties);
    		try {
				properties.load(serviceProperties.openStream());
			} catch (Throwable thrown) {
				LOG.log(Level.CONFIG,"property load", thrown);
			}
    	}
    	for(Object key:properties.keySet()){
    		if(key.toString().equals("json.excludeNullProperties")){
    			excludeNullProperties	= Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().startsWith("json.include")){
    			includeProperties.add(Pattern.compile(properties.getProperty(key.toString())));
    		}else if(key.toString().equals("json.response.gzip")){
    			gzip	= Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals("json.response.enable.payloadname")){
    			responsePayloadEnabled	= Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}/*else if(key.toString().equals("json.request.enable.payloadname")){
    			requestPayloadInJSON	= Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}*/else if(key.toString().startsWith("json.exclude")){
    			excludeProperties.add(Pattern.compile(properties.getProperty(key.toString())));
    		}else if(key.toString().equals("json.list.map.key")){
    			globalMapKeyPattern = Pattern.compile(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals("json.list.map.value")){
    			globalMapValuePattern = Pattern.compile(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals("json.list.wrapperSkip")){
    			listWrapperSkip = Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals(com.jaxws.json.codec.DateFormat.class.getName())){
    			dateFormat	= Enum.valueOf(com.jaxws.json.codec.DateFormat.class, properties.getProperty(key.toString()).trim());
    		}else if(key.toString().equals("json.date.iso.useTimezoneSeparator")){
    			useTimezoneSeparator	= Boolean.valueOf(properties.getProperty(key.toString()).trim());
    		}
    	}
		
		for (Encoder handler : ServiceFinder.find(Encoder.class)) {
			customEncoder.add(handler.getClass());
		}
    }
    
	/**
	 * Create codec using SOAP WSBinding
	 * @param binding
	 */
	public JSONCodec(WSBinding binding) {
		this.binding = binding;
		this.soapVersion = binding.getSOAPVersion();
		initCustom();
	}
	
	/**
	 * Copy codec.
	 * 
	 * @param that
	 */
	public JSONCodec(JSONCodec that) {
        this(that.binding);
        this.endpoint 				= that.endpoint;
        this.jsonObjectCustomizer 	= that.jsonObjectCustomizer;
        this.metadataPublisher		= that.metadataPublisher;
    }
	
	/**
	 * Initialize custom JSON serializer
	 */
	private void initCustom() {
		jsonObjectCustomizer = new HashMap<Class<? extends Object>, JSONObjectCustomizer>();
		for (JSONObjectCustomizer serializer : ServiceFinder
				.find(JSONObjectCustomizer.class)) {
			jsonObjectCustomizer.put(serializer.getAcceptClass(), serializer);
		}
	}
	
	/**
	 * 
	 * @return Map of custom serializers.
	 */
	public Map<Class<? extends Object>, JSONObjectCustomizer> getCustomSerializer(){
		return jsonObjectCustomizer;
	}

	@SuppressWarnings("unchecked")
	public void setEndpoint(WSEndpoint endpoint) {
		this.endpoint = endpoint;
        endpoint.getComponentRegistry().add(this);
	}

	
	/* (non-Javadoc)
	 * @see com.sun.xml.ws.api.pipe.Codec#copy()
	 */
	public Codec copy() {
		 return new JSONCodec(this);
	}
	
	/**
	 * @param packet
	 * @return
	 */
	public SEIModel getSEIModel(@NotNull final Packet packet){
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
	
	/** 
	 * Method converts the JSON input into JAX-WS message object. 
	 * @model
	 */
	public void decode(InputStream in, String sContentType, Packet packet)
			throws IOException {
		// Add trace log if X-Debug or TRACE request.
		DebugTrace 		traceLog		= (packet.supports(MessageContext.HTTP_REQUEST_HEADERS) && 
		((Headers)packet.get(MessageContext.HTTP_REQUEST_HEADERS)).containsKey(XDEBUG_HEADER)) ? new DebugTrace() : null;
		
		if(traceLog != null){
			// Add trace log if X-Debug or TRACE request.
			packet.invocationProperties.put(TRACE, traceLog);
			traceLog.info("Request Content-type: " + sContentType);
		}
		//
		Message 		message 		= null;
		com.sun.xml.ws.encoding.ContentType 	contentType		= new com.sun.xml.ws.encoding.ContentType(sContentType);
		if(contentType.getBaseType().equalsIgnoreCase(JSONContentType.JSON_MIME_TYPE)){
			JSONDecoder decoder = new JSONDecoder(this,in,packet);
			if(traceLog != null)traceLog.info("calling json to ws message converter: "+ new Date());
			try{
				message = decoder.getWSMessage();
			}catch(Exception exp){
				if(traceLog != null)traceLog.error(exp.getMessage());
				if(packet.supports(MessageContext.SERVLET_RESPONSE)){
					((HttpServletResponse)packet.get(MessageContext.SERVLET_RESPONSE)).setStatus(400);
				}
				throwMessageCreationException(exp, traceLog);
			}
			if(traceLog != null)traceLog.info("Message decoded successfully: " + new Date());
		} else if(contentType.getBaseType().equalsIgnoreCase(FormDecoder.FORM_MULTIPART) || 
				contentType.getBaseType().equalsIgnoreCase(FormDecoder.FORM_URLENCODED)){
			FormDecoder decoder = new FormDecoder(this,in,packet, contentType);
			if(traceLog != null)traceLog.info("calling FORM data to ws message converter: "+ new Date());
			try{
				message = decoder.getWSMessage();
			}catch(Exception exp){
				if(traceLog != null)traceLog.error(exp.getMessage());
				if(packet.supports(MessageContext.SERVLET_RESPONSE)){
					((HttpServletResponse)packet.get(MessageContext.SERVLET_RESPONSE)).setStatus(400);
				}
				throwMessageCreationException(exp, traceLog);
			}
			if(traceLog != null)traceLog.info("Message decoded successfully: " + new Date());
		} else {
			if(traceLog != null)traceLog.info("Supported content types: " + JSONContentType.JSON_MIME_TYPE);
			if(packet.supports(MessageContext.SERVLET_RESPONSE)){
				((HttpServletResponse)packet.get(MessageContext.SERVLET_RESPONSE)).setStatus(WSHTTPConnection.UNSUPPORTED_MEDIA);
			}
			throwMessageCreationException(new UnsupportedMediaException(), traceLog);
		}
		
		if(message == null){
			if(traceLog != null)
				traceLog.error("Unexpected case in JSON codec. message still empty. Please report");
			packet.put(MessageContext.HTTP_RESPONSE_CODE, new Integer(400)); //BAD request
			message = Messages.createEmpty(soapVersion);
		}
		packet.setMessage(message);
	}
	
	/**
	 * @param exp
	 * @param traceLog
	 * @throws MessageCreationException
	 */
	private void throwMessageCreationException(Exception exp, final DebugTrace traceLog) throws MessageCreationException{
		if(traceLog == null){
			throw new MessageCreationException(this.soapVersion,exp);
		}else{
			throw new MessageCreationException(this.soapVersion,exp){
				private static final long serialVersionUID = 1L;

				public Message getFaultMessage() {
					return new TrackedMessage(super.getFaultMessage(),traceLog);
				}
			};
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.xml.ws.api.pipe.Codec#decode(java.nio.channels.ReadableByteChannel, java.lang.String, com.sun.xml.ws.api.message.Packet)
	 */
	public void decode(ReadableByteChannel arg0, String arg1, Packet arg2) {
		throw new UnsupportedOperationException();
	}

	/** 
	 * Method converts JAX-WS message to JOSN and write it in output stream object. 
	 * @model
	 */
	public ContentType encode(Packet packet, OutputStream out) throws IOException {
		JSONEncoder encoder = new JSONEncoder(packet,this);
		if(gzip && packet.supports(MessageContext.SERVLET_REQUEST) && isGzipInRequest((HttpServletRequest)packet.get(MessageContext.SERVLET_REQUEST))){
			((HttpServletResponse)packet.get(MessageContext.SERVLET_RESPONSE)).addHeader("Content-Encoding", "gzip");
			out = new GZIPOutputStream(out);
		}
		try {
			// MessageContext.MESSAGE_OUTBOUND_PROPERTY set by JAX_WS only if handler configured. But encode always a out bound.
			packet.invocationProperties.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY,true); 
			return encoder.encode(out);
		} finally {
			if (out != null && out instanceof GZIPOutputStream) {
				try {
					out.close();
				} catch (Exception xe) {
					// let the original exception get through
				}
			}
		}
					
						/*String inEx[][] = getInExProperties(methodImpl);
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
						listMapValue = getListMapValue(methodImpl);*/
	}
	
	/**
	 * 
	 * @param methodImpl
	 * @return List of properties need to be excluded specific to operation call. 
	 * This values defined in implementation operation JSONWebService annotation.
	 * 
	 */
	public static String[][] getInExProperties(JavaMethodImpl methodImpl){
		JSONWebService jsonService = methodImpl.getMethod().getAnnotation(JSONWebService.class);
		if(jsonService != null){
			return new String[][]{jsonService.includeProperties(),jsonService.excludeProperties()};
		}
		// default codec level
		return new String[][]{{},{}};
	}

	/* (non-Javadoc)
	 * @see com.sun.xml.ws.api.pipe.Codec#encode(com.sun.xml.ws.api.message.Packet, java.nio.channels.WritableByteChannel)
	 */
	public ContentType encode(Packet packaet, WritableByteChannel byteChannel) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.sun.xml.ws.api.pipe.Codec#getMimeType()
	 */
	public String getMimeType() {
		return JSONContentType.JSON_MIME_TYPE;
	}

	/* (non-Javadoc)
	 * @see com.sun.xml.ws.api.pipe.Codec#getStaticContentType(com.sun.xml.ws.api.message.Packet)
	 * 
	 * TIP 1: by sending accept http header with value "application/json" increase performance on finding relavent decoder.
	 */
	public ContentType getStaticContentType(Packet packet) {
		/**
		 * Check accept content type is not present.
		 * 
		 * TODO header case sensitive. Multiple header values handling.
		 */
		if(packet != null && 
				packet.invocationProperties != null){
			if(packet.invocationProperties.containsKey(FORCED_RESPONSE_CONTENT_TYPE)){
				return (ContentType)packet.invocationProperties.get(FORCED_RESPONSE_CONTENT_TYPE);
			}else if(packet.invocationProperties.containsKey("accept")
				&& (!packet.invocationProperties.get("accept").equals(JSONContentType.JSON_MIME_TYPE))){
				// Accept content type is not JSON.
				// Test is this accept content type handled by custom response package handlers.
				// Example case: JSON request result with HTML/CSV/PDF etc response.
				/* FIXME if(customEncoder.containsKey(packet.invocationProperties.get("accept"))){
					return customEncoder.get(packet.invocationProperties.get("accept")).contentType();
				}*/
				//Worst perform
				Module modules = endpoint.getContainer().getSPI(com.sun.xml.ws.api.server.Module.class);
				//TODO document more here and increase performance.
				for(BoundEndpoint endPointObj : modules.getBoundEndpoints()){
					if(endPointObj.getEndpoint().getImplementationClass().equals(endpoint.getImplementationClass())
							&& endPointObj.getEndpoint().getBinding().getBindingId() != binding.getBindingId()){
						Codec 		codec 		=	endPointObj.getEndpoint().createCodec();
						ContentType contentType = 	codec.getStaticContentType(packet);
						if(contentType != null && contentType.getContentType().startsWith(packet.invocationProperties.get("accept").toString())){
							return endPointObj.getEndpoint().createCodec().getStaticContentType(packet);
						}
					}
				}
			}
		}
		return jsonContentType;
	}

	/**
	 * @return
	 */
	public SOAPVersion getSoapVersion() {
		return soapVersion;
	}

	public WSEndpoint<?> getEndpoint() {
		return endpoint;
	}
	
	public WSBinding getBinding() {
		return binding;
	}

	private static boolean isGzipInRequest(HttpServletRequest request) {
        String header = request.getHeader("Accept-Encoding");
        return header != null && header.indexOf("gzip") >= 0;
    }
	
	/**
	 * Meta data publisher for get request.
	 */
	public @Nullable <T> T getSPI(@NotNull Class<T> type) {
		if (type == HttpMetadataPublisher.class) {
			// Overwrite http end point document provider.
			if (metadataPublisher == null)
				metadataPublisher = new JSONHttpMetadataPublisher(endpoint, this);
			return type.cast(metadataPublisher);
		}
		return null;
	}
	/**
	 * @param buf
	 * @param caption
	 * @param headers
	 * @throws IOException
	 */
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
