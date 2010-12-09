package com.jaxws.json.codec.decode;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.jvnet.mimepull.MIMEMessage;
import org.jvnet.mimepull.MIMEPart;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.JSONContentType;
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
			this.packet.invocationProperties.put(JSONCodec.MIME_ATTACHMENTS,message.getAttachments());
			com.sun.xml.ws.api.message.Message wsMessage = new JSONDecoder(this.codec,jsonPart.readOnce(),packet).getWSMessage();
			this.packet.invocationProperties.put(JSONCodec.FORCED_RESPONSE_CONTENT_TYPE,JSONContentType.TEXT_PLAIN);
			return wsMessage;
		} else {
			throw new RuntimeException("Multipart form data should be POST. No input stream found.");
		}
	}
	
	/**
	 * @return
	 */
	private Message getFormData() {
		throw new UnsupportedOperationException("http form post with urlencoded format not supported now.");
	}
	
}


