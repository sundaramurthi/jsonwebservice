package com.jaxws.json.codec.doc;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.jaxws.json.codec.JSONCodec;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.WSEndpoint;
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
	JsClientServer 				jsClientServer 			= null;
	@NotNull 
	private JSONCodec codec;
	private WSEndpoint<?> endPoint;

	/**
	 * @param endPoint
	 * @param codec
	 */
	public JSONHttpMetadataPublisher(WSEndpoint<?> endPoint,JSONCodec codec) {
		this.endPoint 	= endPoint;
		this.codec		= codec;
	}

	@Override
	public boolean handleMetadataRequest(HttpAdapter adapter,
			WSHTTPConnection connection) throws IOException {
		String 	queryString 	= connection.getQueryString();
		
		for (HttpMetadataProvider metadataProvider : ServiceFinder.find(HttpMetadataProvider.class)) {
			if(metadataProvider.canHandle(queryString)){
				metadataProvider.setJSONCodec(this.codec);
				metadataProvider.setHttpAdapter(adapter);
				connection.setStatus(HttpURLConnection.HTTP_OK);
				connection.setContentTypeResponseHeader(metadataProvider.getContentType());
				metadataProvider.doResponse(connection.getOutput());
				return true;
			}
		}
		
		if(queryString.startsWith("model")){
			//TODO perform if(metaDataModelServer == null )
				/*metaDataModelServer = new MetaDataModelServer(endPoint,queryString.indexOf("&all") > -1,true,codec);
			
			connection.setStatus(HttpURLConnection.HTTP_OK);
			connection.setContentTypeResponseHeader("text/javascript;charset=\"utf-8\"");
			metaDataModelServer.doResponse(connection.getOutput());*/
			return true;
		}else if(queryString.startsWith("client")){
			if(jsClientServer == null)
				jsClientServer	= new JsClientServer(endPoint);
			
			connection.setStatus(HttpURLConnection.HTTP_OK);
			connection.setContentTypeResponseHeader("text/javascript;charset=\"utf-8\"");
			jsClientServer.doResponse(connection.getOutput());
			return true;
		}else{
			adapter.invokeAsync(connection);
			// TODO respond with options document 
			return true;
		}
	}
}
