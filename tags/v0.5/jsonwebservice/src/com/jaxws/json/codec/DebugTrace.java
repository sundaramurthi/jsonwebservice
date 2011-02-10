package com.jaxws.json.codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author ssaminathan
 *
 */
public class DebugTrace extends HashMap<String, List<String>>{
	public static final String INFO		= "INFO";
	public static final String WARN		= "WARN";
	public static final String ERROR	= "ERROR";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param info
	 */
	public void info(String info){
		List<String> existing = super.get(INFO);
		if(existing == null){
			super.put(INFO, existing = new ArrayList<String>());
			
		}
		existing.add(info);
	}
	
	/**
	 * @param warn
	 */
	public void warn(String warn){
		List<String> existing = super.get(WARN);
		if(existing == null){
			super.put(WARN, existing = new ArrayList<String>());
			
		}
		existing.add(warn);
	}
	
	/**
	 * @param warn
	 */
	public void error(String error){
		List<String> existing = super.get(ERROR);
		if(existing == null){
			super.put(ERROR, existing = new ArrayList<String>());
		}
		existing.add(error);
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public void add(String key,String value){
		List<String> existing = (List<String>)get(key);
		if(existing == null){
			super.put(key, existing = new ArrayList<String>());
		}
		existing.add(value);
	}
}
