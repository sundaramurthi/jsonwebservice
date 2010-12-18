package com.jaxws.json.codec.doc.provider;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.doc.HttpMetadataProvider;
import com.jaxws.json.codec.doc.JSONHttpMetadataPublisher;
import com.jaxws.json.codec.encode.WSJSONWriter;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationsException;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPart;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.util.ServiceFinder;

/**
 * @author Sundaramurthi Saminathan
 * @since JSONWebservice codec version 0.4
 * @version 1.0
 * 
 * Default JSON service end point document provider.
 */
public class DefaultEndpointDocument implements HttpMetadataProvider {
	private static final String[] queries = new String[]{"", "operations"};
	
	/**
	 * Map holder which keeps end point documents.
	 */
	private final static Map<String,String>	endPointDocuments	= new HashMap<String,String>();
	
	/**
	 * Request received codec instance holder
	 */
	private JSONCodec codec;

	/**
	 * Holder for request received HttpAdapter.
	 */
	private HttpAdapter httpAdapter;
	
	/**
	 * Flag to use show request payload enabled or not
	 */
	private boolean 	requestPayloadEnabled = true;
	
	/**
	 * "", "operations" query handled.
	 */
	public String[] getHandlingQueries() {
		return queries;
	}
	
	/**
	 * Default document provided if query string is empty. 
	 */
	public boolean canHandle(String queryString) {
		requestPayloadEnabled	= !(queryString != null && queryString.equals(queries[1]));
		return queryString == null || queryString.equals(queries[0]) || queryString.equals(queries[1]);
	}

	/** 
	 * request received end point codec.
	 */
	public void setJSONCodec(JSONCodec codec) {
		this.codec	= codec;
	}
	
	/** 
	 * Setter for HTTPAdapter. This adapter required to calculate end point URL.
	 */
	public void setHttpAdapter(HttpAdapter httpAdapter) {
		this.httpAdapter	= httpAdapter;
	}
	
	/** 
	 * Plain html document.
	 */
	public String getContentType() {
		return "text/html; charset=\"utf-8\"";
	}
	
	/**
	 * Process document for response. 
	 */
	public void process() {
		if(!endPointDocuments.containsKey(this.codec.getEndpoint().getPortName())){
			WSEndpoint<?> 		endPoint 	= codec.getEndpoint();
			JAXBContextImpl 	context 	= (JAXBContextImpl)endPoint.getSEIModel().getJAXBContext();
			
			Properties  		templates 	= new Properties();
			try {
				templates.load(JSONHttpMetadataPublisher.class.getResourceAsStream("codec.properties"));
			} catch (IOException e) {
				return;
			}
			String templateMain = templates.getProperty("template.default.main", 
					"<html><body>My Bad... Undefined template</body></html>");
		
			templateMain		= templateMain.replaceAll("#SERIVICE_NAME#", endPoint.getServiceName().getLocalPart());

			// NOTE:  endPoint.getSEIModel().getPort().getAddress() is not dynamic. Its address configured in WSDL
			String 				address 			= "#BASEADDRESS#" + httpAdapter.getValidPath();
	        if(address != null){
	        	 if(address.endsWith(".soap")){
	 	        	// Hack to SOAP implementation class configured to JSON end point.
	        		 address = address.replace(".soap", ".json");
	 	        }
	        	templateMain = templateMain.replaceAll("#END_POINT_URL#", address);
	        }
	        StringBuffer queries = new StringBuffer();
	        for (HttpMetadataProvider metadataProvider : ServiceFinder.find(HttpMetadataProvider.class)) {
				String querys [] = metadataProvider.getHandlingQueries();
				for(String query : querys){
					queries.append(String.format("<a href=\"%s?%s\">%s?%s</a>",address,query,address,query));
				}
	        }
	        templateMain		= templateMain.replaceAll("#DOCUMENT_ENDS#",queries.toString());

	        StringBuffer 	methods = new StringBuffer();
        
	        int count = 0;
	        ReflectionNavigator navigator = null;
			try {
				navigator = context.getTypeInfoSet().getNavigator();
				SEIModel seiModel = endPoint.getSEIModel();
				for (JavaMethod javaMethod : seiModel.getJavaMethods()) {
					String methodTemplate = templates.getProperty(
							"template.default.method", "");
					methodTemplate = methodTemplate.replaceAll("#METHOD_NAME#",
							javaMethod.getOperationName());
					methodTemplate = methodTemplate.replaceAll("#TR_CLASS#",
							(count % 2) == 1 ? "odd" : "even");
					WSDLBoundOperation operation = seiModel.getPort()
							.getBinding().get(
									javaMethod.getRequestPayloadName());
					
					String requestJSON = getJSONAsString(operation.getInParts(), navigator
							.getMethodParameters(javaMethod
									.getSEIMethod()));
					
					methodTemplate = methodTemplate.replaceAll("#INPUT_JSON#",
							requestPayloadEnabled ? 
									String.format("{\"%s\":%s}", javaMethod.getRequestPayloadName().getLocalPart(),requestJSON) :
										requestJSON);
					
					String responeJSON = getJSONAsString(operation.getOutParts(),
							new Type[] { navigator
									.getReturnType(javaMethod
											.getSEIMethod()) }).replaceAll("$", "");
					
					methodTemplate = methodTemplate.replaceAll("#OUTPUT_JSON#",
							javaMethod.getMEP().isOneWay() ? "ONE-WAY" :
							JSONCodec.responsePayloadEnabled ? 
								String.format("{\"%s\":%s}", javaMethod.getResponsePayloadName().getLocalPart() ,responeJSON)
								: responeJSON);

					methods.append(methodTemplate);
					count++;
				}
			} catch (IllegalAnnotationsException e) {
				e.printStackTrace();
			}
	      
			endPointDocuments.put(this.codec.getEndpoint().getPortName().getLocalPart() + requestPayloadEnabled
					+ JSONCodec.responsePayloadEnabled,
					templateMain.replace("#METHODS#", methods.toString()));
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jaxws.json.codec.doc.HttpMetadataProvider#doResponse(java.io.OutputStream)
	 */
	public void doResponse(WSHTTPConnection ouStream) throws IOException {
		process();
		String portDocuments =  endPointDocuments.get(this.codec.getEndpoint().getPortName().getLocalPart()
				+ requestPayloadEnabled
				+ JSONCodec.responsePayloadEnabled);
		if(portDocuments != null){
			ouStream.getOutput().write(portDocuments.replaceAll("#BASEADDRESS#", ouStream.getBaseAddress()).getBytes());
		}else{
			ouStream.getOutput().write(String.format("Unable to find default document for %s",
					this.codec.getEndpoint().getPortName()).getBytes());
		}
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
