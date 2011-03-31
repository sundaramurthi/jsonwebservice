package com.jaxws.json.jquery.doc.provider;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.doc.HttpMetadataProvider;
import com.jaxws.json.codec.doc.provider.DefaultEndpointDocument;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.util.ServiceFinder;

/**
 * @author Sundaramurthi Saminathan
 * @version 1.0
 * @date March 12 2011
 *
 * JqueryDefaultEndpointDocument
 */
public class JqueryDefaultEndpointDocument extends DefaultEndpointDocument {
	
	/**
	 * Process document for response. 
	 */
	public void process() {
		if(!DefaultEndpointDocument.endPointDocuments.containsKey(this.codec.getEndpoint().getPortName())){
			WSEndpoint<?> 		endPoint 	= codec.getEndpoint();
			//JAXBContextImpl 	context 	= (JAXBContextImpl)endPoint.getSEIModel().getJAXBContext();
			
			StringBuffer 	buffer 	= new StringBuffer();
			try{
				BufferedReader reader	= new BufferedReader(new InputStreamReader(JqueryDefaultEndpointDocument.class.getResourceAsStream("default.html")));
				String 			line 	= reader.readLine();
				while(line != null){
					buffer.append(line+"\n");
					line = reader.readLine();
				}
				reader.close();
			}catch(Throwable th){
				th.printStackTrace();
			}
			String templateMain = buffer.toString();
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
					queries.append(String.format("<li><a target=\"_new\" href=\"%s?%s\">%s?%s</a><br/></li>",address,query,address,query));
				}
	        }
	        templateMain		= templateMain.replaceAll("#DOCUMENT_ENDS#",queries.toString());

	        StringBuffer 	methods = new StringBuffer();
			endPointDocuments.put(this.codec.getEndpoint().getPortName().getLocalPart() + requestPayloadEnabled
					+ JSONCodec.responsePayloadEnabled,
					templateMain.replace("#METHODS#", methods.toString()));
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
