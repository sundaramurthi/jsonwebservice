package com.jaxws.json.codec;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Sundaramurthi Saminathan
 *
 */
public class PublicFieldPropertyDescriptor extends PropertyDescriptor {

	private Field field;

	public PublicFieldPropertyDescriptor(Field field,
			Class<?> beanClass)
			throws IntrospectionException {
		super(field.getName(), beanClass);
		this.field = field;
	}
	
	public void setValue(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException{
		field.set(obj, value);
	}
	
	public Object getValue(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return field.get(obj);
	}
	
	public static PublicFieldPropertyDescriptor[] getDiscriptors(Field fields[],Class<?> beanClass){
		ArrayList<PublicFieldPropertyDescriptor> list = new ArrayList<PublicFieldPropertyDescriptor>();
		for(Field field : fields){
			if(field.isAnnotationPresent(XmlTransient.class))
				continue;
			try {
				list.add(new PublicFieldPropertyDescriptor(field,beanClass));
			} catch (IntrospectionException e) {}
		}
		return list.toArray(new PublicFieldPropertyDescriptor[list.size()]);
	}

	@Override
	public synchronized Class<?> getPropertyType() {
		return field.getType();
	}
	
	private static Method read, write;
	static{
		try{
			read = PublicFieldPropertyDescriptor.class.getDeclaredMethod("getValue", Object.class);
			write = PublicFieldPropertyDescriptor.class.getDeclaredMethod("setValue", Object.class,Object.class);
		}catch(Throwable th){}
	}
	 
	
	@Override
	public synchronized Method getReadMethod() {
		return read;
	}

	@Override
	public synchronized Method getWriteMethod() {
		return write;
	}
}
