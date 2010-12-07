package com.jaxws.json.codec.doc;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;

import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Facet;
import org.exolab.castor.xml.schema.simpletypes.AtomicType;

import com.jaxws.json.codec.JSONBindingID;
import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.decode.WSJSONPopulator;
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

public class ReusableDataModelProvider {
	
	JAXBContextImpl context;// Should use wsdl TODO
	HashSet<Class<?>> reusableObjects = new HashSet<Class<?>>();
	HashSet<Class<?>> uiqueObjects = new HashSet<Class<?>>();
	public ReusableDataModelProvider(WSEndpoint<?> endPoint,boolean all) {
		createMetaDataModel(endPoint,all);
	}

	public void createMetaDataModel(WSEndpoint<?> endPoint,boolean all) {
		context = (JAXBContextImpl)endPoint.getSEIModel().getJAXBContext();
		if(all){
			Module modules = endPoint.getContainer().getSPI(com.sun.xml.ws.api.server.Module.class);
			for(BoundEndpoint endPointObj : modules.getBoundEndpoints()){
				if(endPointObj.getEndpoint().getBinding().getBindingID() == JSONBindingID.JSON_BINDING){
					createMetaDataModel( endPointObj.getEndpoint());
				}
			}
		}else{
			createMetaDataModel(endPoint);
		}
	}
	
	private void createMetaDataModel(WSEndpoint jsonEndpoint){
		WSDLPortType jsonPort = jsonEndpoint.getPort().getBinding().getPortType();
		// Operations meta data
		String methodUrl 	= "";
		if(jsonEndpoint.getPort().getAddress().getURL() != null)
			methodUrl = jsonEndpoint.getPort().getAddress().getURL().getPath();
		String mimeType 	= jsonEndpoint.createCodec().getMimeType();
		for(WSDLOperation operation : jsonPort.getOperations()){
			createMetaDataModel(operation,methodUrl,mimeType);
		}
	}
	
	
	private void createMetaDataModel(WSDLOperation jsonOperation,String methodUrl,String mimeType ) {
		createMetaDataModel(jsonOperation.getInput());
		createMetaDataModel(jsonOperation.getOutput());
	}

	private void createMetaDataModel(WSDLInput input){
		createMetaDataModel(input.getMessage());
	}
	
	private void createMetaDataModel(WSDLOutput output){
		if(output != null)
			createMetaDataModel(output.getMessage());
	}
	
	private void createMetaDataModel(WSDLMessage message){
		createMetaDataModel(message.parts());
	}
	
	private void createMetaDataModel(Iterable<? extends WSDLPart> parts) {
		for(WSDLPart part : parts){
			createMetaDataModel(part);
		}
	}

	private void createMetaDataModel(WSDLPart part) {
		WSDLPartDescriptor partDescription = (WSDLPartDescriptorImpl) part.getDescriptor();
		if(context != null){
			JaxBeanInfo type = context.getGlobalType(partDescription.name());
			if(type != null)
			createMetaDataModel(type.jaxbType,new ArrayList<Class<?>>());
		}
	}

	private void createMetaDataModel(Class<?> bean,
			ArrayList<Class<?>> stack) {
		for(Class<?> stackBean : stack ){
			if(stackBean.equals(bean)){//Recursion deducted
				return;
			}
		}
		if(uiqueObjects.contains(bean)){
			reusableObjects.add(bean);
		}else{
			uiqueObjects.add(bean);
		}
		if(bean.isEnum()){
			createMetaDataEnum(bean);
			return;
		}
		if(WSJSONPopulator.isJSONPrimitive(bean)){
			createMetaDataPrimitive(bean,null,null);
			return;
		}
		if(bean != null){
			try{
				createBeanMetaData(bean,stack);
			}catch(Throwable th){
			}
		}
	}

	private void createMetaDataEnum(Class<?> bean) {
		int size = bean.getEnumConstants().length;
	}
	
	private void createMetaDataPrimitive(Class<?> bean,XmlElement xmlElemnt, ElementDecl elmDecl) {
		boolean isRequired 	= xmlElemnt != null ? xmlElemnt.required():false;
		if(elmDecl != null){
			StringBuffer facetsBuffer = new StringBuffer();
			Enumeration<Facet> facets = ((AtomicType)elmDecl.getType()).getFacets();
			while(facets.hasMoreElements()){
				Facet facet = facets.nextElement();
				facetsBuffer.append(",\""+facet.getName()+"\":\""+facet.getValue()+"\"");
			}
		}
	}
	
	private void createBeanMetaData(Class<?> bean,ArrayList<Class<?>> stack) {
		
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
				if(field.getType() instanceof Class && !WSJSONPopulator.isJSONPrimitive(field.getType())){
					if(field.getType().getName().equals(JAXBElement.class.getName())){
						//TODO serialize element
						//serializeBean(field.getType(), model);
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
			                
				    			stack.add(itemClass);
				                createMetaDataModel(itemClass,stack);
				                stack.remove(itemClass);
				    			
			            }
			            
					} else{
						stack.add(bean);
						createMetaDataModel(field.getType(),stack);
						stack.remove(bean);
					}
				}else if(field.getType().isEnum()){
					createMetaDataEnum(field.getType());
				}else{
					// Should be primitive
					XmlElement xmlElemnt = field.getAnnotation(XmlElement.class);
					//createMetaDataPrimitive(field.getType(),xmlElemnt);
				}
				hasField = true;
			}
		}
		
	}

	public HashSet<Class<?>> getReusableObjects() {
		return reusableObjects;
	}

	public HashSet<Class<?>> getUiqueObjects() {
		return uiqueObjects;
	}
}
