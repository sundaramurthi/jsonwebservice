package com.jaxws.json.codec;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

public class JSONHttpMetadataPublisher extends HttpMetadataPublisher {

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
			
			OutputStream out = connection.getOutput();
			out.write("<html><body>".getBytes());
			
			for(JavaMethod method:endPoint.getSEIModel().getJavaMethods()){
				out.write(("<br/><b>"+method.getOperationName()+"</b><br/>").getBytes());
				
				QName methodQName = method.getRequestPayloadName();
				
				Class<?> bean = context.getGlobalType(methodQName).jaxbType;
				serializeBean(bean,out);
			}
			
			out.write("</body></html>".getBytes());
			return true;
		}
		return false;
	}
	
	private void serializeBean(Class<?> bean,OutputStream out) throws IOException{
		try{
			if(bean != null){
				out.write("{".getBytes());
				int count =0;
				for(Field field:bean.getDeclaredFields()){
					if(field.getDeclaringClass().getName().equals(bean.getName())){
						if(count != 0 ){
							out.write(",".getBytes());
						}
						if(field.getType() instanceof Class && !field.getType().getName().startsWith("java.lang") ){
							//TODO deduct recursive refrence
							out.write(("\""+field.getName()+"\":").getBytes());
							if(field.getType().getName().equals(JAXBElement.class.getName())){
								//TODO serialize element
								//serializeBean(field.getType(), out);
							}else{
								serializeBean(field.getType(), out);
							}
						}else{
							out.write(("\""+field.getName()+"\":\"\"").getBytes());
						}
					}
					count++;
				}
				out.write("}".getBytes());
			}
		}catch(Throwable th){out.write(th.getMessage().getBytes());}
	}

}
