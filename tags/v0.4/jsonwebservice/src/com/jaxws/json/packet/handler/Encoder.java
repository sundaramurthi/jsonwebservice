package com.jaxws.json.packet.handler;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.ContentType;

/**
 * @author ssaminathan
 * Encoder interface allows user to create different output format other than JSON.
 * Class implements Encoder can act as custom responder.  
 */
public interface Encoder {
	/**
	 * @return any valid MIME type. application/json ignored by JSON encoder.
	 * If your like to customize JSON content type look at JSONObjectCustomizer.
	 */
	public ContentType contentType();
	
	/**
	 * Called when user request accept content type with is supported by MIME type returned in contentType call.
	 * 
	 * @param packet
	 * @param output (Most case direct servlet output stream)
	 * @return content type of response.
	 * @throws IOException
	 * 
	 * WARN: returned content type ignored in current version, due to content not buffered.
	 */
	public ContentType encode(Packet packet, OutputStream output) throws IOException;
}
