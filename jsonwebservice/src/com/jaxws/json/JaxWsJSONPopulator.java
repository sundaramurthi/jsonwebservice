package com.jaxws.json;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementRef;

import com.googlecode.jsonplugin.JSONException;
import com.googlecode.jsonplugin.JSONPopulator;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;

public class JaxWsJSONPopulator extends JSONPopulator {
	
	private JAXBContextImpl context;

	public JaxWsJSONPopulator() {
		super();
	}

	public JaxWsJSONPopulator(String dateFormat) {
		super(dateFormat);
	}
	
	public JaxWsJSONPopulator(JAXBContextImpl context) {
		super();
		this.context = context;
	}
	

	public static boolean isJSONPrimitive(Class<?> clazz) {
		return clazz.isPrimitive() || clazz.equals(String.class)
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
		}
		if(value != null && value.equals("") && isJSONPrimitive(clazz)){
			value = null; // Bug with number conversion
		}
		return super.convert(clazz, type, value, method);
	}

	@SuppressWarnings("unchecked")
    public void populateObject(Object object, final Map elements)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
        IntrospectionException, IllegalArgumentException, JSONException,
        InstantiationException {
		super.populateObject(object, elements);
		
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
}
