package com.jaxws.json.codec.encode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.jaxws.json.codec.DebugTrace;
import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.JSONContentType;
import com.jaxws.json.codec.JSONFault;
import com.jaxws.json.codec.MessageBodyBuilder;
import com.jaxws.json.codec.TrackedMessage;
import com.jaxws.json.packet.handler.Encoder;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.server.WSEndpoint;

/**
 * @author ssaminathan
 *
 */
public class JSONEncoder {
	private static final Logger LOG			= Logger.getLogger(JSONEncoder.class.getName());
	
	private static final String ACCEPT 		= "accept";
	private static final String SOAPACTION 	= "SOAPAction";
	private static final String FAULT 		= "fault";
	/**
	 * WS Packet with message to send response.
	 */
	private Packet packet;
	
	/**
	 * Codec
	 */
	private JSONCodec codec;
	/**
	 * Flag for Request with process track enabled or disabled
	 */
	private boolean traceEnabled;
	
	/**
	 * Trace information. 
	 */
	private DebugTrace traceLog;
	/**
	 * @param packet
	 */
	public JSONEncoder(Packet packet,JSONCodec codec) {
		super();
		if(packet == null){
			throw new RuntimeException("Invalid packet null");
		}
		this.packet 		= packet;
		this.codec 			= codec;
		this.traceEnabled	= packet.invocationProperties.containsKey(JSONCodec.TRACE);
		this.traceLog 		= (DebugTrace)packet.invocationProperties.get(JSONCodec.TRACE);
		
	}
	
	/**
	 * @param output
	 * @return 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 * @throws XMLStreamException 
	 * @throws JAXBException 
	 * @throws JSONException 
	 */
	public ContentType encode(OutputStream output) throws IOException {
		if(traceEnabled)traceLog.info("Response encoding started." + new Date());
		/*
		 * Step 1: check is the accept content type is JSON if not search requested content type 
		 * can be handled by custom response handler. If yes pass it to custom handler. Else find JAX-WS endpoint can handle it. 
		 * If yes pass it to JAX-WS endpoint.
		 */
		Map<String,Object> invocationProperties = this.packet.invocationProperties;
		if(invocationProperties != null && invocationProperties.containsKey(ACCEPT)
				&& (!invocationProperties.get(ACCEPT).equals(JSONContentType.JSON_MIME_TYPE))){
			//1.1  Accept content type not json then attempt to find in custom codec
			for(Class <? extends Encoder> encoder : JSONCodec.customEncoder){
				Encoder encoderInstance = null;
				try {
					encoderInstance = encoder.newInstance();
				} catch (Exception e) {// Log instance creation failure. 
					if(this.traceEnabled)this.traceLog.error("Custom encoder instantation failed. " +
							"Ignoring custom encoder "+encoder.getName());
					LOG.log(Level.WARNING, "Failed to create custom encoder "+e.getMessage());
					LOG.log(Level.INFO,"Instance creation fail",e);
					continue;
				} 
				ContentType contentTypeHandled 	= encoderInstance.contentType();
				/*
				 * Custom encoder identified based on 
				 *  FISRT : 1.1.1 custom Accept header present in HTTP request.
				 *  SECOND: 1.1.2 SOAP action header starts with Encoder specified SOAP action header.
				 *  THIRD : 1.1.3 http accept header not JSON and supported by Content type string specified Encoder. 
				 */
				if(invocationProperties.containsKey(contentTypeHandled.getAcceptHeader())){// case 1 check custom http header present.
					return encoderInstance.encode(this.packet, output);
				}else if(invocationProperties.containsKey(SOAPACTION) && 
						invocationProperties.get(SOAPACTION).toString().startsWith(contentTypeHandled.getSOAPActionHeader())){
					return encoderInstance.encode(this.packet, output);
				}else if(invocationProperties.get(ACCEPT).equals(contentTypeHandled.getContentType())){
					return encoderInstance.encode(this.packet, output);
				}
			}
			//end
			//1.2 Acccept content type herader present and not JSON and its not handled by custom Encoder.
			// Try it in other JAX-WS codec. Eg: accept text/xml can be handled by SOAPCodec 
			// Read all service end point modules declared in container.
			WSEndpoint<?> 	endPoint 	= this.codec.getEndpoint();
			Module 			modules 	= endPoint.getContainer().getSPI(com.sun.xml.ws.api.server.Module.class);
			WSBinding 		jsonBinding = this.codec.getBinding();
			// TODO cache
			for(BoundEndpoint endPointObj : modules.getBoundEndpoints()){
				// Check is this End ponit using same service interface 
				if(endPointObj.getEndpoint().getImplementationClass().equals(endPoint.getImplementationClass())
						&& endPointObj.getEndpoint().getBinding().getBindingId() != jsonBinding.getBindingId()){
					Codec 		codec 		= endPointObj.getEndpoint().createCodec();
					ContentType contentType = codec.getStaticContentType(packet);
					if(contentType != null && 
							contentType.getContentType().startsWith(packet.invocationProperties.get(ACCEPT).toString())){
						return endPointObj.getEndpoint().createCodec().encode(packet, output);
					}
				}
			}
		/*
		 * Step 2: Accept content type not json as well custom encoder or other WS end points not handled this request.
		 * Since request reached to JSON codec endpoint send response as JSON.
		 **/
		}
		
		/*
		 * DEFAULT JSON output
		 */
		final HashMap<String, Object> 	responseJSONMap = new HashMap<String, Object>();
		final WSJSONWriter 				writer 			= new WSJSONWriter(output, responseJSONMap, this.codec.getCustomSerializer());
		
		// Add custom invocation properties to JSON output.
		/*
		 * Step 3: add all custom output content, log trace, status etc
		 */
		for (Iterator<String> iterator = packet.invocationProperties.keySet().iterator(); iterator.hasNext();) {
			String type = iterator.next();
			if(MessageContext.MESSAGE_OUTBOUND_PROPERTY.equals(type))
				continue;
			responseJSONMap.put(type.toString(),packet.invocationProperties.get(type));
		}
		
		final Message 				message 			= packet.getMessage();
		
		/*
		 * Set fault status
		 */
		responseJSONMap.put(JSONCodec.STATUS_STRING_RESERVED, !(message == null || message.isFault()));
		
		/*
		 * Step 4: Check is there message If there is message process it.
		 */
		if (message != null) {
			if(message instanceof TrackedMessage){
				responseJSONMap.put(JSONCodec.TRACE, ((TrackedMessage)message).getTrace());
			}
			/*
			 * Step 4.1: If message is fault message, set status to false and send fault information.
			 */
			if (message.isFault()) {
				// Access SOAP fault.
				try {
					SOAPFault faultObj = message.readAsSOAPMessage().getSOAPBody().getFault();
					HashMap<String,String> detail = new HashMap<String, String>(); 
					try {
						for (Iterator<Element> iterator = faultObj.getDetail().getChildElements(); iterator
								.hasNext();) {
							Element type = iterator.next();
							detail.put(type.getLocalName(), type.getTextContent());
							for(int att = type.getAttributes().getLength() -1;att >-1;att--){
								Node node = type.getAttributes().item(att);
								detail.put(node.getNodeName(), node.getNodeValue());
							}
						}
					} catch(Throwable th){/*Dont mind about custom message set fail*/}
					responseJSONMap.put(FAULT,new JSONFault(faultObj.getFaultCodeAsQName().getLocalPart().toUpperCase(),
							faultObj.getFaultString(),faultObj.getFaultActor(),detail));
					
				} catch (SOAPException e) {
					responseJSONMap.put(FAULT, new JSONFault("Server.json",
							"Failed to read soap fault message","Codec",null));
				}
			}else{
				final String 	payload		= message.getPayloadLocalPart();
				/*
				 * Step 4.2: message is not fault. valid json response
				 */
				if(MessageBodyBuilder.CAN_HANDLE_RESPONE){
					try {
						new MessageBodyBuilder(this.codec).handleMessage(this.packet,payload);
						if(JSONCodec.responsePayloadEnabled){
							responseJSONMap.put(payload,this.packet.invocationProperties.remove(JSONCodec.JSON_MAP_KEY));
						}else{
							responseJSONMap.putAll((Map<? extends String, ? extends Object>) this.packet.invocationProperties.remove(JSONCodec.JSON_MAP_KEY));
						}
					} catch (Exception e1) {
						responseJSONMap.put(JSONCodec.STATUS_STRING_RESERVED,false);
						responseJSONMap.put(FAULT, new JSONFault("Client.json",
								"Unknown payload name: " + message.getPayloadLocalPart(),"Codec",null));
					}
				} else {
					// Until JAX_WS 2.0.1 MessageBodyBuilder.CAN_HANDLE_RESPONE is true, can read using reflection
					throw new RuntimeException("Encoding can only handled on JAXBMessage which has jaxbObject.");
				}
			}
			
		} else {
			responseJSONMap.put(JSONCodec.STATUS_STRING_RESERVED,false);
			/*
			 * Step 4.2: message in packet is null
			 */
			responseJSONMap.put(FAULT, new JSONFault("Server.json",
					"Invalid message null. Try with \"X-Debug: true\" header for more trace","Server",null));
		}
		/*
		 * Step 5: Write processed message map to output stream
		 */
		if(traceEnabled) traceLog.info("Writing json respone from map");
		writer.write(JSONCodec.dateFormat,JSONCodec.excludeProperties,
				JSONCodec.includeProperties,null,null);
		
		if(traceEnabled) traceLog.info("JSON response encoding completed." + new Date());
		return JSONCodec.jsonContentType;
	}
}
