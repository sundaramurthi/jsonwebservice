package com.jaxws.json.codec.doc.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.doc.HttpMetadataProvider;
import com.jaxws.json.codec.encode.WSJSONWriter;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPart;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

/**
 * @author Sundaramurthi Saminathan
 * @since JSONWebservice codec version 0.4
 * @version 1.0
 * 
 */
public class MethodFormServer implements HttpMetadataProvider {
	
	private static final String[] queries = new String[]{"form"};
	
	/**
	 * Cached form content
	 * <Endpoint,<operation name,content>>
	 */
	private final static Map<QName,Map<String,String>>	operationDocuments	= new HashMap<QName,Map<String,String>>();
	
	/**
	 * Request recived codec holder.
	 */
	private JSONCodec codec = null;

	private HttpAdapter httpAdapter;

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

	/* (non-Javadoc)
	 * @see com.jaxws.json.codec.doc.HttpMetadataProvider#setHttpAdapter(com.sun.xml.ws.transport.http.HttpAdapter)
	 */
	public void setHttpAdapter(HttpAdapter httpAdapter) {
		this.httpAdapter	= httpAdapter;
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
		JAXBContextImpl 	context 	= (JAXBContextImpl)endPoint.getSEIModel().getJAXBContext();
		
		WSDLPort port = endPoint.getPort();
		if (!operationDocuments.containsKey(port.getBinding().getName())) {
			BufferedReader ins = new BufferedReader(new InputStreamReader(
			MethodFormServer.class.getResourceAsStream("methodForm.htm")));
			StringBuffer content = new StringBuffer();
			try{
				String line = ins.readLine();
				while(line != null){
					content.append(line + "\n");
					line = ins.readLine();
					
				}
			}catch(Throwable th){}
			Map<String, String> contents = new HashMap<String, String>();
			
			ReflectionNavigator navigator = null;
			try {
				navigator = context.getTypeInfoSet().getNavigator();
				SEIModel seiModel = endPoint.getSEIModel();
				for (JavaMethod javaMethod : seiModel.getJavaMethods()) {
					WSDLBoundOperation operation = seiModel.getPort()
					.getBinding().get(
							javaMethod.getRequestPayloadName());
					
					String requestJSON = getJSONAsString(operation.getInParts(), navigator
							.getMethodParameters(javaMethod
									.getSEIMethod()));
					
					contents.put(javaMethod.getRequestPayloadName().getLocalPart(), 
							content.toString().replaceAll("#INPUT_JSON#", String.format("{\"%s\":%s}",operation.getName().getLocalPart(),
									requestJSON))
							.replaceAll("#METHOD_NAME#", javaMethod.getRequestPayloadName().getLocalPart())
							.replaceAll("#END_POINT_URL#", "#BASEADDRESS#" + httpAdapter.getValidPath()));
				}
			}catch(Throwable th){}
			operationDocuments.put(port.getBinding().getName(), contents);
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
			ouStream.getOutput().write(
					operationDocuments.get(this.codec.getEndpoint().getPort().getBinding().getName())
					.get(oper).replaceAll("#BASEADDRESS#", ouStream.getBaseAddress()).getBytes());
		else
			ouStream.getOutput().write("add operation name in query string after 'form'. formxxxx E.g ?formgetChart".getBytes());
		ouStream.getOutput().flush();
	}
	
	/**
	 * Private utility to conver parameter list to JSON DOC
	 * @param parameters
	 * @return
	 */
	private String getJSONAsString(Map<String,WSDLPart> parts , Type[] types){
		try{
			HashMap<String,Object> parameterMap = new HashMap<String, Object>();
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
			return WSJSONWriter.writeMetadata(parameterMap, codec.getCustomSerializer());
		}catch(Throwable e){
			// IGNORE
			return "{\"ERROR_IN_DOC\":\""+ e.getMessage() +"\"}";
		}
	}
}
