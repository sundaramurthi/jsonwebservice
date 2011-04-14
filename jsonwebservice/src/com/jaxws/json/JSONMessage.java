package com.jaxws.json;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;

import org.jvnet.mimepull.MIMEPart;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.decode.WSJSONPopulator;
import com.jaxws.json.feature.JSONWebService;
import com.sun.istack.Nullable;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;

public class JSONMessage extends Message {

	private @Nullable
	HeaderList headers;
	
	private WSDLBoundOperation operation;

	private WSJSONPopulator jsonPopulator;

	private Map<String, Object> elements;

	public JSONMessage(@Nullable HeaderList headers,WSDLBoundOperation operation,Map<String,Object>  operationParameters,
			WSJSONPopulator 	jsonPopulator) {
        this.headers 		= headers;
        this.operation  	= operation;
        this.jsonPopulator 	= jsonPopulator;
        this.elements		= operationParameters;
	}
	public boolean hasHeaders() {
		return headers != null && !headers.isEmpty();
	}

	public HeaderList getHeaders() {
		if (headers == null) {
			headers = new HeaderList();
		}
		return headers;
	}

	@Override
	public String getPayloadLocalPart() {
		return operation.getName().getLocalPart();
	}

	@Override
	public String getPayloadNamespaceURI() {
		return operation.getName().getNamespaceURI();
	}

	@Override
	public boolean hasPayload() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Source readEnvelopeAsSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Source readPayloadAsSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SOAPMessage readAsSOAPMessage() throws SOAPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller)
			throws JAXBException {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
		try {
			Class<?> parameterType = (Class<?>)bridge.getTypeReference().type;
			if(!WSJSONPopulator.isJSONPrimitive(parameterType)){
            	return (T) jsonPopulator.populateObject(jsonPopulator.getNewInstance(parameterType),
	            		(Map<String,Object>)elements.get(bridge.getTypeReference().tagName.getLocalPart()),null, 
	            		null);
            } else {
            	return (T) jsonPopulator.convert(parameterType, null, elements.get(bridge.getTypeReference().tagName.getLocalPart()),
            			/*seiMethod != null ? seiMethod.getAnnotation(JSONWebService.class) :*/ null, null);
            }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public XMLStreamReader readPayload() throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
		// TODO Auto-generated method stub
		System.out.println("");
	}

	@Override
	public void writeTo(XMLStreamWriter sw) throws XMLStreamException {
		// TODO Auto-generated method stub
		System.out.println("");
	}

	@Override
	public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public Message copy() {
		// TODO Auto-generated method stub
		return null;
	}

}
