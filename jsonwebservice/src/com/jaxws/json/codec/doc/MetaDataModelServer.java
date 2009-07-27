package com.jaxws.json.codec.doc;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.sql.Timestamp;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.validation.SchemaFactory;

import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Facet;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.exolab.castor.xml.schema.simpletypes.AtomicType;
import org.xml.sax.InputSource;

import com.jaxws.json.JaxWsJSONPopulator;
import com.jaxws.json.codec.JSONBindingID;
import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.serializer.CustomSerializer;
import com.sun.istack.NotNull;
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

public class MetaDataModelServer {
	StringBuilder model = new StringBuilder();
	
	JAXBContextImpl context;// Should use wsdl TODO
	HashSet<Class<?>> reusableObjects;
	ArrayList<Class<?>> stack = new ArrayList<Class<?>>();
	org.exolab.castor.xml.schema.Schema schema;
	String serviceName ="UNDEFINDED";
	Map<Class<? extends Object>,CustomSerializer> customCodecs;
	public MetaDataModelServer(WSEndpoint<?> endPoint,
			boolean all,boolean jsRoot,@NotNull
			JSONCodec codec) {
		this.customCodecs = codec.getCustomSerializer();
		SchemaFactory sf = SchemaFactory.newInstance(
			      javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			URL url = new URL(endPoint.getPort().getAddress().getURL().toExternalForm()+"?xsd=1");
			SchemaReader r = new SchemaReader(new InputSource(url.openStream()));
			schema = r.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reusableObjects = new ReusableDataModelProvider(endPoint,all).getReusableObjects();
		createMetaDataModel(endPoint,all,jsRoot);
	}

	public void createMetaDataModel(WSEndpoint<?> endPoint,boolean all,boolean jsRoot) {
		context = (JAXBContextImpl)endPoint.getSEIModel().getJAXBContext();
		model = new StringBuilder();
		serviceName 	= endPoint.getServiceName().getLocalPart();
		if(!jsRoot){
			model.append("{");
			model.append("\""+serviceName+"\":{");// Service start
		}
		else
			model.append("WSCatalog.services."+serviceName+" = {");
		
		model.append("\"types\":{");
		for(Class<?> bean:reusableObjects){
			model.append("\""+bean.getSimpleName()+"\":");
			createBeanMetaData(bean,stack);
			model.append(",");
		}
		model.append("},\"ports\":{");
		
		
		if(all){
			Module modules = endPoint.getContainer().getSPI(com.sun.xml.ws.api.server.Module.class);
			for(BoundEndpoint endPointObj : modules.getBoundEndpoints()){
				if(endPointObj.getEndpoint().getBinding().getBindingID() == JSONBindingID.JSON_BINDING){
					createMetaDataModel( endPointObj.getEndpoint());
					model.append(",");
				}
			}
			model.replace(model.length()-1, model.length(), ""); // remove last extra ,
		}else{
			createMetaDataModel(endPoint);
		}
		model.append(	"}"+// Service end
					"}");// Object warper end
		if(!jsRoot)
			model.append("}");
	}
	
	private void createMetaDataModel(WSEndpoint jsonEndpoint){
		WSDLPortType jsonPort = jsonEndpoint.getPort().getBinding().getPortType();
		model.append("\""+jsonPort.getName().getLocalPart()+"\":{");
		// Operations meta data
		String methodUrl 	= "";
		if(jsonEndpoint.getPort().getAddress().getURL() != null)
			methodUrl = jsonEndpoint.getPort().getAddress().getURL().getPath();
		String mimeType 	= jsonEndpoint.createCodec().getMimeType();
		model.append("\"operations\":{");
		for(WSDLOperation operation : jsonPort.getOperations()){
			createMetaDataModel(operation,methodUrl,mimeType);
			model.append(",");
		}
		model.replace(model.length()-1, model.length(), ""); // remove last extra ,
		model.append("}");
		// End Operations meta
		model.append("}");
	}
	
	
	private void createMetaDataModel(WSDLOperation jsonOperation,String methodUrl,String mimeType ) {
		model.append("\""+jsonOperation.getName().getLocalPart()+"\":{");
		createMetaDataModel(jsonOperation.getInput());
		model.append(",");
		createMetaDataModel(jsonOperation.getOutput());
		model.append(",");
		createMetaDataOperationConfig(jsonOperation,methodUrl,mimeType);
		//Operation close
		model.append("}");
	}
	
	private void createMetaDataOperationConfig(WSDLOperation jsonOperation,String methodUrl, String mimeType){
		model.append("\"config\":{"+
				"\"endPoint\":\""+methodUrl+"\","+
				//"\"requestContentType\":\""+mimeType+"\","+
				//"\"responseContentType\":\""+mimeType+"\","+
				//"\"zipedResponse\":\"false\","+
				//"\"statusAware\":\"false\","+//TODO read from method config
				//"\"statusUrl\":\"null\","+//TODO read from method config
				//"\"featureUrl\":\"null\","+//TODO read from method config
				"\"operationName\":\""+jsonOperation.getName().getLocalPart()+"\"");
				//"\"requestPayload\":\""+jsonOperation.getName().getLocalPart()+"\","+
				//"\"responsePayload\":\""+jsonOperation.getOutput().getMessage().getName().getLocalPart()+"\"");
		model.append("}");
	}

	private void createMetaDataModel(WSDLInput input){
		model.append("\"input\":{");
		createMetaDataModel(input.getMessage());
		model.append("}");
	}
	
	private void createMetaDataModel(WSDLOutput output){
		model.append("\"output\":{");
		if(output != null)
			createMetaDataModel(output.getMessage());
		model.append("}");
	}
	
	private void createMetaDataModel(WSDLMessage message){
		createMetaDataModel(message.parts());
	}
	
	private void createMetaDataModel(Iterable<? extends WSDLPart> parts) {
		boolean hasParts = false;
		for(WSDLPart part : parts){
			hasParts = true;
			createMetaDataModel(part);
			model.append(",");
		}
		if(hasParts)
			model.replace(model.length()-1, model.length(), ""); // remove last extra ,
	}

	private void createMetaDataModel(WSDLPart part) {
		WSDLPartDescriptor partDescription = (WSDLPartDescriptorImpl) part.getDescriptor();
		if(context != null){
			JaxBeanInfo type = context.getGlobalType(partDescription.name());
			model.append("\""+part.getName()+"\":");
			if(type != null)
			createMetaDataModel(type.jaxbType,stack);
			else
				model.append("{}");
		}
	}

	private void createMetaDataModel(Class<?> bean,
			ArrayList<Class<?>> stack) {
		int cont =0;
		for(Class<?> stackBean : stack ){
			if(stackBean.equals(bean)){//Recursion deducted
				cont++;
				if(cont >=2){
					model.append("null");
					return;
				}
			}
		}
		if(customCodecs.containsKey(bean) && customCodecs.get(bean).canBeHandled(null)){
        	customCodecs.get(bean).metaData(model);
        }else if(bean.isEnum()){
			createMetaDataEnum(bean);
		}else if(JaxWsJSONPopulator.isJSONPrimitive(bean)){
			createMetaDataPrimitive(bean,null,null);
		}else if(bean != null){
			try{
				if(reusableObjects.contains(bean)){
					model.append("{");
					model.append("\"type\":\"ref:"+serviceName+"."+bean.getSimpleName()+"\",");
					model.append("\"required\":true");
					model.append("}");
				}else{
					createBeanMetaData(bean,stack);
				}
			}catch(Throwable th){
				model.append(th.getMessage());
			}
		}
	}

	private void createMetaDataEnum(Class<?> bean) {
		int size = bean.getEnumConstants().length;
		model.append("{"+
						"\"defaultValue\":\""+null+"\","+// TODO fix
						"\"type\":\"string\","+
						"\"required\":\"true\","+
						"\"restriction\":{"+
							"\"enumeration\":[");
								for(Object cnst:bean.getEnumConstants()){
									model.append("\""+cnst.toString()+"\""+(size-- <= 1?"":","));
								}
				model.append("]}");
		model.append("}");
	}
	
	private void createMetaDataPrimitive(Class<?> bean,XmlElement xmlElemnt, ElementDecl elmDecl) {
		String defaultValue = "\"\"";
		boolean isRequired 	= xmlElemnt != null ? xmlElemnt.required():false;
		String 	type 		= bean.getName();
		String 	restriction = "";
		
		try{
			if(bean.getName().equals("short") || bean.equals(Short.class)){
				if(xmlElemnt ==null || xmlElemnt.defaultValue() == null || xmlElemnt.defaultValue().charAt(0) == '\0'){
					defaultValue = "0"; // Not defined in xsd
				}else
					defaultValue = xmlElemnt.defaultValue();
					
				type	= "number";// Make it  primitive even for Object
				restriction = "\"minInclusive\":"+Short.MIN_VALUE+","+
							  "\"maxInclusive\":"+Short.MAX_VALUE;
				
			}else if(bean.getName().equals("int") || bean.equals(Integer.class)){
				if(xmlElemnt == null || xmlElemnt.defaultValue() == null || xmlElemnt.defaultValue().charAt(0) == '\0' ){
					defaultValue = "0"; // Not defined in xsd
				}else
					defaultValue = xmlElemnt.defaultValue();
				type	= "number";// Make it  primitive even for Object
				restriction = "\"minInclusive\":"+Integer.MIN_VALUE+","+
							  "\"maxInclusive\":"+Integer.MAX_VALUE;
			}else if(bean.getName().equals("long") || bean.equals(Long.class)){
				if(xmlElemnt == null || xmlElemnt.defaultValue() == null || xmlElemnt.defaultValue().charAt(0) == '\0' ){
					defaultValue = "0"; // Not defined in xsd
				}else
					defaultValue = xmlElemnt.defaultValue();
				type	= "number";// Make it  primitive even for Object
				restriction = "\"minInclusive\":"+Long.MIN_VALUE+","+
							  "\"maxInclusive\":"+Long.MAX_VALUE;
			}else if(bean.getName().equals("byte") || bean.equals(Byte.class)){
				if(xmlElemnt == null || xmlElemnt.defaultValue() == null || xmlElemnt.defaultValue().charAt(0) == '\0' ){
					defaultValue = "0"; // Not defined in xsd
				}else
					defaultValue = xmlElemnt.defaultValue();
				type	= "number";// Make it  primitive even for Object
				restriction = "\"minInclusive\":"+Byte.MIN_VALUE+","+
							  "\"maxInclusive\":"+Byte.MAX_VALUE;
			}else if(bean.equals(String.class)){
				if(xmlElemnt != null && xmlElemnt.defaultValue() != null && xmlElemnt.defaultValue().charAt(0) != '\0'){
					defaultValue = string(xmlElemnt.defaultValue());
				}
				type	= "string";// Make it  primitive even for Object
				//restriction = "\"minLength\":0,"+
					//		  "\"maxLength\":255";
			}else if(bean.getName().equals("boolean") ||bean.equals(Boolean.class)){
				if(xmlElemnt == null || xmlElemnt.defaultValue() == null || xmlElemnt.defaultValue().charAt(0) == '\0' ){
					defaultValue = "false"; // Not defined in xsd
				}else
					defaultValue = xmlElemnt.defaultValue();
				type	= "boolean";// Make it  primitive even for Object
			}else if(bean.equals(Date.class) || bean.equals(Timestamp.class) ){
				if(xmlElemnt == null || xmlElemnt.defaultValue() == null || xmlElemnt.defaultValue().charAt(0) == '\0' ){
					defaultValue = "null"; // Not defined in xsd
				}else
					defaultValue = xmlElemnt.defaultValue();
				type	= "dateTime";// Make it  primitive even for Object
			}else{
				defaultValue = "\"PleaseReportBug"+bean+"\"";
			}
		}catch(Throwable th){
			th.printStackTrace();
		}
		if(elmDecl != null){
			try{
				StringBuffer facetsBuffer = new StringBuffer();
				Enumeration<Facet> facets = ((AtomicType)elmDecl.getType()).getFacets();
				while(facets.hasMoreElements()){
					Facet facet = facets.nextElement();
					facetsBuffer.append(",\""+facet.getName()+"\":\""+facet.getValue()+"\"");
				}
				if(restriction.trim().equals("")){
					restriction = facetsBuffer.toString().replaceFirst(",", "");
				}else{
					restriction += facetsBuffer.toString();
				}
			}catch(Throwable th){}
		}
		model.append("{");
			model.append("\"defaultValue\":"+defaultValue);
			model.append(", \"required\":"+isRequired);
			model.append(", \"type\":\""+type+"\"");
			model.append(", \"restriction\":{");
				model.append(restriction);
			model.append("}");
		model.append("}");
	}
	
	private void createBeanMetaData(Class<?> bean,ArrayList<Class<?>> stack) {
		ComplexType complexType = null;
		try{
			complexType = schema.getComplexType(bean.getSimpleName());
		}catch(Throwable th){}
		model.append("{");
		model.append("\"fields\":{");
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
					model.append("\""+escapeString(field.getName())+"\":");
					if(field.getType().getName().equals(JAXBElement.class.getName())){
						//TODO serialize element
						//serializeBean(field.getType(), model);
						model.append("null");
					}if(Collection.class.isAssignableFrom(field.getType())){
						Type type = field.getGenericType();
						Class<?> itemClass = Object.class;
			            Type itemType = null;
			            if (type != null && type instanceof ParameterizedType) {
			            	int minOccurs = 0;
			            	int maxOccurs = -1;
			            	if(complexType != null){
			            		ElementDecl elmDecl = complexType.getElementDecl(field.getName());
			            		if(elmDecl != null){
			            			minOccurs = elmDecl.getMinOccurs();
			            			maxOccurs	= elmDecl.getMaxOccurs();
			            		}
							}
			            	
			                ParameterizedType ptype = (ParameterizedType) type;
			                itemType = ptype.getActualTypeArguments()[0];
			                if (itemType.getClass().equals(Class.class)) {
			                    itemClass = (Class) itemType;
			                } else {
			                    itemClass = (Class) ((ParameterizedType) itemType).getRawType();
			                }
			                model.append("{");
				    			model.append("\"type\":\"array\"");
				    			model.append(", \"genericType\":");
				    			stack.add(itemClass);
				                createMetaDataModel(itemClass,stack);
				                stack.remove(itemClass);
				    			model.append(", \"restriction\":{");
				    				model.append("\"minOccurs\":"+minOccurs);
				    				if(maxOccurs > -1)
				    					model.append(",\"maxOccurs\":"+maxOccurs);
				    			model.append("}");
				    		model.append("}");
			            }else{
			            	model.append("[]");
			            }
			            
					} else{
						stack.add(bean);
						createMetaDataModel(field.getType(),stack);
						stack.remove(bean);
					}
				}else if(field.getType().isEnum()){
					model.append("\""+escapeString(field.getName())+"\":");
					createMetaDataEnum(field.getType());
				}else{
					// Should be primitive
					XmlElement xmlElemnt = field.getAnnotation(XmlElement.class);
					ElementDecl elmDecl =null;
					if(complexType != null){
						elmDecl = complexType.getElementDecl(field.getName());
					}
					model.append("\""+escapeString(field.getName())+"\":");
					createMetaDataPrimitive(field.getType(),xmlElemnt,elmDecl);
				}
				model.append(",");
				hasField = true;
			}
		}
		if(hasField)
			model.replace(model.length()-1, model.length(), ""); // remove last extra ,
		else
			model.append("\"NO_FIELD\":\""+bean.getSimpleName()+"\"");
		model.append("},\"type\":\"object\"");
		model.append("}");
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
	
	private String string(Object obj) {
		StringBuffer out = new StringBuffer();
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
		return out.toString();
	}
	
	
	public void doResponse(OutputStream ouStream) throws IOException{
		ouStream.write(model.toString().getBytes());
		ouStream.flush();
	}
}
