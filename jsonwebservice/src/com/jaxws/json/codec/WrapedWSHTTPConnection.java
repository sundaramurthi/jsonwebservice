package com.jaxws.json.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

public class WrapedWSHTTPConnection extends WSHTTPConnection {
	WSHTTPConnection wsHTTPConnection;
	private String	method;
	public WrapedWSHTTPConnection(WSHTTPConnection wsHTTPConnection,String method) {
		super();
		this.wsHTTPConnection = wsHTTPConnection;
		this.method = method;
	}

	@Override
	public InputStream getInput() throws IOException {
		return this.wsHTTPConnection.getInput() ;
	}

	@Override
	public OutputStream getOutput() throws IOException {
		return this.wsHTTPConnection.getOutput();
	}

	@Override
	public String getPathInfo() {
		return this.wsHTTPConnection.getPathInfo();
	}

	@Override
	public String getQueryString() {
		return this.wsHTTPConnection.getQueryString();
	}

	@Override
	public String getRequestHeader(String arg0) {
		return this.wsHTTPConnection.getRequestHeader(arg0);
	}

	@Override
	public Map<String, List<String>> getRequestHeaders() {
		return this.wsHTTPConnection.getRequestHeaders();
	}

	@Override
	public String getRequestMethod() {
		return method;
	}

	@Override
	public Map<String, List<String>> getResponseHeaders() {
		return this.wsHTTPConnection.getResponseHeaders();
	}

	@Override
	public int getStatus() {
		return this.wsHTTPConnection.getStatus();
	}

	@Override
	public WebServiceContextDelegate getWebServiceContextDelegate() {
		return this.wsHTTPConnection.getWebServiceContextDelegate();
	}

	@Override
	public boolean isSecure() {
		return this.wsHTTPConnection.isSecure();
	}

	@Override
	public void setContentTypeResponseHeader(String arg0) {
		this.wsHTTPConnection.setContentTypeResponseHeader(arg0);
	}

	@Override
	public void setResponseHeaders(Map<String, List<String>> arg0) {
		this.wsHTTPConnection.setResponseHeaders(arg0);
	}

	@Override
	public void setStatus(int arg0) {
		this.wsHTTPConnection.setStatus(arg0);
	}

	@Override
	protected PropertyMap getPropertyMap() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sun.xml.ws.api.PropertySet#get(java.lang.Object)
	 */
	@Override
	public Object get(Object key) {
		return this.wsHTTPConnection.get(key);
	}

	/* (non-Javadoc)
	 * @see com.sun.xml.ws.api.PropertySet#put(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object put(String arg0, Object arg1) {
		return this.wsHTTPConnection.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see com.sun.xml.ws.api.PropertySet#remove(java.lang.Object)
	 */
	@Override
	public Object remove(Object arg0) {
		return this.wsHTTPConnection.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see com.sun.xml.ws.api.PropertySet#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Object key) {
		return this.wsHTTPConnection.supports(key);
	}

	@Override
	public void setResponseHeader(String paramString, List<String> paramList) {
		this.wsHTTPConnection.setResponseHeader(paramString, paramList);
		
	}

	@Override
	public Set<String> getRequestHeaderNames() {
		return this.wsHTTPConnection.getRequestHeaderNames();
	}

	@Override
	public List<String> getRequestHeaderValues(String paramString) {
		return this.wsHTTPConnection.getRequestHeaderValues(paramString);
	}

	@Override
	public String getRequestURI() {
		return this.wsHTTPConnection.getRequestURI();
	}

	@Override
	public String getRequestScheme() {
		return this.wsHTTPConnection.getRequestScheme();
	}

	@Override
	public String getServerName() {
		return this.wsHTTPConnection.getServerName();
	}

	@Override
	public int getServerPort() {
		return this.wsHTTPConnection.getServerPort();
	}
	

}
