package com.jaxws.json.codec.doc;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.Properties;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

/**
 * @author Sundaramurthi
 * @version 0.1
 * @mail sundaramurthis@gmail.com
 */
public class JSONHttpMetadataPublisher extends HttpMetadataPublisher {
	private static final Properties  templates = new Properties();
	static{
		try {
			templates.load(JSONHttpMetadataPublisher.class.getResourceAsStream("codec.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private WSEndpoint<?> endPoint;

	public JSONHttpMetadataPublisher(WSEndpoint<?> endPoint) {
		this.endPoint = endPoint;
	}

	@Override
	public boolean handleMetadataRequest(HttpAdapter adapter,
			WSHTTPConnection connection) throws IOException {
		String queryString = connection.getQueryString();
		if ((queryString == null || queryString.equals("")) && endPoint != null){
			
			JAXBContextImpl context = (JAXBContextImpl)endPoint.getSEIModel().getJAXBContext();
			connection.setStatus(HttpURLConnection.HTTP_OK);
			connection.setContentTypeResponseHeader("text/html;charset=\"utf-8\"");
	        
			OutputStream out = connection.getOutput();
			
			String templateMain = templates.getProperty("template.main", "<html><body>My Bad...</body></html>");
			
			templateMain		= templateMain.replaceAll("#SERIVICE_NAME#", endPoint.getServiceName().getLocalPart());

	        PortAddressResolver portAddressResolver = adapter.owner.createPortAddressResolver(connection.getBaseAddress());
	        String address = portAddressResolver.getAddressFor(endPoint.getServiceName(), endPoint.getPortName().getLocalPart());
	        if(address != null)
	        	templateMain = templateMain.replaceAll("#END_POINT_URL#", address);

	        StringBuffer 	methods = new StringBuffer();
	        
	        int count = 0;
			for(JavaMethod method:endPoint.getSEIModel().getJavaMethods()){
				String methodTemplate 	= templates.getProperty("template.method", "");
				methodTemplate			= methodTemplate.replaceAll("#METHOD_NAME#", method.getOperationName());
				methodTemplate			= methodTemplate.replaceAll("#TR_CLASS#",(count % 2) == 1 ? "odd" : "even");

				QName methodQName = method.getRequestPayloadName();
				
				Class<?> bean = context.getGlobalType(methodQName).jaxbType;
				StringBuffer jsonIn = new StringBuffer();
				jsonIn.append("{\""+method.getOperationName()+"\":");
				serializeBean(bean,jsonIn);
				jsonIn.append("}");
				methodTemplate = methodTemplate.replaceAll("#INPUT_JSON#", jsonIn.toString());
				bean = context.getGlobalType(method.getResponsePayloadName()).jaxbType;
				StringBuffer jsonOut = new StringBuffer();
				serializeBean(bean,jsonOut);
				methodTemplate = methodTemplate.replaceFirst("#OUTPUT_JSON#", jsonOut.toString());
				methods.append(methodTemplate);
				count++;
			}
			templateMain		= templateMain.replace("#METHODS#", methods.toString());
			
			out.write(templateMain.getBytes());
			return true;
		}
		return false;
	}
	
	private void serializeBean(Class<?> bean,StringBuffer out) throws IOException{
		try{
			if(bean != null){
				out.append("{");
				int count =0;
				for(Field field:bean.getDeclaredFields()){
					if(field.getDeclaringClass().getName().equals(bean.getName())){
						if(count != 0 ){
							out.append(",");
						}
						if(field.getType() instanceof Class && !field.getType().getName().startsWith("java.lang") ){
							//TODO deduct recursive refrence
							out.append("\""+field.getName()+"\":");
							if(field.getType().getName().equals(JAXBElement.class.getName())){
								//TODO serialize element
								//serializeBean(field.getType(), out);
								out.append("\"\"");
							}else{
								serializeBean(field.getType(), out);
							}
						}else{
							out.append("\""+field.getName()+"\":\"\"");
						}
					}
					count++;
				}
				out.append("}");
			}
		}catch(Throwable th){out.append(th.getMessage());}
	}
	
}
