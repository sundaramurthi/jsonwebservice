package com.jaxws.json.codec.doc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;

import com.jaxws.json.JaxWsJSONPopulator;
import com.jaxws.json.codec.JSONBindingID;
import com.jaxws.json.codec.JSONCodec;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.ws.api.model.wsdl.WSDLInput;
import com.sun.xml.ws.api.model.wsdl.WSDLMessage;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import com.sun.xml.ws.api.model.wsdl.WSDLPart;
import com.sun.xml.ws.api.model.wsdl.WSDLPartDescriptor;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.model.wsdl.WSDLPartDescriptorImpl;

public class JsClientServer {
	StringBuffer clientCode = new StringBuffer();
	
	JAXBContextImpl context;// Should use wsdl TODO
	public JsClientServer(WSEndpoint<?> endPoint) {
		createClientTemplete(endPoint,true);
	}

	public void createClientTemplete(WSEndpoint<?> endPoint,boolean all) {
		context = (JAXBContextImpl)endPoint.getSEIModel().getJAXBContext();
		clientCode = new StringBuffer();
		clientCode.append("{\""+endPoint.getServiceName().getLocalPart()+"\":{");// Service start
		if(all){
			Module modules = endPoint.getContainer().getSPI(com.sun.xml.ws.api.server.Module.class);
			for(BoundEndpoint endPointObj : modules.getBoundEndpoints()){
				if(endPointObj.getEndpoint().getBinding().getBindingID() == JSONBindingID.JSON_BINDING){
					createClientTemplete( endPointObj.getEndpoint());
					clientCode.append(",");
				}
			}
			clientCode.replace(clientCode.length()-1, clientCode.length(), ""); // remove last extra ,
		}else{
			createClientTemplete(endPoint);
		}
		// Validataion invoke 
		clientCode.append(convertStreamToString(getClass().getResourceAsStream("client.js")));
		//
		
		clientCode.append(	"}"+// Service end
					"}");// Object warper end
	}
	
	private void createClientTemplete(WSEndpoint jsonEndpoint){
		
		WSDLPortType jsonPort = jsonEndpoint.getPort().getBinding().getPortType();
		clientCode.append("\""+jsonPort.getName().getLocalPart()+"\":{");
		// Operations meta data
		String methodUrl 	= "";
		if(jsonEndpoint.getPort().getAddress().getURL() != null)
			methodUrl = jsonEndpoint.getPort().getAddress().getURL().getPath();
		String mimeType 	= jsonEndpoint.createCodec().getMimeType();
		for(WSDLOperation operation : jsonPort.getOperations()){
			createClientTemplete(operation,methodUrl,mimeType);
			clientCode.append(",");
		}
		clientCode.replace(clientCode.length()-1, clientCode.length(), ""); // remove last extra ,
		// End Operations meta
		clientCode.append("}");
	}
	
	
	private void createClientTemplete(WSDLOperation jsonOperation,String methodUrl,String mimeType ) {
		clientCode.append("\""+jsonOperation.getName().getLocalPart()+"\":{");
		createClientTemplete(jsonOperation.getInput());
		clientCode.append(",");
		createClientTemplete(jsonOperation.getOutput());
		//clientCode.append(",");
		createResponseSync(jsonOperation,methodUrl,mimeType);
		//Operation close
		clientCode.append("}");
	}
	
	private void createResponseSync(WSDLOperation jsonOperation,String methodUrl, String mimeType){
		// TODO client server sync
	}

	private void createClientTemplete(WSDLInput input){
		clientCode.append("\"input\":{");
		createClientTemplete(input.getMessage());
		clientCode.append("}");
	}
	
	private void createClientTemplete(WSDLOutput output){
		clientCode.append("\"output\":{");
		if(output != null)
			createClientTemplete(output.getMessage());
		clientCode.append("}");
	}
	
	private void createClientTemplete(WSDLMessage message){
		createClientTemplete(message.parts());
	}
	
	private void createClientTemplete(Iterable<? extends WSDLPart> parts) {
		boolean hasParts = false;
		for(WSDLPart part : parts){
			hasParts = true;
			createClientTemplete(part);
			clientCode.append(",");
		}
		if(hasParts)
			clientCode.replace(clientCode.length()-1, clientCode.length(), ""); // remove last extra ,
	}

	private void createClientTemplete(WSDLPart part) {
		WSDLPartDescriptor partDescription = (WSDLPartDescriptorImpl) part.getDescriptor();
		if(context != null){
			JaxBeanInfo type = context.getGlobalType(partDescription.name());
			clientCode.append("\""+part.getName()+"\":");
			if(type != null)
				createClientTemplete(type.jaxbType,new ArrayList<Class<?>>());
			else
				clientCode.append("{}");
		}
	}

	private void createClientTemplete(Class<?> bean,
			ArrayList<Class<?>> stack) {
		int cont =0;
		for(Class<?> stackBean : stack ){
			if(stackBean.equals(bean)){//Recursion deducted
				cont++;
				if(cont >=2){
					clientCode.append("null");
					return;
				}
			}
		}
		if(bean.isEnum()){
			createClientEnum(bean);
			return;
		}
		if(JaxWsJSONPopulator.isJSONPrimitive(bean)){
			createClientPrimitive(bean);
			return;
		}
		if(bean != null){
			try{
				createBeanClient(bean,stack);
			}catch(Throwable th){
				clientCode.append(th.getMessage());
			}
		}
	}

	private void createClientEnum(Class<?> bean) {
		int size = bean.getEnumConstants().length;
		clientCode.append("\"\"");
	}
	
	private void createClientPrimitive(Class<?> bean) {
		clientCode.append("\"\"");
	}
	
	private void createBeanClient(Class<?> bean,ArrayList<Class<?>> stack) {
		clientCode.append("{");
		boolean hasField = false;
		nextField:
		for(Field field:bean.getDeclaredFields()){
			field.getAnnotations();
			for(Pattern patten:JSONCodec.excludeProperties){
				if(patten.matcher(field.getName()).matches()){
					continue nextField;
				}
			}
			if(field.getDeclaringClass().getName().equals(bean.getName())){
				if(field.getType() instanceof Class && !JaxWsJSONPopulator.isJSONPrimitive(field.getType())){
					clientCode.append("\""+escapeString(field.getName())+"\":");
					if(field.getType().getName().equals(JAXBElement.class.getName())){
						//TODO serialize element
						//serializeBean(field.getType(), model);
						clientCode.append("null");
					}if(Collection.class.isAssignableFrom(field.getType())){
						Type type = field.getGenericType();
						Class<?> itemClass = Object.class;
			            Type itemType = null;
			            if (type != null && type instanceof ParameterizedType) {
			                ParameterizedType ptype = (ParameterizedType) type;
			                itemType = ptype.getActualTypeArguments()[0];
			                if (itemType.getClass().equals(Class.class)) {
			                    itemClass = (Class) itemType;
			                } else {
			                    itemClass = (Class) ((ParameterizedType) itemType).getRawType();
			                }
			                clientCode.append("[");
			                stack.add(itemClass);
			                //createClientTemplete(itemClass,stack);
			                stack.remove(itemClass);
			                clientCode.append("]");
			            }else{
			            	clientCode.append("[]");
			            }
			            
					} else{
						stack.add(bean);
						createClientTemplete(field.getType(),stack);
						stack.remove(bean);
					}
				}else{
					XmlElement xmlElemnt = field.getAnnotation(XmlElement.class);
					clientCode.append("\""+escapeString(field.getName())+"\":\"\"");
					// Generate object meta data
				}
				clientCode.append(",");
				hasField = true;
			}
		}
		if(hasField)
			clientCode.replace(clientCode.length()-1, clientCode.length(), ""); // remove last extra ,
		clientCode.append("}");
	}
	
	private String escapeString(String input){
		try{
			return input.replaceAll("\\$", "");
		}catch(NullPointerException exp){
			return "null";
		}catch(Throwable th){
			return input;
		}
	}
	
	private void string(Object obj, StringBuffer out) {
		out.append('"');
		CharacterIterator it = new StringCharacterIterator(obj.toString());
		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
			if (c == '"') {
				out.append("\\\"");
			} else if (c == '\\') {
				out.append("\\\\");
			} else if (c == '/') {
				out.append("\\/");
			} else if (c == '\b') {
				out.append("\\b");
			} else if (c == '\f') {
				out.append("\\f");
			} else if (c == '\n') {
				out.append("\\n");
			} else if (c == '\r') {
				out.append("\\r");
			} else if (c == '\t') {
				out.append("\\t");
			} else if (Character.isISOControl(c)) {
				// this.unicode(c);
			} else {
				out.append(c);
			}
		}
		out.append('"');
	}
	
	public String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
	
	
	public void doResponse(OutputStream ouStream) throws IOException{
		ouStream.write(clientCode.toString().getBytes());
		ouStream.flush();
	}
}
