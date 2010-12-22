package com.jaxws.json.codec.doc;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jws.soap.SOAPBinding.Style;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.encode.WSJSONWriter;
import com.sun.istack.NotNull;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.model.wsdl.WSDLPart;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.util.ServiceFinder;

/**
 * @author Sundaramurthi
 * @version 0.1
 * @mail sundaramurthis@gmail.com
 */
public class JSONHttpMetadataPublisher extends HttpMetadataPublisher {
	/**
	 * Template cache
	 */
	
	/**
	 * meta data model cache.
	 */
	@NotNull 
	private JSONCodec codec;

	/**
	 * @param endPoint
	 * @param codec
	 */
	public JSONHttpMetadataPublisher(JSONCodec codec) {
		this.codec		= codec;
	}

	@Override
	public boolean handleMetadataRequest(HttpAdapter adapter,
			WSHTTPConnection connection) throws IOException {
		String 	queryString 	= connection.getQueryString();
		
		// If query handled by document provider, handle it.
		for (HttpMetadataProvider metadataProvider : ServiceFinder.find(HttpMetadataProvider.class)) {
			if(metadataProvider.canHandle(queryString)){
				metadataProvider.setJSONCodec(this.codec);
				metadataProvider.setHttpAdapter(adapter);
				connection.setStatus(HttpURLConnection.HTTP_OK);
				connection.setContentTypeResponseHeader(metadataProvider.getContentType());
				metadataProvider.doResponse(connection);
				return true;
			}
		}
		// Call http get operationn. 
		adapter.invokeAsync(connection);
		int i = 0;
		while(!connection.isClosed()){
			// Wait untill response complete
			try {
				Thread.sleep(200);// 0.1 milliseconds
				
			} catch (InterruptedException e) {
				
			}
			if(i++ > 90000){
				connection.close();
				break;
				//5 * 60 * 60 * 1000 / 200 maximum of 5 minitus to respond
				
			}
		}
		return true;
	}
	
	/**
	 * Private utility to conver parameter list to JSON DOC
	 * @param parameters
	 * @return
	 */
	public static String getJSONAsString(Map<String,WSDLPart> parts , Type[] types, Style style, JAXBContextImpl context,JSONCodec codec){
		try{
			if(style == Style.RPC){
				return WSJSONWriter.writeMetadata(getJSONAsMap(parts, types), codec.getCustomSerializer());
			} else {//Style.DOCUMENT
				assert parts.size() == 1; //TODO test one way
				Class<?> type = context.getGlobalType(parts.values().iterator().next().getDescriptor().name()).jaxbType;
				return WSJSONWriter.writeMetadata(type.newInstance(), codec.getCustomSerializer());
			}
		}catch(Throwable e){
			// IGNORE
			return "{\"ERROR_IN_DOC\":\""+ e.getMessage() +"\"}";
		}
	}
	
	/**
	 * Private utility to conver parameter list to JSON DOC
	 * @param parameters
	 * @return
	 */
	public static HashMap<String,Object> getJSONAsMap(Map<String,WSDLPart> parts , Type[] types){
		HashMap<String,Object> parameterMap = new HashMap<String, Object>();
		try{
			for(Entry<String, WSDLPart> part : parts.entrySet()){
				Class<?> clazz = ((Class<?>)types[part.getValue().getIndex()]);
				if(clazz.isPrimitive()){
					parameterMap.put(part.getKey(), clazz.getSimpleName());
				} else if(clazz.isEnum()){
					parameterMap.put(part.getKey(), clazz.getEnumConstants()[0]);
				}else{
					parameterMap.put(part.getKey(), clazz.newInstance());
				}
			}
		}catch(Throwable e){
			parameterMap.put("ERROR_IN_DOC",e.getMessage());
		}
		return parameterMap;
	}
}
