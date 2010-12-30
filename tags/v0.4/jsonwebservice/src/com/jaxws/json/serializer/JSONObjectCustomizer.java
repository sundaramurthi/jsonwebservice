package com.jaxws.json.serializer;

import java.io.OutputStream;


/**
 * @author ssaminathan
 *
 */
public interface JSONObjectCustomizer {
	
	/**
	 * Codec handle class type
	 */
	public Class<? extends Object> getAcceptClass();
	
	/**
	 * To Json
	 */
	public void encode(OutputStream output,Object object);
	
	/**
	 * To Json
	 */
	public Object decode(Object value);
	
	/**
	 * Content type json model
	 */
	public void metaData(StringBuilder buf);
}
