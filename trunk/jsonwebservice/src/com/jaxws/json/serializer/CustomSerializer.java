package com.jaxws.json.serializer;

import java.lang.reflect.Method;


public interface CustomSerializer {
	
	/**
	 * Codec handle class type
	 */
	public Class<? extends Object> getAcceptClass();
	
	/**
	 * Codec handle class type
	 */
	public boolean canBeHandled(Method method);
	
	/**
	 * To Json
	 */
	public void encode(StringBuilder buf,Object object);
	
	/**
	 * To Json
	 */
	public Object decode(Object value);
	
	/**
	 * Content type json model
	 */
	public void metaData(StringBuilder buf);
}
