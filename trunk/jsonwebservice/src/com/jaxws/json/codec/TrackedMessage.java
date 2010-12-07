package com.jaxws.json.codec;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;

/**
 * @author ssaminathan
 *
 */
public class TrackedMessage extends Message {
	
	private Message that;
	private DebugTrace trace;

	public TrackedMessage(Message that,DebugTrace trace){
		if(that == null)
			throw new NullPointerException();
		this.that = that;
		this.trace = trace;
	}
	
	@Override
	public Message copy() {
		return that.copy();
	}

	@Override
	public HeaderList getHeaders() {
		return that.getHeaders();
	}

	@Override
	public String getPayloadLocalPart() {
		return that.getPayloadLocalPart();
	}

	@Override
	public String getPayloadNamespaceURI() {
		return that.getPayloadNamespaceURI();
	}

	@Override
	public boolean hasHeaders() {
		return that.hasHeaders();
	}

	@Override
	public boolean hasPayload() {
		return that.hasPayload();
	}

	@Override
	public SOAPMessage readAsSOAPMessage() throws SOAPException {
		return that.readAsSOAPMessage();
	}

	@Override
	public Source readEnvelopeAsSource() {
		return that.readEnvelopeAsSource();
	}

	@Override
	public XMLStreamReader readPayload() throws XMLStreamException {
		return that.readPayload();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller)
			throws JAXBException {
		return (T) that.readPayloadAsJAXB(unmarshaller);
	}

	@Override
	public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
		return that.readPayloadAsJAXB(bridge);
	}

	@Override
	public Source readPayloadAsSource() {
		return that.readPayloadAsSource();
	}

	@Override
	public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
		that.writePayloadTo(sw);
	}

	@Override
	public void writeTo(XMLStreamWriter sw) throws XMLStreamException {
		that.writeTo(sw);
	}

	@Override
	public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler)
			throws SAXException {
		that.writeTo(contentHandler, errorHandler);
		
	}

	public DebugTrace getTrace() {
		return trace;
	}

	public void setTrace(DebugTrace trace) {
		this.trace = trace;
	}
	
}
