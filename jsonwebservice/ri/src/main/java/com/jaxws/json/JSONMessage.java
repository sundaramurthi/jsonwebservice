package com.jaxws.json;

import java.io.StringReader;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.ws.Holder;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.jaxws.json.codec.decode.WSJSONPopulator;
import com.sun.istack.Nullable;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.message.EmptyMessageImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.WrapperComposite;
import com.sun.xml.ws.spi.db.XMLBridge;

public class JSONMessage extends Message {

	private @Nullable
	HeaderList headers;
	
	private WSDLBoundOperation operation;

	private WSJSONPopulator jsonPopulator;

	private Map<String, Object> elements;
	
	XMLInputFactory factory = XMLInputFactory.newInstance();

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
		return true;
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

	@Override
	public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
		return readPayloadAsJAXB((Class<?>)bridge.getTypeReference().type, bridge.getTypeReference().tagName.getLocalPart());
	}
	
	private <T> T readPayloadAsJAXB(Class<?> parameterType, String localPart) throws JAXBException {
		try {
			if(!WSJSONPopulator.isJSONPrimitive(parameterType)){
            	return (T) jsonPopulator.populateObject(jsonPopulator.getNewInstance(parameterType),
	            		(Map<String,Object>)elements.get(localPart),null, 
	            		null);
            } else {
            	return (T) jsonPopulator.convert(parameterType, null, elements.get(localPart),
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
		if(this.operation.getInputParts().isEmpty()){
			//JAXBMessage.create(BindingContext context, Object jaxbObject, SOAPVersion soapVersion).readPayload();
			XMLStreamReader reader = factory.createXMLStreamReader(new StringReader("<"+this.operation.getRequestPayloadName().getLocalPart()+" xmlns=\""+this.operation.getRequestNamespace()+"\" />"));
			reader.next();
			return reader;
		}else{
			//readPayloadAsJAXB(this.operation.getInputParts(). ,this.getPayloadLocalPart());
			return factory.createXMLStreamReader(new StringReader(""));
		}
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

	public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public Message copy() {
		// TODO Auto-generated method stub
		return null;
	}
	public <T> T readPayloadAsJAXB(XMLBridge<T> bridge) throws JAXBException {
		if(bridge.getTypeInfo().type.equals(com.sun.xml.ws.spi.db.WrapperComposite.class)){
			WrapperParameter p = ((WrapperParameter)(bridge.getTypeInfo().properties().get(com.sun.xml.ws.model.WrapperParameter.class.getName())));
			WrapperComposite com = new WrapperComposite();
			com.bridges = new XMLBridge[p.getWrapperChildren().size()];
			com.values = new Object[p.getWrapperChildren().size()];
			for(ParameterImpl child : p.getWrapperChildren()){
				com.bridges[child.getIndex()] = child.getXMLBridge();
				Object val = readPayloadAsJAXB((Class<?>)child.getTypeInfo().type, child.getTypeInfo().tagName.getLocalPart());
				if(child.isINOUT()){
					val = new Holder(val); 
				}
				com.values[child.getIndex()] = val;
			}
			if(p.getWrapperChildren().size() == 0){
				return null;
			}else if(p.getWrapperChildren().size() == 1){
				return (T)com.values[0];
			}else{
				return (T)com;
			}
		}
		return readPayloadAsJAXB((Class<?>)bridge.getTypeInfo().type, bridge.getTypeInfo().tagName.getLocalPart());
	}

}
