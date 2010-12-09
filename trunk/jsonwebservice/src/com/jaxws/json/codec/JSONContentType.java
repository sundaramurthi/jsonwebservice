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
    
    public final static ContentType TEXT_JSON = new ContentType(){

    	public String getAcceptHeader() {
    		return null;
    	}

    	public String getContentType() {
    		return "text/json";
    	}

    	public String getSOAPActionHeader() {
    		return null;
    	}
    	
    };
    
    public final static ContentType TEXT_PLAIN = new ContentType(){

    	public String getAcceptHeader() {
    		return null;
    	}

    	public String getContentType() {
    		return "text/plain";
    	}

    	public String getSOAPActionHeader() {
    		return null;
    	}
    	
    };
}
