package com.jaxws.json.codec.decode;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.jvnet.mimepull.MIMEMessage;
import org.jvnet.mimepull.MIMEPart;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.JSONContentType;
import com.jaxws.json.codec.JSONFault;
import com.jaxws.json.codec.MessageBodyBuilder;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentDisposition;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ParseException;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.encoding.ContentType;
import com.sun.xml.ws.transport.Headers;

/**
 * @author ssaminathan
 *
 */
public class FormDecoder {
	public 	static final String FORM_MULTIPART 		= "multipart/form-data";
	public 	static final String FORM_URLENCODED 	= "application/x-www-form-urlencoded";
	
	/**
	 * Codec instance to read JSON configuration properties, date format etc.
	 */
	private JSONCodec 		codec;
	/**
	 * Input stream with/out attachment.
	 */
	private InputStream 	input;
	/**
	 * JAXB read packet to get trace log.
	 */
	private Packet 			packet;
	/**
	 * Request content type either multipart/form-data or application/x-www-form-urlencoded with boundtry or encoding 
	 */
	private ContentType		contentType;
	
	private final String	JSON_PARAM_NAME;
	/**
	 * @param codec
	 * @param in
	 * @param packet
	 * @param contentType
	 */
	public FormDecoder(JSONCodec codec, InputStream in, Packet packet,
			ContentType contentType) {
		if(!(contentType.getBaseType().equalsIgnoreCase(FORM_MULTIPART) || 
				contentType.getBaseType().equalsIgnoreCase(FORM_URLENCODED))){
			throw new RuntimeException("Invalid contenty type. FormDecoder handle only " + FORM_MULTIPART + " " + FORM_URLENCODED);
		}
		this.codec 			= codec;
		this.input 			= in;
		this.packet 		= packet;
		this.contentType 	= contentType;
		this.JSON_PARAM_NAME =  (packet.supports(MessageContext.HTTP_REQUEST_HEADERS) && 
					((Headers)packet.get(MessageContext.HTTP_REQUEST_HEADERS)).getFirst(JSONCodec.XJSONPARAM_HEADER) != null)? 
					((Headers)packet.get(MessageContext.HTTP_REQUEST_HEADERS)).getFirst(JSONCodec.XJSONPARAM_HEADER) :
						JSONCodec.XJSONPARAM_DEFAULT;
	}
	
	/**
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public Message getWSMessage() throws UnsupportedEncodingException {
		if(contentType.getBaseType().equalsIgnoreCase(FORM_MULTIPART)){
			return getMultiPartMessageWithAttachement();
		}else{
			return getFormData();
		}
	}

	/**
	 * @return Message with from data.
	 * @throws UnsupportedEncodingException 
	 */
	private Message getMultiPartMessageWithAttachement() throws UnsupportedEncodingException {
		if(input != null){
		    String boundary = contentType.getParameter("boundary");
	        if (boundary == null || boundary.equals("")) {
	            throw new WebServiceException("MIME boundary parameter not found" + contentType);
	        }
		       
	        MIMEMessage message =  new MIMEMessage(input, boundary);
	        MIMEPart	jsonPart	= null;    
			for(MIMEPart mimeAttach : message.getAttachments()){
				if(mimeAttach.getContentType().equalsIgnoreCase(JSONContentType.JSON_MIME_TYPE)) {
					jsonPart	= mimeAttach;
					break;
				} else {
					List<String> contentDisposition = mimeAttach.getHeader(JSONCodec.CONTENT_DISPOSITION_HEADER);
					if(contentDisposition != null && contentDisposition.size() > 0){
						try {
							ContentDisposition disp = new ContentDisposition(contentDisposition.get(0));
							if(disp.getParameter("name") != null && disp.getParameter("name").equals(JSON_PARAM_NAME)){
								jsonPart = mimeAttach;
								break;
							}
						} catch (ParseException e) {}
					}
				}
			}
			if(jsonPart == null){
				throw new RuntimeException(String.format("There is no request JSON found in multipart mime. Your JSON input http \"parameter\"" +
						" must be named as \"%s\" or pass %s: <Your json param name>. In alternate case body part content type should be %s " +
						" json content as mime part body.", JSONCodec.XJSONPARAM_DEFAULT, JSONCodec.XJSONPARAM_HEADER, JSONContentType.JSON_MIME_TYPE));
			}
			List<MIMEPart> attachments = message.getAttachments();
			/*
			 * Remove JSON part from attachment list. JSON part reseved for codec. and handled by JSONDecoder.
			 * 
			 */
			attachments.remove(jsonPart);
			/*
			 * Put attachemnts into invoke properties of packet.
			 */
			this.packet.invocationProperties.put(JSONCodec.MIME_ATTACHMENTS,attachments);
			/*
			 * Decode message
			 */
			com.sun.xml.ws.api.message.Message wsMessage = new JSONDecoder(this.codec,jsonPart.readOnce(),packet).getWSMessage();
			/*
			 * Remove attachment. Else same packet object used in response. It leads attachment fall back in response.
			 */
			this.packet.invocationProperties.remove(JSONCodec.MIME_ATTACHMENTS);
			/*
			 * TODO
			 * Its weared all form post leads to multipart response. Invetigate and find better response type for form post.
			 * In case of application/json form post leads popup in browser.
			 * So multipart or text/plain prefred by user. But when XMLHttp support attachment, its good to send application/json or 
			 * multi part mime.
			 * 
			 */
			if(this.packet.invocationProperties.get(JSONCodec.ENCODER) == null){
				this.packet.invocationProperties.put(JSONCodec.FORCED_RESPONSE_CONTENT_TYPE,JSONContentType.MULTIPART_MIXED);
			}
			return wsMessage;
		} else {
			throw new RuntimeException("Multipart form data should be POST. No input stream found.");
		}
	}
	
	/**
	 * @return
	 */
	private Message getFormData() {
		ServletRequest request = (ServletRequest) this.packet.get(MessageContext.SERVLET_REQUEST);
		
		@SuppressWarnings("unchecked")
		Map<String,Object> parameter = request.getParameterMap();
		Map<String,Object> jsonMap	= new HashMap<String, Object>();
		for(Entry<String, Object> paramEntry : parameter.entrySet()){
			fillParameterMap(paramEntry.getKey(),request.getParameter(paramEntry.getKey()), jsonMap);
		}
		try {
			if(jsonMap.size() == 1){
				// Must be only one operation
				packet.invocationProperties.put(JSONCodec.FORCED_RESPONSE_CONTENT_TYPE, null);
				Entry<String, Object> operation = jsonMap.entrySet().iterator().next();
				this.packet.invocationProperties.put(JSONCodec.JSON_MAP_KEY, operation.getValue());
				return new MessageBodyBuilder(this.codec).handleMessage(this.packet,operation.getKey());
			}else{
				throw new RuntimeException("More than one operation found");
			}
		} catch (Exception e) {
			throw new JSONFault("Client","Failed to create message body."+e.getMessage(),"MessageBody Builder",null,e);
		}	
		
	}
	
	private void fillParameterMap(String parameter,Object value,Map<String,Object> mapValue){
		if(parameter.indexOf('.') > -1){
			String key = parameter.substring(0, parameter.indexOf('.'));
			// TODO IF key is idex like 0..x then its array
			@SuppressWarnings("unchecked")
			Map<String,Object> jsonMap	= mapValue.containsKey(key)? (Map<String,Object>)mapValue.get(key) : new HashMap<String, Object>();
			if(!mapValue.containsKey(key))
				mapValue.put(key, jsonMap);
			fillParameterMap(parameter.substring(parameter.indexOf('.') + 1), value, jsonMap);
		}else{
			mapValue.put(parameter, value);
		}
	}
}


