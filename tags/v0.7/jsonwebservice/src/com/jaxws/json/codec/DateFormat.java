package com.jaxws.json.codec;


public enum DateFormat {
    ISO(""),
    RFC3339("yyyy-MM-dd'T'HH:mm:ss"),
    PLAIN(""),
    CUSTOM("");
    
    private String format;

	DateFormat(String format){
    	this.format = format;
    }

	public String getFormat() {
		return format;
	}
}
