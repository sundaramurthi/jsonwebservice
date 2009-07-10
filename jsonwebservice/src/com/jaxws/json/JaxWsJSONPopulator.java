package com.jaxws.json;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementRef;

import com.googlecode.jsonplugin.JSONException;
import com.googlecode.jsonplugin.JSONPopulator;
import com.jaxws.json.codec.JSONRequestBodyBuilder;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;

public class JaxWsJSONPopulator extends JSONPopulator {
	
	private JAXBContextImpl context;

	private boolean skipListWrapper = false;
	private DateFormat dateFormat;

	private Pattern listMapKey;
	private Pattern listMapValue;
	
	public JaxWsJSONPopulator(boolean skipListWrapper,Pattern listMapKey,Pattern listMapValue,DateFormat dateFormat) {
		super();
		this.skipListWrapper 	= skipListWrapper;
		this.listMapKey 		= listMapKey;
		this.listMapValue		= listMapValue;
		this.dateFormat			= dateFormat;
	}

	public JaxWsJSONPopulator(String dateFormat,boolean skipListWrapper,Pattern listMapKey,Pattern listMapValue,DateFormat dateFormatType) {
		super(dateFormat);
		this.skipListWrapper = skipListWrapper;
		this.listMapKey 		= listMapKey;
		this.listMapValue		= listMapValue;
		this.dateFormat			= dateFormatType;
	}
	
	public JaxWsJSONPopulator(JAXBContextImpl context,boolean skipListWrapper,Pattern listMapKey,Pattern listMapValue,DateFormat dateFormat) {
		super();
		this.context = context;
		this.skipListWrapper = skipListWrapper;
		this.listMapKey 		= listMapKey;
		this.listMapValue		= listMapValue;
		this.dateFormat			= dateFormat;
	}
	

	public static boolean isJSONPrimitive(Class<?> clazz) {
		return clazz.isPrimitive() || clazz.equals(String.class)
				|| clazz.equals(Timestamp.class) 
				|| clazz.equals(Date.class) || clazz.equals(Boolean.class)
				|| clazz.equals(Byte.class) || clazz.equals(Character.class)
				|| clazz.equals(Double.class) || clazz.equals(Float.class)
				|| clazz.equals(Integer.class) || clazz.equals(Long.class)
				|| clazz.equals(Short.class) || clazz.equals(Locale.class)
				|| clazz.isEnum();
	}

	@SuppressWarnings("unchecked")
	public Object convert(Class clazz, Type type, Object value, Method method) throws IllegalArgumentException, JSONException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IntrospectionException {
		if(clazz.equals(JAXBElement.class)){
			if(clazz.getGenericInterfaces().length == 1 && clazz.getGenericInterfaces()[0].equals(Serializable.class)){
				XmlElementRef elmRef = method.getAnnotation(XmlElementRef.class);
				String elementName = null;
				if(elmRef ==null || elmRef.name() == null){
					if(method.getName().startsWith("set")){
						String charStart = ""+method.getName().charAt(3);
						elementName = charStart.toLowerCase()+method.getName().substring(4);
					}else{
						elementName = method.getName();
					}
				}else{
					elementName = elmRef !=null ? elmRef.name():null;
				}
				if(elementName != null){
					JaxBeanInfo beanInfo = context.getBeanInfo(elementName);
					return new JAXBElement(beanInfo.getTypeName(elementName),beanInfo.jaxbType,value);
				}
			}
		}else if (value !=null && (clazz.equals(Date.class) || clazz.equals(Timestamp.class))){
				if(dateFormat == DateFormat.ISO){
					Date dateObj = ISO2Date(value.toString());
					if(dateObj == null)
						return null;
					return new Timestamp(dateObj.getTime());
				}else if(dateFormat == DateFormat.PLAIN){
					return new Timestamp(new Long(value.toString()));
				}
		}
		if(value != null && value.equals("") && isJSONPrimitive(clazz)){
			value = null; // Bug with number conversion
		}
		if(skipListWrapper && value instanceof List ){
			String name = JSONRequestBodyBuilder.getWarpedListName(clazz);
			if(name != null){
				HashMap map = new HashMap();
				map.put(name, value);
				value = map;
			}
		}
		return super.convert(clazz, type, value, method);
	}

	@SuppressWarnings("unchecked")
    public void populateObject(Object object, Map elements)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
        IntrospectionException, IllegalArgumentException, JSONException,
        InstantiationException {
		// unmap
		if(listMapKey != null ){
	        Method[] methods1 = object.getClass().getDeclaredMethods();
	        for(Method meth1: methods1){
	        	if(meth1.getReturnType() == List.class){
	        		String charStart = ""+meth1.getName().charAt(3);
					String key = charStart.toLowerCase()+meth1.getName().substring(4);
		        	Type ob = ((java.lang.reflect.ParameterizedType)meth1.getGenericReturnType()).getActualTypeArguments()[0];
		        	Method[]  methods =  ((Class)ob).getMethods();
		        	 for(Method meth: methods){
		        		String charStartOb = ""+meth.getName().charAt(3);
						String keyObj = charStartOb.toLowerCase()+meth.getName().substring(4);
			        	if(listMapKey.matcher(meth.getName().replaceFirst("get","")).matches()){
			        		Map list = new HashMap();
			        		if(meth.getName().startsWith("get")){
			        			List lis = new ArrayList();
			        			if(skipListWrapper){// Map objects warper skiped
				        			for(Object keyMap :elements.keySet()){
				        				if(listMapValue !=null){
				        					Map<String, Object> prop = new HashMap<String, Object>();
				        					
				        					prop.put(keyObj, keyMap);// FIXME proper property
				        					lis.add(prop);
				        				}else{
				        					lis.add(elements.get(keyMap));
				        				}
				        			}
									list.put(key, lis);
			        			}else{
			        				Map warpedMap = (Map)elements.get(key);
			        				for(Object keyMap :warpedMap.keySet()){
			        					if(listMapValue !=null){
			        						Map<String, Object> prop = new HashMap<String, Object>();
			        						prop.put(keyObj, keyMap);// FIXME proper property
			        						lis.add(prop);
				        				}else{
				        					lis.add(warpedMap.get(keyMap));
				        				}
				        			}
									list.put(key, lis);
			        			}
								elements = list;
								break;
							}
			        	}
		        	 }
	        	}
	        }
        } 
		// demap
		
		try{
			super.populateObject(object, elements);
		}catch(Throwable th){
			th.printStackTrace();
		}
        Class clazz = object.getClass();

        BeanInfo info = Introspector.getBeanInfo(clazz);
        PropertyDescriptor[] props = info.getPropertyDescriptors();

        //iterate over class fields
        for (int i = 0; i < props.length; ++i) {
            PropertyDescriptor prop = props[i];
            String name = prop.getName();

            if (elements.containsKey(name)) {
                Object value = elements.get(name);
                Method method = prop.getWriteMethod();

                if (method == null && prop.getReadMethod() != null && Collection.class.isAssignableFrom(prop.getPropertyType())){
					try {
						Method readMethod = prop.getReadMethod();
						Collection objectList = (Collection) readMethod.invoke(object, new Object[] {});
						if(objectList !=null){
							Object convertedValue = this.convert(readMethod.getReturnType(), readMethod.getGenericReturnType(), value, readMethod);
							objectList.addAll((Collection) convertedValue);
						}
					} catch (Exception e) {
						// IGNORE it
						e.printStackTrace();
					}
                }
            }
        }
    }
	
	/**
     * Convert ISO formated dates to Java Date
     *
     *
     * @param dateStr
     *
     * @return
     *
     * @see
     */
    static public Date ISO2Date(String dateStr) {

        String timePattern = "";
        // select the time pattern to use:
        if (dateStr.length() == 10) {
            timePattern = "yyyy-MM-dd";
        } else if (dateStr.length() == 15) {
            timePattern = "yyyy-MM-ddHH:mm";
        } else if (dateStr.length() == 16) {
            timePattern = "yyyy-MM-dd'T'HH:mm";
        } else if (dateStr.length() == 18) {
            timePattern = "yyyy-MM-ddHH:mm:ss";
        } else if (dateStr.length() == 19) {
            timePattern = "yyyy-MM-dd'T'HH:mm:ss";
        } else if (dateStr.length() > 10 && dateStr.charAt(10) == 'T') {
            timePattern = "yyyy-MM-dd'T'HH:mm:ssz";
        } else {
            timePattern = "yyyy-MM-ddHH:mm:ssz";
        }
        // Format the current time.
        SimpleDateFormat formatter = new SimpleDateFormat(timePattern);

        Date d = null;
        try {
            d = formatter.parse(dateStr, new ParsePosition(0));
        } catch (NullPointerException e) {
          //  LOG.error("constructor failed for" + dateStr);
        }
        return d;
    }
}
