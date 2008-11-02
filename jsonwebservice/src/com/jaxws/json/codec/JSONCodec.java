package com.jaxws.json.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import com.googlecode.jsonplugin.JSONException;
import com.googlecode.jsonplugin.JSONPopulator;
import com.googlecode.jsonplugin.JSONUtil;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.server.EndpointAwareCodec;
import com.sun.xml.ws.api.server.EndpointComponent;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

/**
 * @author Sundaramurthi
 * @version 1.0
 * @mail sundaramurthis@gmail.com
 */
public class JSONCodec implements EndpointAwareCodec, EndpointComponent {
	private static final String 		JSON_MIME_TYPE 	= "application/json";
	private static final ContentType 	jsonContentType = new JSONContentType();
	
	private final 	WSBinding 		binding;
	private final 	SOAPVersion 	soapVersion;
    private 		WSEndpoint 		endpoint;
    private HttpMetadataPublisher 	metadataPublisher;

	public JSONCodec(WSBinding binding) {
		this.binding = binding;
		 this.soapVersion = binding.getSOAPVersion();
	}
	
	public JSONCodec(JSONCodec that) {
        this(that.binding);
        this.endpoint = that.endpoint;
    }

	public void setEndpoint(WSEndpoint endpoint) {
		this.endpoint = endpoint;
        endpoint.getComponentRegistry().add(this);
	}

	public Codec copy() {
		 return new JSONCodec(this);
	}

	public void decode(InputStream in, String contentType, Packet response)
			throws IOException {
		Message message = null;
		Object requestMethodJSON;
		try {
			JAXBContextImpl context = (JAXBContextImpl)endpoint.getSEIModel().getJAXBContext();
			
			requestMethodJSON = JSONUtil.deserialize(new InputStreamReader(in));
			if(requestMethodJSON != null && requestMethodJSON instanceof Map){
				Map requestMethodJSONMap = (Map) requestMethodJSON;
				//TODO right now handle only last method, change this to handle multiple batch request
				for(Object method : requestMethodJSONMap.keySet()){
					String methodName = method.toString();
					
					Class bean = null;
					for(JavaMethod m:endpoint.getSEIModel().getJavaMethods()){
						QName methodQName = m.getRequestPayloadName();
						if(methodQName.getLocalPart().equals(methodName)){
							bean = context.getGlobalType(methodQName).jaxbType;
							break;
						}
					}
					if(bean != null){
						Object object = bean.newInstance();
						Object methodParameter = requestMethodJSONMap.get(methodName);
						if(methodParameter instanceof Map){
							Map methodParameterMap = (Map) methodParameter;
							for(Field field:bean.getFields()){
								if(field.getType() instanceof Class){//TODO check accessablity
									Object val = field.getType().newInstance();
									new JSONPopulator().populateObject(val, (Map) methodParameterMap.get(field.getName()));
									field.set(object, val);
								}else{
									throw new Exception("TODO JSON Codec , Non object method parameter");
								}
							}
							message = JAXBMessage.create(context, object, soapVersion);
						}else{
							throw new Exception("Methods parameter without map not implemented");
						}
					}
				}
			}else{
				throw new JSONException("No method name found");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			message = Messages.createEmpty(soapVersion);
		} 
	    response.setMessage(message);
	}

	public void decode(ReadableByteChannel arg0, String arg1, Packet arg2) {
		throw new UnsupportedOperationException();
	}

	public ContentType encode(Packet packet, OutputStream out) throws IOException {
		Message message = packet.getMessage();
		if (message != null) {
			// XMLStreamWriter sw = null;
			OutputStreamWriter sw = null;
			//BindingID.parse("https://www.nedstat.com/codec/html/").createEncoder(binding).encode(packet, out);
			try {
				sw = new OutputStreamWriter(out, "UTF-8");
				HashMap<String, Object> result = new HashMap<String, Object>();
				if (message.isFault()) {
					result.put("status", "flase");
					result.put("message", message.readAsSOAPMessage()
							.getSOAPBody().getFault().getFaultString());
					// result.put("detail",
					// message.readAsSOAPMessage().getSOAPBody().getFault().getDetail());
				} else {
					Object obj = message.readPayloadAsJAXB(endpoint.getSEIModel().getJAXBContext().createUnmarshaller());
					for (int i = 0; i < obj.getClass().getDeclaredFields().length; i++) {
						Field field = obj.getClass().getDeclaredFields()[i];
						try {
							result.put(field.getName(), field.get(obj));
						} catch (Throwable th) {
						}
					}
				}
				JSONUtil.serialize(sw, result);
				
			} catch (Exception xe) {
				throw new WebServiceException(xe);
			} finally {
				if (sw != null) {
					try {
						sw.close();
					} catch (Exception xe) {
						// let the original exception get through
					}
				}
			}
		}
		return jsonContentType;
	}

	public ContentType encode(Packet arg0, WritableByteChannel arg1) {
		throw new UnsupportedOperationException();
	}

	public String getMimeType() {
		return JSON_MIME_TYPE;
	}

	public ContentType getStaticContentType(Packet arg0) {
		return jsonContentType;
	}

	public @Nullable <T> T getSPI(@NotNull Class<T> type) {
		if (type == HttpMetadataPublisher.class) {
			if (metadataPublisher == null)
				metadataPublisher = new HttpMetadataPublisher(){
					@Override
					public boolean handleMetadataRequest(HttpAdapter arg0,
							WSHTTPConnection arg1) throws IOException {
						return true;
					}
				
			};
			return type.cast(metadataPublisher);
		}
		return null;
	}
	
	 private static final class  JSONContentType implements ContentType {

	        private static final String JSON_CONTENT_TYPE = JSON_MIME_TYPE;

	        public String getContentType() {
	            return JSON_CONTENT_TYPE;
	        }

	        public String getSOAPActionHeader() {
	            return null;
	        }

	        public String getAcceptHeader() {
	            return JSON_CONTENT_TYPE;
	        }
	    }
}
