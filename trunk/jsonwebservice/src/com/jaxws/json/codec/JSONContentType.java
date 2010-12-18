package com.jaxws.json.codec;

import com.sun.xml.ws.api.pipe.ContentType;

/**
 * @author Sundaramurthi saminathan
 * 
 * Version 1.0
 * Content types used in codec are declared here.
 *
 */
public final class JSONContentType implements ContentType{
	
	/**
	 * Default accepet and response JSON content type.
	 */
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
    
    /**
     * JSON as Text plain response, used part of multipart upload and download.
     * When user upload file, By targetting iframe post content and get read response from iframe display.
     * 
     * Its require because its handy to make AJAX call with attachment.
     */
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
    
    /**
     * Fixed multipart mime body boundary.
     */
    public static final String BOUNDARY = "####JSONCODEC-SPLIT####";
    /**
     * Multipart mime content type.
     */
    public final static ContentType MULTIPART_MIXED = new ContentType(){
    	public String getAcceptHeader() {
    		return null;
    	}

    	public String getContentType() {
    		return "multipart/mixed; boundary="+BOUNDARY;
    	}

    	public String getSOAPActionHeader() {
    		return null;
    	}
    	
    };
}
