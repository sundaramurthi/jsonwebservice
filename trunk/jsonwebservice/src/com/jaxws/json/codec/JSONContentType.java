package com.jaxws.json.codec;

import com.sun.xml.ws.api.pipe.ContentType;

public final class JSONContentType implements ContentType{
	public static final String JSON_MIME_TYPE 		= "application/json";

    public String getContentType() {
        return JSON_MIME_TYPE;
    }

    public String getSOAPActionHeader() {
        return null;
    }

    public String getAcceptHeader() {
        return JSON_MIME_TYPE;
    }
}
