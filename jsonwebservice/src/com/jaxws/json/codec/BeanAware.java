/**
 * 
 */
package com.jaxws.json.codec;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import com.jaxws.json.feature.JSONObject;

/**
 * @author Sundaramurthi 
 * 
 * Encode and Decoder extends Bean aware. To use common bean properties.
 *  
 */
public abstract class BeanAware {
	
	/**
	 * Private bean property cache.
	 * Standard bean inspector cache only for with out Hierarchy, When Hierarchy specify bean parser always parse bean. Its too slow.
	 */
	private static final Map<Class<?>,PropertyDescriptor[]> propertyDescriptorCache = 
    	Collections.synchronizedMap(new WeakHashMap<Class<?>,PropertyDescriptor[]>());
	
	/**
	 * Field name cache
	 */
	private static final Map<Class<?>,Map<String,java.lang.reflect.Field>> classFieldCache = 
    	Collections.synchronizedMap(new WeakHashMap<Class<?>,Map<String,java.lang.reflect.Field>>());
	
	/**
     * Utility method 
	 * @param clazz
	 * @return
	 */
	public static boolean isJSONPrimitive(Class<?> clazz) {
		return 		clazz.isPrimitive() 			
				|| clazz.equals(String.class)		|| clazz.equals(Locale.class)
				|| clazz.equals(Boolean.class)		|| clazz.isEnum()
				|| clazz.equals(Byte.class) 		|| clazz.equals(Character.class)
				|| clazz.equals(Double.class) 		|| clazz.equals(Float.class)
				|| clazz.equals(Integer.class) 		|| clazz.equals(Long.class)
				|| clazz.equals(Short.class) 		|| clazz.equals(BigDecimal.class) 
				|| clazz.equals(BigInteger.class) 	|| isDateTime(clazz);
	}
	
	/**
	 * Utility method 
	 * @param clazz
	 * @return
	 */
	protected static boolean isDateTime(Class<?> clazz){
		return clazz.equals(Timestamp.class) 
				|| clazz.equals(Calendar.class)
				|| clazz.equals(Date.class)
				|| clazz.equals(java.sql.Date.class);
	}
	/**
 	 * Utility method to return bean property information.
 	 * @param clazz
 	 * @return
 	 * @throws IntrospectionException
 	 */
	protected static PropertyDescriptor[] getBeanProperties(Class<?> clazz) throws IntrospectionException{
 		if(propertyDescriptorCache.containsKey(clazz))
 			return propertyDescriptorCache.get(clazz);
		PropertyDescriptor[] props =  ((clazz.isAnnotationPresent(JSONObject.class) && 
    			clazz.getAnnotation(JSONObject.class).ignoreHierarchy()) 
    			? Introspector.getBeanInfo(clazz, clazz.getSuperclass()) 
    					: Introspector.getBeanInfo(clazz,clazz.isEnum() ? Enum.class : Object.class)).getPropertyDescriptors();
 		if(props.length == 0 && !clazz.isEnum()){
        	// There is no property descriptor, then use public fields, RPC document require this
        	props	= PublicFieldPropertyDescriptor.getDiscriptors(clazz.getFields(), clazz);
        }
 		propertyDescriptorCache.put(clazz, props);
 		return props;
 	}
 	
 	/**
	 * Utility method to read declaring field including private scope.
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	protected static java.lang.reflect.Field getDeclaredField(Class<?> clazz, String fieldName){
		if(!classFieldCache.containsKey(clazz)){
			classFieldCache.put(clazz, getAllFields(clazz));
		} 
		return classFieldCache.get(clazz).get(fieldName);
	}
	
	private static Map<String, Field> getAllFields(Class<?> clazz) {
		Map<String, Field> fieldMap = new HashMap<String, Field>();
		fillDeclaredFields(clazz,fieldMap);
		return fieldMap;
	}
	
	private static void fillDeclaredFields(Class<?> clazz,Map<String, Field> fieldMap){
		try {
			for(java.lang.reflect.Field field : clazz.getDeclaredFields()){
				if(!fieldMap.containsKey(field.getName()))
					fieldMap.put(field.getName(), field);
			}
			if(!Object.class.equals(clazz.getSuperclass())){
				fillDeclaredFields(clazz.getSuperclass(),fieldMap);
			}
		} catch (Throwable e) {
			//
		}
	}
}
