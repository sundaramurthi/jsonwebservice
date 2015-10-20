package com.jaxws.json.parser;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import com.jaxws.json.codec.JSONBindingID;
import com.jaxws.json.codec.JSONConstants;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.model.ParameterBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLPort;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.model.wsdl.WSDLBoundOperationImpl;
import com.sun.xml.ws.model.wsdl.WSDLBoundPortTypeImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.ParserUtil;
import com.sun.xml.ws.wsdl.parser.SOAPConstants;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;

public class WSDLJSONParserExtension extends WSDLParserExtension {
	private static final Logger 	LOG		= Logger.getLogger(WSDLJSONParserExtension.class.getName());
	@Override
	public boolean bindingElements(EditableWSDLBoundPortType binding,
			XMLStreamReader reader) {
		QName name = reader.getName();
        if (JSONBindingID.NS_JSON_BINDING.equals(name) && binding instanceof WSDLBoundPortTypeImpl ) {
        	WSDLBoundPortTypeImpl bindingImpl = (WSDLBoundPortTypeImpl)binding;
        	bindingImpl.setBindingId(JSONBindingID.JSON_HTTP);
        	//bindingImpl.setBindingId(BindingID.SOAP11_HTTP);
            String style = reader.getAttributeValue(null, "style");

            if ((style != null) && (style.equals("rpc"))) {
            	bindingImpl.setStyle(Style.RPC);
            } else {
            	bindingImpl.setStyle(Style.DOCUMENT);
            }
            goToEnd(reader);
            return true;
        }
        return false;
	}
	
	@Override
	public boolean portElements(EditableWSDLPort port, XMLStreamReader reader) {
		 QName name = reader.getName();
		if(JSONBindingID.QNAME_ADDRESS.equals(name)){
			String location = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_LOCATION);
	        if (location != null && port instanceof WSDLPortImpl) {
	            try {
	                ((WSDLPortImpl)port).setAddress(new EndpointAddress(location));
	            } catch (URISyntaxException e) {
	                //Lets not throw any exception, latter on it should be thrown when invocation happens. At this
	                // time user has option to set the endopint address using request contexxt property.
	            }
	        }
	        XMLStreamReaderUtil.next(reader);
	        return true;
		}
        return false;
	}
	

	@Override
	public boolean bindingOperationElements(EditableWSDLBoundOperation operation,
			XMLStreamReader reader) {
		QName name = reader.getName();
		if (operation instanceof WSDLBoundOperationImpl && JSONBindingID.QNAME_OPERATION.equals(name)) {
           // style = reader.getAttributeValue(null, "style");
            String soapAction = reader.getAttributeValue(null, "soapAction");
            if (soapAction != null)
            	((WSDLBoundOperationImpl)operation).setSoapAction(soapAction);
            goToEnd(reader);
            return true;
        }
		return false;
	}
	
	@Override
	public boolean bindingOperationInputElements(EditableWSDLBoundOperation operation,
			XMLStreamReader reader) {
		return handleOperationInOutElements(operation, reader, BindingMode.INPUT) ? true : 
			super.bindingOperationInputElements(operation, reader);
	}

	@Override
	public boolean bindingOperationOutputElements(EditableWSDLBoundOperation operation,
			XMLStreamReader reader) {
		return handleOperationInOutElements(operation, reader, BindingMode.OUTPUT) ? true : 
			super.bindingOperationOutputElements(operation, reader);
	}
	
	private boolean handleOperationInOutElements(WSDLBoundOperation operation,
			XMLStreamReader reader,BindingMode mode){
		QName name = reader.getName();
		WSDLBoundOperationImpl bindingOp = (WSDLBoundOperationImpl)operation;
		if (JSONConstants.QNAME_BODY.equals(name) || SOAPConstants.QNAME_BODY.equals(name)) {
			if(mode == BindingMode.INPUT)
				bindingOp.setInputExplicitBodyParts(parseBodyBinding(reader, bindingOp, mode));
			else if(mode == BindingMode.OUTPUT)
				bindingOp.setOutputExplicitBodyParts(parseBodyBinding(reader, bindingOp, mode));
            goToEnd(reader);
            return true;
        } else if(JSONConstants.QNAME_CONTENT.equals(name) || 
        		/* remove in latter by notfiying major users.
        		 * FOR Support 06 and early users.*/com.sun.xml.rpc.wsdl.document.mime.MIMEConstants.QNAME_CONTENT.equals(name)){
        	// Direct content type appears in req/res binding. Vilating BP. 
        	// But for just content response E.g image this is required to avoid mutipart type
        	String type = reader.getAttributeValue(null, "type");
        	String part = reader.getAttributeValue(null, "part");
        	if(type != null && bindingOp != null){
        		if(BindingMode.INPUT == mode){
        			// part name , type
        			// FIXME bindingOp.getMimeTypeForInputPart(part).
        			//bindingOp.getInputMimeTypes().put(part != null ? part : "main" , type);
        		}else{
        			//bindingOp.getOutputMimeTypes().put(part != null ? part : "main" , type);
        		}
        	}
        	// Special case only in json
        	if(com.sun.xml.rpc.wsdl.document.mime.MIMEConstants.QNAME_CONTENT.equals(name)){
        		LOG.warning("Your operation json binding mime part using xmlsoap mime (http://schemas.xmlsoap.org/wsdl/mime/) namespace." +
        				"This namespace not going to be supported in feature version of jsonsoap plugin." +
        				" Suggested to update this namespce to jsonsoap (http://schemas.jsonsoap.org/wsdl/mime/) ");
        	}
        	/*
        	 * TODO PUT parts part of operation
        	 * 
        	 *  Map<String, ParameterBinding> parts = null;
		        if (mode == BindingMode.INPUT) {
		            parts = op.getInputParts();
		        } else if (mode == BindingMode.OUTPUT) {
		            parts = op.getOutputParts();
		        } else if (mode == BindingMode.FAULT) {
		            parts = op.getFaultParts();
		        }
        
                String part = reader.getAttributeValue(null, "part");
                String type = reader.getAttributeValue(null, "type");
                if ((part == null) || (type == null)) {
                    XMLStreamReaderUtil.skipElement(reader);
                    continue;
                }
                ParameterBinding sb = ParameterBinding.createAttachment(type);
                if (parts != null && sb != null && part != null)
                    parts.put(part, sb);
                XMLStreamReaderUtil.next(reader);
        	 */
        	goToEnd(reader);
        	return true;
			//bindingOp.addExtension(ex );
        	//operation.getOperation().addExtension(arg0)
        	//operation.getOutput().addExtension(WSDLExtension )
        }else if(JSONConstants.QNAME_MULTIPART_RELATED.equals(name)){//
        	//<mime:multipartRelated><mime:part><mime:content part="image" type="image/jpeg"/></mime:part></mime:multipartRelated>
        	parseMimeMultipartBinding(reader, bindingOp, mode);
			//goToEnd(reader);
        	return true;
        }
		return false;
	}
	
	 private enum BindingMode {
	        INPUT, OUTPUT, FAULT}

	    private static boolean parseBodyBinding(XMLStreamReader reader, WSDLBoundOperationImpl op, BindingMode mode) {
	        String namespace = reader.getAttributeValue(null, "namespace");
	        if (mode == BindingMode.INPUT) {
	            op.setRequestNamespace(namespace);
	            return parseBodyBinding(reader, op.getInputParts());
	        }
	        //resp
	        op.setResponseNamespace(namespace);
	        return parseBodyBinding(reader, op.getOutputParts());
	    }

	    /**
	     * Returns true if body has explicit parts declaration
	     */
	    private static boolean parseBodyBinding(XMLStreamReader reader, Map<String, ParameterBinding> parts) {
	        String partsString = reader.getAttributeValue(null, "parts");
	        if (partsString != null) {
	            List<String> partsList = XmlUtil.parseTokenList(partsString);
	            if (partsList.isEmpty()) {
	                parts.put(" ", ParameterBinding.BODY);
	            } else {
	                for (String part : partsList) {
	                    parts.put(part, ParameterBinding.BODY);
	                }
	            }
	            return true;
	        }
	        return false;
	    }

	private static void goToEnd(XMLStreamReader reader) {
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            XMLStreamReaderUtil.skipElement(reader);
        }
    }
	
	private void parseMimeMultipartBinding(XMLStreamReader reader, WSDLBoundOperationImpl op, BindingMode mode) {
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if (JSONConstants.QNAME_PART.equals(name)) {
                parseMIMEPart(reader, op, mode);
            } else {
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
    }

    private void parseMIMEPart(XMLStreamReader reader, WSDLBoundOperationImpl op, BindingMode mode) {
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
        	if(!handleOperationInOutElements(op, reader, mode))
                XMLStreamReaderUtil.skipElement(reader);
        }
    }

	
}
