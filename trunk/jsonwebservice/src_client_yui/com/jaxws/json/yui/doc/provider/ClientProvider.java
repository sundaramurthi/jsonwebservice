package com.jaxws.json.yui.doc.provider;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.JSONContentType;
import com.jaxws.json.codec.doc.AbstractHttpMetadataProvider;
import com.jaxws.json.codec.doc.HttpMetadataProvider;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

/**
 * @author Sundaramurthi Saminathan
 * @since JSONWebservice codec version 0.7
 * @since JQuery client version 0.1
 * @version 1.0
 * 
 * Javascript client provider for jquery users.
 */
public class ClientProvider extends AbstractHttpMetadataProvider implements HttpMetadataProvider {
	
	private static final String[] queries = new String[]{"client"};
	
	/**
	 * Map holder which keeps end point documents.
	 */
	private final static Map<QName,String>	endPointClientJs	= Collections.synchronizedMap(new HashMap<QName,String>());
	
	/**
	 * Request received codec instance holder
	 */
	private JSONCodec codec;

	/**
	 * "jsonmodel" query handled.
	 */
	public String[] getHandlingQueries() {
		return queries;
	}

	/**
	 * Handler flag, If query string is jsonmodel , its handled by model server.
	 */
	public boolean canHandle(String queryString) {
		return queryString != null && queryString.equals(queries[0]);
	}
	
	/**
	 * end point codec set holder.
	 */
	public void setJSONCodec(JSONCodec codec) {
		this.codec	= codec;
	}
	
	/**
	 * Meta data model content provider.
	 * @see HttpMetadataProvider.getContentType
	 */
	public String getContentType() {
		return "text/javascript; charset=\"utf-8\"";
	}

	@SuppressWarnings("unchecked")
	public void process() {
		WSEndpoint<?> endPoint = this.codec.getEndpoint();
		Map<String, Object> modelMap 	= getMetadataModelMap(endPoint,true);
		Map<String, Object> ports 		= (Map<String, Object>) modelMap.get(endPoint.getServiceName().getLocalPart());
		String				portName	= endPoint.getPortName().getLocalPart();			
		Map<String, Object> selectedPort = (Map<String, Object>) ports.get(portName);
		String address	= (String) selectedPort.remove(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
		
		StringBuffer clientJS			= new StringBuffer();
		clientJS.append(
				"if(typeof YAHOO == \"undefined\" || !YAHOO){\n" +
					"var file = document.createElement('script');file.setAttribute(\"type\",\"text/javascript\");\n" +
					"file.setAttribute(\"src\",\"" + address + "?CLIENT_RESOURCE=js/yahoo-all.js\");\n" +
					"document.getElementsByTagName(\"head\")[0].appendChild(file);\n"+
				"}\n" 
		);
		
		clientJS.append("var " + portName + " = {};\n");
		for(String operation : selectedPort.keySet()){
			clientJS.append(portName + "." + operation +" = function(");
			Map<String, Object> operationMap = (Map<String, Object>) selectedPort.get(operation);
			Map<String, Object> parameter = (Map<String, Object>) operationMap.get(operation);
			String parameterJson		= "";
			int index = 0;
			for(String paramName : parameter.keySet()){
				if(index > 0){
					parameterJson += ",";
				}
				clientJS.append(paramName +",");// In last parameter "," required to separate callback
				parameterJson += "\""+paramName + "\":"+paramName;
				index++;
			}
			clientJS.append(" callback){\n" +
					"if(typeof callback == 'undefined' || callback == null){" +
					" callback = {success: function(o){alert(o);},"+
					" failure: function(o){alert(o);},"+
					" argument: []}"+
					"}"+
					"YAHOO.util.Connect.setDefaultPostHeader(false);\n" +
					"YAHOO.util.Connect.initHeader(\"Content-Type\",\"" + JSONContentType.JSON_CONTENT_TYPE + "\");\n" +
					"YAHOO.util.Connect.asyncRequest('POST', '#BASEADDRESS#" + httpAdapter.getValidPath() + "', callback, \n"+
						"YAHOO.lang.JSON.stringify({" + operation + " : {" + parameterJson + "}})\n"+
					");\n"+
				"};\n");
		}
		endPointClientJs.put(this.codec.getEndpoint().getServiceName(),
				clientJS.toString());
	}
	
	/**
	 * Output responder.
	 */
	public void doResponse(WSHTTPConnection ouStream) throws IOException {
		QName serviceName = this.codec.getEndpoint().getServiceName();
		//DEbug if(!endPointClientJs.containsKey(serviceName))
			process();
		String portDocuments =  endPointClientJs.get(serviceName);
		if(portDocuments != null){
			doResponse(ouStream, portDocuments);
		}else{
			ouStream.getOutput().write(String.format("Unable to find default document for %s",
					this.codec.getEndpoint().getPortName()).getBytes());
		}
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
