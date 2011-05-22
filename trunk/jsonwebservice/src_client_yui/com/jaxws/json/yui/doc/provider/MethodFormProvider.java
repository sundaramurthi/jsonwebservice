package com.jaxws.json.yui.doc.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.ws.BindingProvider;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.doc.AbstractHttpMetadataProvider;
import com.jaxws.json.codec.doc.HttpMetadataProvider;
import com.jaxws.json.codec.encode.WSJSONWriter;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

/**
 * @author Sundaramurthi Saminathan
 * @since JSONWebservice codec version 0.4
 * @version 1.0
 * 
 */
public class MethodFormProvider extends AbstractHttpMetadataProvider implements HttpMetadataProvider {
	
	private static final String[] queries = new String[]{"form"};
	
	/**
	 * Cached form content
	 * <Endpoint,<operation name,content>>
	 */
	private final static Map<String,Map<String,String>>	operationDocuments	= Collections.synchronizedMap(new WeakHashMap<String,Map<String,String>>());
	
	/**
	 * Request recived codec holder.
	 */
	private JSONCodec codec = null;

	/**
	 * "config" query handled.
	 */
	public String[] getHandlingQueries() {
		return queries;
	}

	/**
	 * Document request with config query handled by ServiceConfigurationServer
	 * 
	 */
	public boolean canHandle(String queryString) {
		return queryString != null && queryString.startsWith(queries[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jaxws.json.codec.doc.HttpMetadataProvider#setJSONCodec(com.jaxws.
	 * json.codec.JSONCodec)
	 */
	public void setJSONCodec(JSONCodec codec) {
		this.codec = codec;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jaxws.json.codec.doc.HttpMetadataProvider#getContentType()
	 */
	public String getContentType() {
		return "text/html; charset=\"utf-8\"";
	}

	/**
	 * Init configuration holder
	 */
	public void process() {
		WSEndpoint<?> 		endPoint 		= this.codec.getEndpoint();
		Map<String, Object> modelMap 		= getMetadataModelMap(endPoint,true);
		@SuppressWarnings("unchecked")
		Map<String, Object> ports 			= (Map<String, Object>) modelMap.get(endPoint.getServiceName().getLocalPart());
		String				portName		= endPoint.getPortName().getLocalPart();			
		@SuppressWarnings("unchecked")
		Map<String, Object> selectedPort 	= (Map<String, Object>) ports.get(portName);
		String 				address			= (String) selectedPort.remove(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
		BufferedReader ins = new BufferedReader(new InputStreamReader(
				MethodFormProvider.class.getResourceAsStream("methodForm.htm")));
		StringBuffer content = new StringBuffer();
		try{
			String line = ins.readLine();
			while(line != null){
				content.append(line + "\n");
				line = ins.readLine();
				
			}
		}catch(Throwable th){}
		
		if (!operationDocuments.containsKey(portName)) {
			Map<String, String> contents = new HashMap<String, String>();
			
			for(String operation : selectedPort.keySet()){
				@SuppressWarnings("unchecked")
				Map<String, Object> operationMap = (Map<String, Object>) selectedPort.get(operation);
				@SuppressWarnings("unchecked")
				Map<String, Object> parameter = (Map<String, Object>) operationMap.remove(operation);
				
				String schemaIn = WSJSONWriter.writeMetadata(parameter,
						this.codec.getCustomSerializer(),true);
				
				Collection<Object> value = operationMap.values();
				String schemaOut = WSJSONWriter.writeMetadata(value.size() ==1 ? value.toArray()[0] : new HashMap<String,String>(),
						this.codec.getCustomSerializer(),true);
				contents.put(operation, 
						content.toString().replaceAll("#JSON_METHOD_SCHEMA#","{\"" + operation + "\" : " + schemaIn +"}")
						.replaceAll("#JSON_METHOD_RESPONSE_SCHEMA#","{\"" + operation + "\" : " + schemaOut +"}")
						.replaceAll("#METHOD_NAME#", operation));
				operationMap.put(operation, parameter);
			}
			operationDocuments.put(portName, contents);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jaxws.json.codec.doc.HttpMetadataProvider#doResponse(java.io.OutputStream
	 * )
	 */
	public void doResponse(WSHTTPConnection ouStream) throws IOException {
		process();
		String oper = ouStream.getQueryString().substring(4);
		if(!oper.isEmpty())
			doResponse(ouStream,
					operationDocuments.get(this.codec.getEndpoint().getPortName().getLocalPart())
					.get(oper));
		else
			ouStream.getOutput().write("add operation name in query string after 'form'. formxxxx E.g ?formgetChart".getBytes());
		ouStream.getOutput().flush();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(HttpMetadataProvider o) {
		if(o.equals(this)){
			return 0;
		}else{
			return Integer.MIN_VALUE;
		}
	}
}
