package com.jaxws.json.codec;

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
	public JSONFault(String code, String message, String actor,HashMap<String,String> detail, Exception e) {
		super(e);
		this.code = code;
		this.message = message;
		this.actor = actor;
		this.detail = detail;
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
