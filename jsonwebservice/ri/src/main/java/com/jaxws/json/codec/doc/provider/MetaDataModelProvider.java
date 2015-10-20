package com.jaxws.json.codec.doc.provider;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.doc.AbstractHttpMetadataProvider;
import com.jaxws.json.codec.doc.HttpMetadataProvider;
import com.jaxws.json.codec.encode.WSJSONWriter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

/**
 * @author Sundaramurthi Saminathan
 * @since JSONWebservice codec version 0.4
 * @version 2.0
 * 
 * Default JSON service end point document provider.
 * @deprecated suggested to use JSONSchema.
 */
public class MetaDataModelProvider extends AbstractHttpMetadataProvider implements HttpMetadataProvider {
	
	private static final String[] queries = new String[]{"jsonmodel","defaultjsonmodel"};
	
	/**
	 * Map holder which keeps end point documents.
	 */
	private final static Map<QName,String>	endPointDocuments	= Collections.synchronizedMap(new HashMap<QName,String>());
	
	/**
	 * Request received codec instance holder
	 */
	private JSONCodec codec;
	
	
	/**
	 * "jsonmodel" query handled.
	 */
	public String[] getHandlingQueries() {
		return queries;
	}

	/**
	 * Handler flag, If query string is jsonmodel , its handled by model server.
	 */
	public boolean canHandle(String queryString) {
		return queryString != null && (queryString.equals(queries[0]) || queryString.equals(queries[1]));
	}
	
	/**
	 * end point codec set holder.
	 */
	public void setJSONCodec(JSONCodec codec) {
		this.codec	= codec;
	}
	
	/**
	 * Meta data model content provider.
	 * @see HttpMetadataProvider.getContentType
	 */
	public String getContentType() {
		return "application/json; charset=\"utf-8\"";
	}

	public void process() {
		endPointDocuments.put(this.codec.getEndpoint().getServiceName(),
				WSJSONWriter.writeMetadata(getMetadataModelMap(this.codec.getEndpoint(),true),
						this.codec.getCustomSerializer()));
		
		// TODO Auto-generated method stub
		/*XmlElement xmlElemnt = field.getAnnotation(XmlElement.class);
		out.append("\""+escapeString(field.getName())+"\":{");
		// Generate object meta data
		if(xmlElemnt != null){
			out.append("\"defaultValue\":");
			string(xmlElemnt.defaultValue(),out);
			out.append(", \"nillable\":\""+xmlElemnt.nillable()+"\"");
			out.append(", \"required\":\""+xmlElemnt.required()+"\"");// TODO difrence betwin nillable and required
			out.append(", \"type\":\""+field.getType().getSimpleName()+"\"");
			out.append(", \"restriction\":{");
				out.append("\"minLength\":0");
				out.append(",");
				out.append("\"maxLength\":255");
				out.append(",\"pattern\":\"\"");
			out.append("}");
			
		}*/
	}
	
	/**
	 * Output responder.
	 */
	public void doResponse(WSHTTPConnection ouStream) throws IOException {
		QName serviceName = this.codec.getEndpoint().getServiceName();
		if(!endPointDocuments.containsKey(serviceName))
			process();
		String portDocuments =  endPointDocuments.get(serviceName);
		if(portDocuments != null){
			doResponse(ouStream, portDocuments);
		}else{
			ouStream.getOutput().write(String.format("Unable to find default document for %s",
					this.codec.getEndpoint().getPortName()).getBytes());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(HttpMetadataProvider o) {
		if(o.equals(this)){
			return 0;
		}else{
			return Integer.MAX_VALUE;
		}
	}
}
