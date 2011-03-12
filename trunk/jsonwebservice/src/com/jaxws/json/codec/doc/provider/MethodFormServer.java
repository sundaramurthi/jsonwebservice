package com.jaxws.json.codec.doc.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.doc.HttpMetadataProvider;
import com.jaxws.json.codec.doc.JSONHttpMetadataPublisher;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
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
		JAXBContextImpl 	context 		= (JAXBContextImpl)endPoint.getSEIModel().getJAXBContext();
		
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
			
			SEIModel seiModel = endPoint.getSEIModel();
			for (WSDLBoundOperation operation : seiModel.getPort().getBinding().getBindingOperations()) {
				String requestJSON = JSONHttpMetadataPublisher.getJSONAsString(operation.getInParts(), context, this.codec );
				contents.put(operation.getOperation().getName().getLocalPart(), 
						content.toString().replaceAll("#INPUT_JSON#", String.format("{\"%s\":%s}",operation.getName().getLocalPart(),
								requestJSON))
						.replaceAll("#METHOD_NAME#", operation.getName().getLocalPart())
						.replaceAll("#END_POINT_URL#", "#BASEADDRESS#" + httpAdapter.getValidPath()));
			}
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
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(HttpMetadataProvider o) {
		if(o.equals(this)){
			return 0;
		}else{
			return Integer.MAX_VALUE;
		}
	}
}
