package com.jaxws.json.codec.doc;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.jaxws.json.codec.JSONCodec;
import com.sun.istack.NotNull;
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
		return true;
	}
}
