package com.jaxws.json.yui.doc.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.doc.HttpMetadataProvider;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

/**
 * @author Sundaramurthi Saminathan
 * @version 1.0
 * @date May 8 2011
 *
 * YUI DefaultEndpointDocument
 */
public class DefaultEndpointDocument extends com.jaxws.json.codec.doc.provider.DefaultEndpointDocument {
	
	private static final String CLIENT_RESOURCE = "CLIENT_RESOURCE";

	private String queryString;
	public boolean canHandle(String queryString) {
		this.queryString = queryString;
		return super.canHandle(queryString) || (queryString != null && queryString.startsWith(CLIENT_RESOURCE)); 
	}
	
	/** 
	 * Plain html document.
	 */
	public String getContentType() {
		if(queryString != null){
			if(queryString.endsWith(".js")){
				return "text/javascript; charset=\"UTF-8\"";
			}else if(queryString.endsWith(".css")){
				return "text/css; charset=\"utf-8\"";
			}else if(queryString.endsWith(".png")){
				return "image/png";
			}
		}
		return super.getContentType();
	}
	
	/**
	 * Process document for response. 
	 */
	public void process() {
		if(!DefaultEndpointDocument.endPointDocuments.containsKey(this.codec.getEndpoint().getPortName())){
			WSEndpoint<?> 		endPoint 	= codec.getEndpoint();
			//JAXBContextImpl 	context 	= (JAXBContextImpl)endPoint.getSEIModel().getJAXBContext();
			
			StringBuffer 	buffer 	= new StringBuffer();
			try{
				BufferedReader reader	= new BufferedReader(new InputStreamReader(DefaultEndpointDocument.class.getResourceAsStream("default.html"))); //$NON-NLS-1$
				String 			line 	= reader.readLine();
				while(line != null){
					buffer.append(line+"\n"); //$NON-NLS-1$
					line = reader.readLine();
				}
				reader.close();
			}catch(Throwable th){
				th.printStackTrace();
			}
			String templateMain = buffer.toString();
			templateMain		= templateMain.replaceAll("#SERIVICE_NAME#", endPoint.getServiceName().getLocalPart()); //$NON-NLS-1$

	        StringBuffer 	methods = new StringBuffer();
			endPointDocuments.put(this.codec.getEndpoint().getPortName().getLocalPart() + requestPayloadEnabled
					+ JSONCodec.responsePayloadEnabled,
					templateMain.replace("#METHODS#", methods.toString())); //$NON-NLS-1$
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jaxws.json.codec.doc.HttpMetadataProvider#doResponse(java.io.OutputStream)
	 */
	public void doResponse(WSHTTPConnection ouStream) throws IOException {
		String queryString = ouStream.getQueryString();
		if(queryString != null && queryString.startsWith(CLIENT_RESOURCE)){
			if(queryString.length() > CLIENT_RESOURCE.length()){
				InputStream in = getClass().getResourceAsStream(queryString.substring(CLIENT_RESOURCE.length() + 1));
				if(in != null){
					writeToOutput(in,
						ouStream.getOutput());
				}else{
					// Resource not found.
				}
			}
		}else{
			super.doResponse(ouStream);
		}
	}
	
	private void writeToOutput(InputStream in, OutputStream out) throws IOException {
		byte b[] = new byte[in.available()];
		in.read(b);
		out.write(b);
		out.flush();
		in.close();
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
