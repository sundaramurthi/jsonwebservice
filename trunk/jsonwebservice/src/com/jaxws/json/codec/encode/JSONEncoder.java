package com.jaxws.json.codec.encode;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
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
	@SuppressWarnings("unchecked")
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
		 * Read and remove the forced content type don't end in response body part. 
		 */
		ContentType contentType = (ContentType) invocationProperties.remove(JSONCodec.FORCED_RESPONSE_CONTENT_TYPE);
		/*
		 * DEFAULT JSON output
		 */
		final HashMap<String, Object> 	responseJSONMap = new HashMap<String, Object>();
		final WSJSONWriter 				writer 			= new WSJSONWriter(output, responseJSONMap, this.codec.getCustomSerializer());
		
		// Add custom invocation properties to JSON output.
		/*
		 * Step 3: add all custom output content, log trace, status etc
		 */
		for (Entry<String, Object> property : invocationProperties.entrySet()) {
			if(MessageContext.MESSAGE_OUTBOUND_PROPERTY.equals(property.getKey()) ||
					JSONCodec.globalMapKeyPattern_KEY.equals(property.getKey()) ||
					JSONCodec.globalMapValuePattern_KEY.equals(property.getKey()))
				continue;
			responseJSONMap.put(property.getKey(), property.getValue());
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
						for (Iterator<Element> iterator = faultObj.getDetail().getChildElements(); iterator.hasNext();) {
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
							responseJSONMap.put(payload,invocationProperties.remove(JSONCodec.JSON_MAP_KEY));
						}else{
							responseJSONMap.putAll((Map<String, ? extends Object>) invocationProperties.remove(JSONCodec.JSON_MAP_KEY));
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
		
		/*
		 * In case of multipart mime body add boundry header
		 */
		if(contentType != null && contentType == JSONContentType.MULTIPART_MIXED){
			output.write(JSONContentType.BOUNDARY.getBytes());
			output.write(("\nContent-Type: "+JSONContentType.TEXT_PLAIN.getContentType()+"\n\n").getBytes());
		}
		// JSON data write.
		writer.write(JSONCodec.dateFormat,JSONCodec.excludeProperties,
				JSONCodec.includeProperties,
				(Pattern) invocationProperties.get(JSONCodec.globalMapKeyPattern_KEY),
				JSONCodec.globalMapValuePattern);
		
		//Process all response attachments
		for(Map<String, Object> attachInfo : writer.getAttachments()){
			Object value = attachInfo.remove("value");
			if (value != null) {
				String mimeType = (String)attachInfo.remove("mimeType");
				output.write(JSONContentType.BOUNDARY.getBytes());
				output.write(String.format("\nContent-Type: %s",mimeType).getBytes());
				output.write(String.format("\nContent-Disposition: attachment; name=\"%s\"; filename=\"%s.%s\"\n\n",
						attachInfo.get("name"),attachInfo.get("name"),
						mimeType.split("/")[mimeType.indexOf('/') > -1? 1 : 0]).getBytes());
				// TODO DataHandler and Source
				if (Image.class.isAssignableFrom(value.getClass())) {
					if (mimeType == null || mimeType.startsWith("image/*"))
						mimeType = "image/png";
					Iterator<ImageWriter> itr = ImageIO
							.getImageWritersByMIMEType(mimeType);
					if (itr.hasNext()) {
						ImageWriter w = itr.next();
                        w.setOutput(ImageIO.createImageOutputStream(output));
						w.write(convertToBufferedImage((Image)value));
						w.dispose();
					} else {
						// LOG no handler
					}
				}else if(DataHandler.class.isAssignableFrom(value.getClass())){
					((DataHandler)value).writeTo(output);
				}else if(Source.class.isAssignableFrom(value.getClass())){
					//TODO ((Source)value).writeTo(output);
				}
			}
		}
		
		if(traceEnabled) traceLog.info("JSON response encoding completed." + new Date());
		return JSONCodec.jsonContentType;
	}
	
	private BufferedImage convertToBufferedImage(Image image) throws IOException {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;

        } else {
            @SuppressWarnings("serial")
			MediaTracker tracker = new MediaTracker(new Component(){}); // not sure if this is the right thing to do.
            tracker.addImage(image, 0);
            try {
                tracker.waitForAll();
            } catch (InterruptedException e) {
                throw new IOException(e.getMessage());
            }
            BufferedImage bufImage = new BufferedImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);

            Graphics g = bufImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            return bufImage;
        }
    }
}
