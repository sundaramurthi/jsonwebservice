package com.jaxws.json.parser;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

import com.jaxws.json.codec.JSONBindingID;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.model.ParameterBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.model.wsdl.WSDLBoundOperationImpl;
import com.sun.xml.ws.model.wsdl.WSDLBoundPortTypeImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.model.wsdl.WSDLServiceImpl;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.ParserUtil;
import com.sun.xml.ws.wsdl.parser.SOAPConstants;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;

public class WSDLJSONParserExtension extends WSDLParserExtension {
	
	@Override
	public boolean bindingElements(WSDLBoundPortType binding,
			XMLStreamReader reader) {
		QName name = reader.getName();
        if (JSONBindingID.NS_JSON_BINDING.equals(name) && binding instanceof WSDLBoundPortTypeImpl ) {
        	WSDLBoundPortTypeImpl bindingImpl = (WSDLBoundPortTypeImpl)binding;
        	//bindingImpl.setBindingId(JSONBindingID.JSON_HTTP);
        	bindingImpl.setBindingId(BindingID.SOAP11_HTTP);
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
	public boolean portElements(WSDLPort port, XMLStreamReader reader) {
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
	public boolean bindingOperationElements(WSDLBoundOperation operation,
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
	public boolean bindingOperationInputElements(WSDLBoundOperation operation,
			XMLStreamReader reader) {
		QName name = reader.getName();
        if (SOAPConstants.QNAME_BODY.equals(name)) {
        	WSDLBoundOperationImpl bindingOp = (WSDLBoundOperationImpl)operation;
            bindingOp.setInputExplicitBodyParts(parseSOAPBodyBinding(reader, bindingOp, BindingMode.INPUT));
            goToEnd(reader);
        }
		return super.bindingOperationInputElements(operation, reader);
	}

	@Override
	public boolean bindingOperationOutputElements(WSDLBoundOperation operation,
			XMLStreamReader reader) {
		QName name = reader.getName();
		 if (SOAPConstants.QNAME_BODY.equals(name)) {
			 WSDLBoundOperationImpl bindingOp = (WSDLBoundOperationImpl)operation;
             bindingOp.setOutputExplicitBodyParts(parseSOAPBodyBinding(reader, bindingOp, BindingMode.OUTPUT));
             goToEnd(reader);
         }
		return super.bindingOperationOutputElements(operation, reader);
	}
	
	 private enum BindingMode {
	        INPUT, OUTPUT, FAULT}

	    private static boolean parseSOAPBodyBinding(XMLStreamReader reader, WSDLBoundOperationImpl op, BindingMode mode) {
	        String namespace = reader.getAttributeValue(null, "namespace");
	        if (mode == BindingMode.INPUT) {
	            op.setRequestNamespace(namespace);
	            return parseSOAPBodyBinding(reader, op.getInputParts());
	        }
	        //resp
	        op.setResponseNamespace(namespace);
	        return parseSOAPBodyBinding(reader, op.getOutputParts());
	    }

	    /**
	     * Returns true if body has explicit parts declaration
	     */
	    private static boolean parseSOAPBodyBinding(XMLStreamReader reader, Map<String, ParameterBinding> parts) {
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

	
}
