package com.jaxws.json.codec.doc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import com.jaxws.json.codec.BeanAware;
import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.WrapedWSHTTPConnection;
import com.jaxws.json.codec.encode.WSJSONWriter;
import com.sun.istack.NotNull;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.ws.api.model.ParameterBinding;
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
		if(connection instanceof WrapedWSHTTPConnection){
			return false;
		}
		String 	queryString 	= connection.getQueryString();
		ServiceFinder<HttpMetadataProvider> providers = ServiceFinder.find(HttpMetadataProvider.class);
		HttpMetadataProvider[] providersArray = providers.toArray();
		Arrays.sort(providersArray);
		// If query handled by document provider, handle it.
		for (HttpMetadataProvider metadataProvider : providersArray) {
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
		adapter.invokeAsync(new WrapedWSHTTPConnection(connection,"POST"));
		int i = 0;
		while(!connection.isClosed()){
			// Wait until response complete
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
	public static String getJSONAsString(Map<String,? extends WSDLPart> parts , JAXBContextImpl context,JSONCodec codec){
		try{
			// RPC and DOCUMENT
			return WSJSONWriter.writeMetadata(getJSONAsMap(parts, context), codec.getCustomSerializer());
		}catch(Throwable e){
			// IGNORE
			return "{\"ERROR_IN_DOC\":\""+ e.getMessage() +"\"}";
		}
	}
	
	/**
	 * Private utility to conver parameter list to JSON DOC
	 * @param ordered map of parameters
	 * @return
	 */
	public static HashMap<String,Object> getJSONAsMap(Map<String, ? extends WSDLPart> map, JAXBContextImpl context){
		HashMap<String,Object> parameterMap = new LinkedHashMap<String, Object>();
		try{
			BeanAware  beanAware = new BeanAware(){};
			TreeSet<Entry<String, ? extends WSDLPart>> indexOrderedSet = new TreeSet<>(new Comparator<Entry<String, ? extends WSDLPart>>(){
				public int compare(Entry<String, ?extends WSDLPart> o1, Entry<String, ? extends WSDLPart> o2) {
					return new Integer(o1.getValue().getIndex()).compareTo(o2.getValue().getIndex()) ;
				}
			});
			indexOrderedSet.addAll(map.entrySet());
			for(Entry<String, ? extends WSDLPart> part : indexOrderedSet){
				if(part.getValue().getBinding() == ParameterBinding.BODY){
					JaxBeanInfo<?> globalType = context.getGlobalType(part.getValue().getDescriptor().name());
					Class<?> clazz = null;
					if(globalType != null){
						clazz	= globalType.jaxbType;
						if(BeanAware.isJSONPrimitive(clazz)){
							parameterMap.put(part.getKey(), beanAware.getNewInstance(clazz));
						} else if(clazz.isEnum()){
							parameterMap.put(part.getKey(), clazz.getEnumConstants()[0]);
						}else{
							parameterMap.put(part.getKey(), beanAware.getNewInstance(clazz));
						}
					} else {
						/*
						 * This case handled using SEI method parameter in body builder.
						 */
						// Extended simple type.
						parameterMap.put(part.getKey(), part.getValue().getDescriptor().name().getLocalPart());
					}
				}else{
					parameterMap.put(part.getKey(), part.getValue().getBinding().kind);
				}
			}
		}catch(Throwable e){
			parameterMap.put("ERROR_IN_DOC",e.getMessage());
		}
		return parameterMap;
	}
}
