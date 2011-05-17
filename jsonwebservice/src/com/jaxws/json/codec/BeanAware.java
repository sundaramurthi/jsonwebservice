/**
 * 
 */
package com.jaxws.json.codec;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.jaxws.json.feature.JSONObject;

/**
 * @author Sundaramurthi 
 * @since 0.5
 * @version 1.0
 * 
 * Encode and Decoder extends Bean aware. To use common bean properties.
 *  
 */
public abstract class BeanAware {
	private static DatatypeFactory datatypeFactory; 
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
	 * Flag Property of populator which enable create default object on non nullable property. 
	 */
	protected boolean createDefaultOnNonNullable	= JSONCodec.createDefaultOnNonNullable;
	
	/**
     * Utility method 
	 * @param clazz
	 * @return
	 */
	public static boolean isJSONPrimitive(Class<?> clazz) {
		return 		clazz.isPrimitive() || Number.class.isAssignableFrom(clazz)			
				|| clazz.equals(String.class)		|| clazz.equals(Boolean.class)		
				|| clazz.isEnum()					|| isDateTime(clazz)
				|| clazz.equals(Character.class)	|| clazz.equals(Locale.class);
	}
	
	/**
	 * Utility method 
	 * @param clazz
	 * @return
	 */
	protected static boolean isDateTime(Class<?> clazz){
		return Date.class.isAssignableFrom(clazz)//Timestamp.class, java.sql.Date.class
				|| Calendar.class.isAssignableFrom(clazz); // GregorianCalendar, Calendar;
		// XMLGregorianCallender should be responded as bean with year, day , month property.
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
    					: Introspector.getBeanInfo(clazz,clazz.isEnum() ? Enum.class : clazz.equals(Object.class)? null : Object.class)).getPropertyDescriptors();
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
			classFieldCache.put(clazz, fillDeclaredFields(clazz,new HashMap<String, Field>()));
		} 
		return classFieldCache.get(clazz).get(fieldName);
	}
	
	/**
	 * declared fields.
	 */
	private static Map<String, Field> fillDeclaredFields(Class<?> clazz,Map<String, Field> fieldMap){
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
		return fieldMap;
	}
	
	
	/**
	 * @param clazz
	 * @return instanceof class or possible sub level object
	 */
	public Object getNewInstance(Class<?> clazz){
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			if (List.class.isAssignableFrom(clazz)){
                return new ArrayList<Object>();
            }else if(Map.class.isAssignableFrom(clazz)){
				return new HashMap<String,Object>();
			} else if (Set.class.isAssignableFrom(clazz)) {
                return new HashSet<Object>();
            } else if (Integer.class.isAssignableFrom(clazz)) {
                return new Integer(0);
            } else if (Long.class.isAssignableFrom(clazz)) {
                return new Long(0);
            } else if (Double.class.isAssignableFrom(clazz)) {
                return new Double(0.0);
            } else if (Float.class.isAssignableFrom(clazz)) {
                return new Float(0.0);
            } else if (Number.class.isAssignableFrom(clazz)) {
                return 0;
            } else if (Boolean.class.isAssignableFrom(clazz)) {
                return new Boolean(false);
            } else if(XMLGregorianCalendar.class.isAssignableFrom(clazz)){
            	if(datatypeFactory == null){
    				try {
						datatypeFactory = DatatypeFactory.newInstance();
					} catch (DatatypeConfigurationException e1) {
						return null;
					}
    			}
            	return datatypeFactory.newXMLGregorianCalendar();
            }
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @return current populator instance value of createDefaultOnNonNullable 
	 * 
	 * Bean aware property.
	 * By setting value to  true, either populator or writer compose default value for read and 
	 * write for non nullable object with null value passed from client or server. 
	 * 
	 * Default value of createDefaultOnNonNullable assigned from JSONCodec.createDefaultOnNonNullable
	 * @see JSONCodec createDefaultOnNonNullable
	 */
	public boolean isCreateDefaultOnNonNullable() {
		return createDefaultOnNonNullable;
	}


	/**
	 * @param createDefaultOnNonNullable
	 */
	public void setCreateDefaultOnNonNullable(boolean createDefaultOnNonNullable) {
		this.createDefaultOnNonNullable = createDefaultOnNonNullable;
	}
}
