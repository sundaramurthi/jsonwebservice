package com.test.jsonwebservice;

import java.io.IOException;
import java.io.OutputStream;

import com.jaxws.json.packet.handler.Encoder;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.ContentType;

public class ObjectEncoder implements Encoder {

	public String mimeContent() {
		return "text/html";
	}
	
	public ContentType encode(Packet packet, OutputStream out)
			throws IOException {
		out.write("<html><body>object</body></html>".getBytes());
		return html;
	}

	public ContentType getStaticContentType(Packet arg0) {
		return html;
	}

	ContentType html = new ContentType(){

		public String getAcceptHeader() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getContentType() {
			return "text/html";
		}

		public String getSOAPActionHeader() {
			// TODO Auto-generated method stub
			return null;
		}
		
	};

}
