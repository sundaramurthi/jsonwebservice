package com.jaxws.json.codec.doc;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import com.jaxws.json.codec.JSONBindingID;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;


/**
 * @author Sundaramurthi Saminathan
 * @since 0.7
 * @version 1.0
 * 
 * AbstractHttpMetadataProvider provide common utility methods required for http meta data providers.
 * 
 * methods getMetadataModelMap provides map of service end point map which includes all port operation and messages.
 * 
 * @see com.jaxws.json.codec.doc.provider.MetaDataModelProvider
 * @see com.jaxws.json.codec.doc.provider.MethodFormProvider
 * @see com.jaxws.json.codec.doc.provider.JSMetaDataModelProvider
 * @see com.jaxws.json.jquery.doc.provider.JQueryMetaDataModelServer
 */
public abstract class AbstractHttpMetadataProvider implements
		HttpMetadataProvider {
	/**
	 * @return ordered map contains service, port, operation and messages.
	 */
	protected Map<String,Object> getMetadataModelMap(WSEndpoint<?> endPoint,boolean includePortAddress){
		Map<String,Object> 		metadataModel 	= new LinkedHashMap<String, Object>();
		JAXBContextImpl 		context 		= (JAXBContextImpl)endPoint.getSEIModel().getJAXBContext();
		Map<String,Object>  	service 		= new LinkedHashMap<String, Object>();
		metadataModel.put(endPoint.getServiceName().getLocalPart(), service );
		
		// TODO this method gets back all available service and ports.  Filter only specified service.
		Module 				modules 		= endPoint.getContainer().getSPI(com.sun.xml.ws.api.server.Module.class);
		/*
		 * Iterate through all bind endpoints to get service ports and operations 
		 */
		for(BoundEndpoint endPointObj : modules.getBoundEndpoints()){
			if(endPointObj.getEndpoint().getBinding().getBindingID() == JSONBindingID.JSON_BINDING){
				Map<String,Object>   portJSONMap 	= new LinkedHashMap<String, Object>();
				service.put(endPointObj.getEndpoint().getPortName().getLocalPart(), portJSONMap);
				
				SEIModel 	seiModel 		= endPointObj.getEndpoint().getSEIModel();
				for (WSDLBoundOperation operation : seiModel.getPort().getBinding().getBindingOperations()) {
					Map<String,Object>    operationMap = new LinkedHashMap<String, Object>();
					portJSONMap.put(operation.getName().getLocalPart(), operationMap );
					
					// TODO key should be a input message part name, Why here operation name?
					operationMap.put(operation.getOperation().getName().getLocalPart(), 
							JSONHttpMetadataPublisher.getJSONAsMap(operation.getInParts(),
							context));
					
					operationMap.put(operation.getOperation().getOutput().getName(),
							JSONHttpMetadataPublisher.getJSONAsMap(operation.getOutParts(), context));
				}
				if(includePortAddress){
					if(endPointObj instanceof HttpAdapter){
						portJSONMap.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,  "#BASEADDRESS#" + ((HttpAdapter)endPointObj).getValidPath());
					} else {
						portJSONMap.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPointObj.getEndpoint().getPort().getAddress().toString());
					}
				}
			}
		}
		return metadataModel;
	}
	
	/**
	 * @param ouStream
	 * @param content
	 * @throws IOException
	 * 
	 * Write string into response by replacing standard constants.
	 * Replacing values: "#BASEADDRESS#"
	 * 
	 */
	public void doResponse(WSHTTPConnection ouStream, String content) throws IOException {
		if(content != null){
			ouStream.getOutput().write(
					content.replaceAll("#BASEADDRESS#",
							ouStream.getBaseAddress()).getBytes());
		}else{
			ouStream.getOutput().write("Content not found".getBytes());
		}
	}
}
