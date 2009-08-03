package com.jaxws.json.packet.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.sun.xml.ws.api.pipe.ContentType;

public interface ResponsePacketHandler {
	public ContentType responseContentType();
	
	public ContentType encode(Map<String,Object> result, OutputStream out) throws IOException;
}
