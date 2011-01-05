package com.jaxws.json.codec;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

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
	private HashMap<String,String> detail;
	
	
	/**
	 * @param code
	 * @param message
	 * @param actor
	 * @param detail
	 */
	public JSONFault(String code, String message, String actor, HashMap<String,String> detail) {
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
	public JSONFault(String code, String message, String actor,HashMap<String,String> detail, Throwable th) {
		super(th);
		if(detail == null){
			detail = new HashMap<String, String>();
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
	public HashMap<String,String> getDetail() {
		return detail;
	}
	/**
	 * @param detail
	 */
	public void setDetail(HashMap<String,String> detail) {
		this.detail = detail;
	}
}
