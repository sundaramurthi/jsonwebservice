package com.jaxws.json.codec.decode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;

import org.jvnet.mimepull.MIMEPart;

import com.jaxws.json.codec.DebugTrace;
import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.JSONFault;
import com.jaxws.json.codec.encode.JSONResponseBodyBuilder;
import com.sun.xml.bind.StringInputStream;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

/**
 * @author ssaminathan
 * @version 1.0
 */
public class JSONDecoder {

	private static final String GET = "GET";
	
	private JSONCodec		codec;			
	/**
	 * Request stream
	 */
	private InputStream 	input; 
	
	/**
	 * Request Message holder.
	 */
	private Packet 			packet;
	
	/**
	 * WSHTTPConnection servlet request, response and headers holder.
	 */
	private WSHTTPConnection connection;
	
	/**
	 * Request query string
	 */
	private String 			queryString;
	/**
	 * X-Debug or http TRACE sends trace log to client.
	 */
	private DebugTrace 		traceLog		= null;
	
	/**
	 * @param input
	 * @param contentType
	 * @param packet
	 */
	public JSONDecoder(JSONCodec codec, InputStream input,Packet packet) {
		super();
		if(input == null || packet == null){
			throw new RuntimeException("Invalid parameters to JOSN Decoder.");
		}
		this.codec			= codec;
		this.input 			= input;
		this.packet 		= packet;
		if(packet.webServiceContextDelegate != null 
				&& packet.webServiceContextDelegate instanceof WSHTTPConnection){
			this.connection = (WSHTTPConnection)packet.webServiceContextDelegate;
			this.queryString = connection.getQueryString();
		}
		traceLog = (DebugTrace) packet.invocationProperties.get(JSONCodec.TRACE);
		if(traceLog != null)
			traceLog.info("JSONDecoder initalized");
	}
	
	/**
	 * @return WS message object.
	 * @throws UnsupportedEncodingException 
	 * @throws JSONException 
	 */
	@SuppressWarnings("unchecked")
	public com.sun.xml.ws.api.message.Message getWSMessage() throws UnsupportedEncodingException{
		if(traceLog != null)
			traceLog.info("decoding JSON content started: " + new Date());
		/* 
		 * Step 1: check http method.  
		 * If method is GET use query string as input stream for JSON input. Else use post body.
		 **/
		InputStream input = isJSONGetRequest() ? getQueryStringInputStream() : this.input;
		
		if(traceLog != null)
			traceLog.info("Converting json input as string buffer");
		
		/*
		 * Step 2: Convert input stream into string buffer to read as JSON object.
		 */
		StringBuilder 	jsonBuffer 		= new StringBuilder();
		BufferedReader 	bufferReader 	= new BufferedReader(new InputStreamReader(input));
		try {
			String line = null;
			while ((line = bufferReader.readLine()) != null) {
				jsonBuffer.append(line);
	        }
	    } catch(IllegalStateException exp){
	    	// TODO on multipart
	    }catch (Exception e) {
	    	if(traceLog != null){
	    		traceLog.error("Exception occured while reading JSON inpu as string");
	    		traceLog.info("Error detail: Lines read successfully: " + jsonBuffer.toString());
	    		traceLog.info("Error detail: Exception message" + e.getMessage());
	    	}
	    	throw new JSONFault("Client",jsonBuffer.toString(),e.getMessage(),null);
	    }finally{
	    	try {
	    		if(traceLog != null)
	    			traceLog.info("JSON input reader closed");
	    		input.close();
				bufferReader.close();
			} catch (Exception e) {}
	    }
	    
	    /*
	     * Step 3: If request method in post and operation name located in query string, then append 
	     * operation name part of query string. 
	     * 
	     * Refer getQueryStringInputStream() "else" condition in case of GET method
	     */
	    if(queryString != null && !queryString.isEmpty() && !isJSONGetRequest()){
	    	if(traceLog != null)
	    		traceLog.info("POST method with query string added. Opeartion name identified from first parameter");
			String params[] = queryString.split("&");
			// http post request: Operation name should be first parameter if query string present.
			if(params.length > 0 && params[0].indexOf('=') == -1){
				jsonBuffer.insert(0, ("{\"" + params[0] +"\":"));
				jsonBuffer.append("}");
			}
		}
	    
	    /*
	     * Step 4: Parsing input JSON string into java Object.
	     */
	    if(traceLog != null)
	    	traceLog.info("Parsing input JSON string into java Object");
	    
	    Object inputJSON = new JSONReader().read(jsonBuffer.toString());
	    
	    /*
	     * Step 5: If input json parsed successfully by JSON reader and is it is map the process as operation.
	     */
	    if(inputJSON != null && inputJSON instanceof Map){
	    	if(traceLog != null)
	    		traceLog.info("Input JSON parsed successfully as plain map");
	    	/********************************************************************************/
	    	/* ACTUAL JSON TO JAVA CONVERSION HAPPEN HERE									*/
	    	/********************************************************************************/
	    	Map<String, Object> requestJSONMap = (Map<String, Object>) inputJSON;
	    	
	    	/*
	    	 * Step 5.1 check is it comming from json service, with status flag.
	    	 */
	    	if(requestJSONMap.containsKey(JSONCodec.STATUS_STRING_RESERVED)){
	    		if(traceLog != null)
	    			traceLog.info("Input identified as direct output of jsonservice since it has " + JSONCodec.STATUS_STRING_RESERVED);
	    		
	    		// Output of JSON service and its status is false, then create fault message.
				if(!new Boolean(requestJSONMap.get(JSONCodec.STATUS_STRING_RESERVED).toString())){
					return new com.sun.xml.ws.message.FaultMessage(Messages.createEmpty(this.codec.getSoapVersion()),null);
				}
				// Remove codec set value WARN user should not used codec specific key
				requestJSONMap.remove(JSONCodec.STATUS_STRING_RESERVED);
			}
			
	    	if(traceLog != null)
	    		traceLog.info("Reading operation using service endpoint definition");
	    	/*
			 * Step 5.2: access SEI model to identify operation definition. 
			 */
			SEIModel 			seiModel 	= codec.getSEIModel(this.packet);
			JAXBContextImpl 	context 	= (JAXBContextImpl)seiModel.getJAXBContext();
			
			/*
			 * Step 5.3: Identify method definition using operation/payload name.
			 */
			for(Object payload : requestJSONMap.keySet()){
				if(payload.equals(MessageContext.MESSAGE_OUTBOUND_PROPERTY))
					continue;
				// payload string can be operation request name or operation response name.
				JavaMethodImpl methodImpl = codec.getJavaMethodUsingPayloadName(seiModel,payload.toString());
				if(methodImpl == null){
					if(traceLog != null){
						traceLog.error("Operaion unknown in this endpoint. " + payload);
			    		String methods = "";
			    		for(com.sun.xml.ws.api.model.JavaMethod method : seiModel.getJavaMethods()){
			    			methods += method.getOperationName() + ",";
			    		}
			    		traceLog.info("Available operations " + methods);
					}
					throw new JSONFault("Client","Unknown payload/operation " + payload, "Codec", null);
				}
				/*
				 * Step 5.4: Identify method definition using operation/payload name.
				 */
				if(methodImpl.getOperationName().equals(payload)){
					// TEST HIT 2
					// PRODUCTION HIT 1
					// Decode as Request
					return new JSONRequestBodyBuilder(this.codec).createMessage(methodImpl, requestJSONMap, context,(List<MIMEPart>)this.packet.invocationProperties.get(JSONCodec.MIME_ATTACHMENTS),
							traceLog != null, traceLog);
				}else{
					// TEST HIT 4 END
					//Decode as Response
					// Should happen only in TEST decoder
					return new JSONResponseBodyBuilder(this.codec).createMessage(methodImpl, requestJSONMap, context,(List<MIMEPart>)this.packet.invocationProperties.get(JSONCodec.MIME_ATTACHMENTS),
							traceLog != null, traceLog);
				}
			}
			if(traceLog != null)
	    		traceLog.add("ERROR-JSON-DECODER" , "Unknown payload/operation in json request");
			throw new JSONFault("Client","Unknown payload/operation in json request","Codec",null);
	    }else{
	    	if(traceLog != null) traceLog.error("Input JSON parsing failed. parse result : " + String.valueOf(jsonBuffer));
	    	throw new JSONFault("Client","Invalid JSON input : " + jsonBuffer.toString(),"Codec",null);
	    }
	}
	
	
	/**
	 * Utility method to check is it HTTP get method.
	 * @return
	 */
	private boolean isJSONGetRequest(){
		return connection != null && connection.getRequestMethod().equals(GET);
	}
	
	/**
	 * Utility method.
	 * 
	 * @return Query string as Input stream.
	 * @throws UnsupportedEncodingException
	 */
	private InputStream getQueryStringInputStream() throws UnsupportedEncodingException{
		if(connection == null || queryString == null || queryString.isEmpty()){
			throw new RuntimeException("Invalid Operation name in query parameter.(Please make payload name enabled or pass valid opeartion as query parameter)");
		} else {
			String queryStringUpdated = this.queryString;
			if(!queryString.trim().startsWith("{")){
				// JSON input as operation parameter
				String params[] = queryString.split("&");
				if(params.length > 0){
					String paramValue[] = params[0].split("=");
					if(paramValue.length == 2){
						queryStringUpdated = String.format("{\"%s\":%s}", params[0],params[1]);
					}else{
						throw new RuntimeException("Invalid Operation name in query parameter.(Please pass valid opeartion name as query parameter)");
					}
				}else{
					throw new RuntimeException("Invalid Operation name in query parameter.(Please pass valid opeartion name as query parameter)");
				}
			}
			return new StringInputStream(URLDecoder.decode(queryStringUpdated,System.getProperty("javaEncoding", "UTF-8")));
		}
	}
}
