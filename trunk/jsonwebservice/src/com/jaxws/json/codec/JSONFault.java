package com.jaxws.json.codec;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import com.sun.xml.ws.fault.SOAPFaultBuilder;

/**
 * @author ssaminathan
 *
 */
public class JSONFault extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2788345443869004719L;
	
	public static final String INVALID_STRUCTURE = "Client.Invalid.structure";
	/**
	 * fault code 
	 */
	private String code;
	/**
	 * Fault message
	 */
	private String message;
	/**
	 * Fault actor
	 */
	private String actor;
	/**
	 * 
	 */
	private HashMap<String,Object> detail;
	
	
	/**
	 * @param code
	 * @param message
	 * @param actor
	 * @param detail
	 */
	public JSONFault(String code, String message, String actor, HashMap<String,Object> detail) {
		super();
		this.code = code;
		this.message = message;
		this.actor = actor;
		this.detail = detail;
	}
	
	/**
	 * @param code
	 * @param message
	 * @param actor
	 * @param detail
	 */
	public JSONFault(String code, String message, String actor,HashMap<String,Object> detail, Throwable th) {
		super(th);
		if(detail == null){
			detail = new HashMap<String, Object>();
		}
		this.code = code;
		this.message = message;
		this.actor = actor;
		this.detail = detail;
		if(th != null){
			detail.put("message", th.getMessage());
			StringWriter stringWriter = new StringWriter();
			PrintWriter pw = new PrintWriter(stringWriter );
			th.printStackTrace(pw);
			if(th.getCause() != null){
				th.getCause().printStackTrace(pw);
				if(th.getCause() != null){
					th.getCause().printStackTrace(pw);
				}
			}
			pw.close();
			detail.put("printStackTrace", stringWriter.toString());
		}
	}
	
	
	/**
	 * @return
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return
	 */
	public String getActor() {
		return actor;
	}
	/**
	 * @param actor
	 */
	public void setActor(String actor) {
		this.actor = actor;
	}
	/**
	 * @return
	 */
	public HashMap<String,Object> getDetail() {
		if(SOAPFaultBuilder.captureStackTrace)
			return detail;
		else// If stack trace disabled don't send any detail
			return new HashMap<String,Object>();
	}
	/**
	 * @param detail
	 */
	public void setDetail(HashMap<String,Object> detail) {
		this.detail = detail;
	}
	
	public StackTraceElement[] getStackTrace() {
		// DONO't send codec start trace
	    return null;
	}

}
