package com.jaxws.json.codec.decode;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.transform.stream.StreamSource;

import org.jvnet.mimepull.MIMEPart;

import com.jaxws.json.codec.BeanAware;
import com.jaxws.json.codec.DateFormat;
import com.jaxws.json.codec.DebugTrace;
import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.PublicFieldPropertyDescriptor;
import com.jaxws.json.feature.JSONWebService;
import com.jaxws.json.serializer.JSONObjectCustomizer;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentDisposition;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimePartDataSource;

/**
 * @author Sundaramurthi Saminathan
 * @version 2.0
 * 
 * WSJSONPopulator create java object from input JSON map. Uses bean inspector and populate all object.
 * 
 * Population customized using annotations.
 * 
 * @see XmlElement
 * @see XmlAttribute
 * @see JSONWebService
 *
 */
public class WSJSONPopulator extends BeanAware {
	/**
	 * Static string value for JSON/Default null. 
	 */
	private static final String NULL 		= "\u0000";
	
	/**
	 * List of property name can be serialized to MAP
	 */
	@SuppressWarnings("unused")
	private Pattern listMapKey;
	
	/**
	 * List of value property names used in MAP 
	 */
	@SuppressWarnings("unused")
	private Pattern listMapValue;
	
	/**
	 * JSON user specified object customizer.
	 */
	Map<Class<? extends Object>,JSONObjectCustomizer> objectCustomizers;
	
	/**
	 * Date format used to deserialize JSON
	 */
	private DateFormat dateFormat = DateFormat.RFC3339;
	
	/**
	 * MIME attachments
	 */
	private List<MIMEPart> 	attachments	= null;

	//private JAXBContext context;
	
	/**
	 * 
	 */
	private boolean traceEnabled = false;
	
	/**
	 * Trace information. 
	 */
	private DebugTrace 			traceLog;

	
	
	/**
	 * @param listMapKey
	 * @param listMapValue
	 * @param dateFormat
	 * @param objectCustomizers
	 */
	public WSJSONPopulator(Pattern listMapKey,Pattern listMapValue,
			DateFormat dateFormat,Map<Class<? extends Object>, 
			JSONObjectCustomizer> objectCustomizers,
			DebugTrace traceLog) {
		this.listMapKey 		= listMapKey;
		this.listMapValue		= listMapValue;
		if(dateFormat != null){
			this.dateFormat		= dateFormat;
		}
		
		if(objectCustomizers != null){
			this.objectCustomizers	= objectCustomizers;
		}else{
			this.objectCustomizers	= new HashMap<Class<? extends Object>, JSONObjectCustomizer>();
		}
		this.traceEnabled 	= traceLog != null;
		this.traceLog		= traceLog;
	}


	/**
	 * Entry method
	 * @param clazz
	 * @param type
	 * @param value
	 * @param customizeInfo
	 * @param method
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws IntrospectionException
	 */
	@SuppressWarnings("unchecked")
	public Object convert(Class<?> clazz, Type type, Object value, 
			JSONWebService customizeInfo, Method method) throws Exception {
		if(this.objectCustomizers.containsKey(clazz)){
			// Case 1: is it handled by Customizer?
			return this.objectCustomizers.get(clazz).decode(value);
		}else if (isJSONPrimitive(clazz))
			// Case 2: primitive class 
            return convertPrimitive(clazz, value, customizeInfo, method);
		else if (Collection.class.isAssignableFrom(clazz))
			// Case 3: Collection deserialization
            return convertToCollection(clazz, type, value, customizeInfo, method);
        else if (clazz.isArray())
        	// Case 4: Array conversion
            return convertToArray(clazz, type, value, customizeInfo, method);
        else if (Map.class.isAssignableFrom(clazz))
        	// Case 5: Map conversion
            return convertToMap(clazz, type, value, customizeInfo, method);
        else if (value instanceof Map) {
        	// Case 6: Object inside object conversion
            Object convertedValue = clazz.newInstance();
            this.populateObject(convertedValue, (Map<String,Object>) value, customizeInfo);
            return convertedValue;
        } else if(clazz.equals(JAXBElement.class)){
			// Case 7: is it JAXBElement bound with object?
        	return convertJAXBElement(clazz, method);
		} else{
			if(traceEnabled){
				traceLog.warn("Input json value can't be handled by custom handler," +
						" and value not a primitive, collection, array, map, or jaxb elemnt. Please send only supported type or consider to write custom serializer");
			}
			return null;
		}
	}

	/**
	 * Case 7: JAXBElement conversion
	 * @param clazz
	 * @param method
	 * @return
	 */
	private JAXBElement<?> convertJAXBElement(Class<?> clazz, Method method){
		if(clazz.getGenericInterfaces().length == 1 
				&& clazz.getGenericInterfaces()[0].equals(Serializable.class)){
			XmlElementRef elmRef = method.getAnnotation(XmlElementRef.class);
			String elementName = null;
			if(elmRef == null || elmRef.name() == null){
				// Not a referred element
				if(method.getName().startsWith("set")){
					String charStart = ""+method.getName().charAt(3);
					elementName = charStart.toLowerCase()+method.getName().substring(4);
				}else{
					elementName = method.getName();
				}
			}else{
				elementName = elmRef !=null ? elmRef.name() : null;
			}
			if(elementName != null){
				throw new RuntimeException("FIXME JAXBElement");
				// FIXME JaxBeanInfo beanInfo = context.getBeanInfo(elementName);
				//return new JAXBElement(beanInfo.getTypeName(elementName), beanInfo.jaxbType, value);
			}
		}
		return null;
	}
	
	/**
	 * Case 6: Object inside object conversion
	 * @param object
	 * @param elements
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IntrospectionException
	 * @throws IllegalArgumentException
	 * @throws JSONException
	 * @throws InstantiationException
	 */
    public void populateObject(Object object, Map<String,Object> elements, JSONWebService customizeInfo, List<MIMEPart> attachments)
        throws Exception {
		this.attachments	= attachments;
		this.populateObject(object, elements, customizeInfo);
	}
	
	/**
	 * Case 6: Object inside object conversion
	 * @param object
	 * @param elements
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IntrospectionException
	 * @throws IllegalArgumentException
	 * @throws JSONException
	 * @throws InstantiationException
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void populateObject(Object object, Map<String,Object> elements, JSONWebService customizeInfo)
        throws Exception {
    	Class<?>				clazz	= object.getClass();
		PropertyDescriptor[] 	props 	= getBeanProperties(clazz);

		//iterate over class fields
		for (PropertyDescriptor prop : props) {
			Method 				writeMethod 		= prop.getWriteMethod();
		    JSONWebService 		writeMethodConfig 	= writeMethod != null ? writeMethod.getAnnotation(JSONWebService.class) : null;
		    String 				expectedJSONPropName= (writeMethodConfig != null && !writeMethodConfig.name().isEmpty()) ? writeMethodConfig.name() : prop.getName();
		    // JOSN input contains specified property.
		    if (elements.containsKey(expectedJSONPropName)) {
		    	Object value 	= elements.get(expectedJSONPropName);
		        if (writeMethod != null) {
		            if (writeMethodConfig != null
		            		&& !writeMethodConfig.deserialize()) {
		            	if(traceEnabled){
		            		traceLog.info(String.format("Ignoring property %s due to deserialize set to false", expectedJSONPropName));
		            	}
		            	continue;
		            }
		            //use only public setters Bean property describer get only accessable setter.
	                // Bean getter always works on single property get. if (paramTypes.length == 1) {
                	try{
                		Class<?>[] 	paramTypes 		= writeMethod.getParameterTypes();
    	                Type[] 		genericTypes 	= writeMethod.getGenericParameterTypes();
    	                
                		Object convertedValue = this.convert(paramTypes[0], genericTypes[0], value, writeMethodConfig, writeMethod);
                		writeMethod.invoke(object, convertedValue);
                	}catch(Throwable exp){
                		if(prop instanceof PublicFieldPropertyDescriptor){
                			((PublicFieldPropertyDescriptor)prop).setValue(object, value);
                    	}
                		if(traceEnabled){
                			traceLog.warn(String.format("Exception while writing property \"%s\". Input %s. Expected type %s",
                					expectedJSONPropName, value, prop.getPropertyType().getSimpleName()));
		            	}
                	}
	               // }
		        } else if (prop.getReadMethod() != null && Collection.class.isAssignableFrom(prop.getPropertyType())) {
					try {
						Method 				readMethod 			= prop.getReadMethod();
						JSONWebService 		readMethodConfig 	= readMethod.getAnnotation(JSONWebService.class);
						if(readMethodConfig == null || readMethodConfig.deserialize()){
							//  add configuration
							Collection<?> objectList = (Collection<?>) readMethod.invoke(object);
							if(objectList != null){
								if(traceEnabled){
									traceLog.info(String.format("Only list read method found for property %s adding new values to existing collection. " +
				            				"Old list size: %d", expectedJSONPropName, objectList.size()));
				            	}
								java.lang.reflect.Field f = getDeclaredField(clazz,prop.getName());
								if(f != null && f.isAnnotationPresent(XmlElements.class)){
									// Field is choice. name1OrName2OrName3. But user passing as "name1OrName2OrName3" single field name. Its invalid.
									// end with hashMap can't converted to xml exception.
									// Ignore this property.
									if(traceEnabled){
					            		traceLog.warn(String.format("Property %s is invalid. %s is a choice list. For more above choice read endpoint document.",
												expectedJSONPropName,expectedJSONPropName));
					            	}
									continue;
								}
								Object convertedValue = this.convert(readMethod.getReturnType(), readMethod.getGenericReturnType(), value, 
										readMethodConfig, readMethod);
								objectList.addAll((Collection) convertedValue);
							}
						}
					} catch (Exception e) {
						if(traceEnabled){
							traceLog.warn(String.format("Failed to add list to existing collection for property %s in class %s. message %s",
									expectedJSONPropName, clazz.getSimpleName(),e.getMessage()));
						}
					}
                } else {
                	if(traceEnabled){
                		traceLog.warn(String.format("Ignoring property %s in class %. message %s",
								expectedJSONPropName, clazz.getSimpleName(),""));
					}
                }
		    } else {
		    	 // JOSN input DON'T contains specified property. May come from customized json name or XMLElement definition.
	    		java.lang.reflect.Field f = getDeclaredField(clazz,prop.getName());
	    		if(f != null){
					if(f.isAnnotationPresent(XmlElements.class)){
						// Handle XSD choice element. 
						XmlElements 	ann 		= f.getAnnotation(XmlElements.class);
						XmlElement[] 	xmlElements = ann.value();
						for(XmlElement elm : xmlElements){
							if(elements.containsKey(elm.name())){
								try{
									Object jsonValue = elements.get(elm.name());
									List<Map<String,Object>> objects = new ArrayList<Map<String,Object>>();
									if(jsonValue instanceof Map){// Single object XSD choice with maxOccur 1
										objects.add((Map<String,Object>)jsonValue);
									}else if(jsonValue instanceof List){// List of object XSD choice with maxOccur greater than 1
										objects.addAll((List)jsonValue);
									}else if(traceEnabled){
										traceLog.warn(String.format("Object choice found. But input JSON is not object or LIST ignoring %s",
												prop.getName()));
									}
									List<Object> populatedObjects = new ArrayList<Object>();
									for(Map v :  objects){
										Object ob = elm.type().newInstance();
										populateObject(ob, v, writeMethodConfig);
										populatedObjects.add(ob);
									}
									Method readMethod = prop.getReadMethod();
									if(writeMethod != null && readMethod != null && readMethod.invoke(object) == null){
										// init list if read method returns null
										writeMethod.invoke(object, new ArrayList<Object>());
									}
									if (readMethod != null){
										Collection objectList = (Collection) readMethod.invoke(object);
										if(objectList != null){
											objectList.addAll(populatedObjects);
										}else{
											if(traceEnabled)
												traceLog.warn("List property dont have write method. Also read method returned null. " +
														"Ignoring json value for list " + prop.getName());
										}
									}
								} catch (Throwable th){
									if(traceEnabled)
										traceLog.warn(String.format("Failed to populate choice element list. property %s in clazz %s",prop.getName(),
												clazz.getSimpleName()));
								}
							}
						}
					} else if(f.isAnnotationPresent(XmlMimeType.class) && writeMethod != null){
						// Process Attachments
						XmlMimeType 	ann 		= f.getAnnotation(XmlMimeType.class);
						writeMethod.invoke(object, new Object[]{handleMimeAttachement(f.getType(),ann,expectedJSONPropName)});
					} else if(f.isAnnotationPresent(XmlElement.class)){
						// Handle default value
						XmlElement 		element 	= f.getAnnotation(XmlElement.class);
						// JSON property name is same as in XML annotation, but class property name is different. 
						if(!element.name().equals(NULL) && elements.containsKey(element.name())){
							writeMethod.invoke(object,
									convert(prop.getPropertyType(), f.getType(), elements.get(element.name()), 
											writeMethod.getAnnotation(JSONWebService.class), writeMethod));
						}else if(!element.defaultValue().equals(NULL)){
							if(traceEnabled)
								traceLog.info(String.format("Input do not have %s. Populating default value: %s",
										expectedJSONPropName, element.defaultValue()));
							writeMethod.invoke(object,
									convert(prop.getPropertyType(), f.getType(), element.defaultValue(), 
											writeMethod.getAnnotation(JSONWebService.class), writeMethod));
						}else if(!element.nillable() && JSONCodec.createDefaultOnNonNullable){
							if(!isJSONPrimitive(prop.getPropertyType())){
								if(traceEnabled)
									traceLog.warn("Non nillable object(\""+expectedJSONPropName +
										"\") with nill value, populating default.");
								try{
									Object ob = prop.getPropertyType().newInstance();
									populateObject(ob, new HashMap<String, Object>(), writeMethodConfig);
									writeMethod.invoke(object,ob);
								}catch(Throwable th){
									if(traceEnabled)
										traceLog.error("Non nillable object(\""+expectedJSONPropName +
											"\") with nill value, failed to create instance.");
								}
							}else{
								// TODO ??
								if(traceEnabled)
									traceLog.error(String.format("Non nillable primitive \"%s\", also don't have default value." +
										" Implementation may may fail to hanlde your request.  ", expectedJSONPropName));
							}
						}
					}
	    		}
            }
		}
    }
	
    /**
     * Case 6: Mime attachment
     * @param ann
     */
    private Object handleMimeAttachement(Class<?> clazz, XmlMimeType ann,String propertyName) {
		if(this.attachments != null){
			for(MIMEPart mimePart : this.attachments){
				List<String> contentDisposition = mimePart.getHeader(JSONCodec.CONTENT_DISPOSITION_HEADER);
				if(contentDisposition != null && contentDisposition.size() > 0){
					try {
						ContentDisposition disp = new ContentDisposition(contentDisposition.get(0));
						if(disp.getParameter("name") != null && disp.getParameter("name").equals(propertyName)){
							if(clazz.isAssignableFrom(DataHandler.class))
								return new DataHandler(new MimePartDataSource(new MimeBodyPart(mimePart.read())));
							else if(clazz.isAssignableFrom(javax.xml.transform.Source.class))
								return new StreamSource(mimePart.read());
						}
					} catch (Exception e) {
						if(this.traceEnabled){
							traceLog.error(String.format("Error while handling attachment name:\"%s\". message: \"%s\"",
									propertyName,e.getMessage()));
						}
					}
				}
			}
		}
		if(this.traceEnabled){
			traceLog.warn(String.format("attachment name:\"%s\" Not found in request.", propertyName));
		}
		return null;
	}


	/**
     * Case 5: Map conversion
     * 
     * @param clazz
     * @param type
     * @param value
     * @param accessor
     * @return
     * @throws JSONException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws IntrospectionException
     */
    @SuppressWarnings("unchecked")
    private Object convertToMap(Class<?> clazz, Type type, Object value, JSONWebService customizeInfo,Method accessor)
            throws Exception {
        if (value == null)
            return null;
        else if (value instanceof Map) {
            Class<?> itemClass = Object.class;
            Type itemType = null;
            if (type != null && type instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType) type;
                itemType = ptype.getActualTypeArguments()[1];
                if (itemType.getClass().equals(Class.class)) {
                    itemClass = (Class<?>) itemType;
                } else {
                    itemClass = (Class<?>) ((ParameterizedType) itemType).getRawType();
                }
            }
            Map<String,Object> values = (Map<String,Object>) value;

            Map<String,Object> newMap = null;
            try {
                newMap = (Map<String,Object>) clazz.newInstance();
            } catch (InstantiationException ex) {
                // fallback if clazz represents an interface or abstract class
                newMap = new HashMap<String,Object>();
            }

            //create an object for each element
            Iterator<Map.Entry<String,Object>> iter = values.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String,Object> 	entry 	= iter.next();
                String 						key 	= entry.getKey();
                Object 						v 		= entry.getValue();

                if (itemClass.equals(Object.class)) {
                    //String, Object
                    newMap.put(key, v);
                } else if (isJSONPrimitive(itemClass)) {
                    //primitive map
                    newMap.put(key, this.convertPrimitive(itemClass, v, customizeInfo,
                            accessor));
                } else if (Map.class.isAssignableFrom(itemClass)) {
                    Object newObject = convertToMap(itemClass, itemType, v, customizeInfo, accessor);
                    newMap.put(key, newObject);
                } else if (List.class.isAssignableFrom(itemClass)) {
                    Object newObject = convertToCollection(itemClass, itemType, v, customizeInfo, accessor);
                    newMap.put(key, newObject);
                } else if (v instanceof Map) {
                    //map of beans
                    Object newObject = itemClass.newInstance();
                    this.populateObject(newObject, (Map<String,Object>) v, null);
                    newMap.put(key, newObject);
                } else if(traceEnabled){
                	traceLog.error(String.format("Incompatible types for property %s in class %s",
                			accessor.getName(),
                			accessor.getDeclaringClass().getSimpleName()));
                }
            }
            return newMap;
        } else if(traceEnabled){
        	traceLog.error(String.format("Incompatible types for property %s in class %s",
        			accessor.getName(),
        			accessor.getDeclaringClass().getSimpleName()));
        }
        return null;
    }

    /**
     * Case 4: Array conversion
     * @param clazz
     * @param type
     * @param value
     * @param Method
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object convertToArray(Class<?> clazz, Type type, Object value,
    		JSONWebService customizeInfo, Method accessor) throws Exception {
        if (value == null)
            return null;
        else if (value instanceof List) {
            Class<?> arrayType = clazz.getComponentType();
            List values = (List) value;
            Object newArray = Array.newInstance(arrayType, values.size());

            //create an object for each element
            for (int j = 0; j < values.size(); j++) {
                Object listValue = values.get(j);

                if (arrayType.equals(Object.class)) {
                    //Object[]
                    Array.set(newArray, j, listValue);
                } else if (isJSONPrimitive(arrayType)) {
                    //primitive array
                    Array.set(newArray, j, this.convertPrimitive(arrayType, listValue, customizeInfo, 
                            accessor));
                } else if (listValue instanceof Map) {
                    //array of other class
                    Object newObject = null;
                    if (Map.class.isAssignableFrom(arrayType)) {
                        newObject = convertToMap(arrayType, type, listValue, customizeInfo, accessor);
                    } else if (List.class.isAssignableFrom(arrayType)) {
                        newObject = convertToCollection(arrayType, type, listValue, customizeInfo, accessor);
                    } else {
                        newObject = arrayType.newInstance();
                        this.populateObject(newObject, (Map<String,Object>) listValue, customizeInfo);
                    }

                    Array.set(newArray, j, newObject);
                } else if(traceEnabled){
                	traceLog.error(String.format("Incompatible types for property %s in class %s",
                			accessor.getName(),
                			accessor.getDeclaringClass().getSimpleName()));
                }
            }

            return newArray;
        } else if(traceEnabled){
        	traceLog.error(String.format("Incompatible types for property %s in class %s",
        			accessor.getName(),
        			accessor.getDeclaringClass().getSimpleName()));
        }
        return null;
    }
    /**
     * Deserializer Case 3: collection class 
     * @param clazz
     * @param type
     * @param value
     * @param accessor
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws IntrospectionException
     */
    @SuppressWarnings("unchecked")
    private Object convertToCollection(Class<?> clazz, Type type, Object value,
    		JSONWebService customizeInfo, Method accessor) throws Exception {
        if (value == null)
            return null;
        
        Class<?> itemClass = Object.class;
        Type itemType = null;
        if (type != null && type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            itemType = ptype.getActualTypeArguments()[0];
            if (itemType.getClass().equals(Class.class)) {
                itemClass = (Class<?>) itemType;
            } else {
                itemClass = (Class<?>) ((ParameterizedType) itemType).getRawType();
            }
        }
        
        if (Collection.class.isAssignableFrom(value.getClass())) {
            Collection<?> values = (Collection<?>) value;
			@SuppressWarnings("rawtypes")
			Collection newCollection = null;
            try {
                newCollection = (Collection<?>) clazz.newInstance();
            } catch (InstantiationException ex) {
                // fallback if clazz represents an interface or abstract class
                if (Set.class.isAssignableFrom(clazz)) {
                    newCollection = new HashSet<Object>();
                } else {
                    newCollection = new ArrayList<Object>(values.size());
                }
            }
            //create an object for each element
            if (itemClass.equals(Object.class)) {
                //Object[]
            	newCollection.addAll(values);
                if(traceEnabled)
                	traceLog.warn(String.format("Unparameterazed list with object type found. accessor: \"%s\" class: %s",
                			accessor.getName(),accessor.getDeclaringClass()));
            } else if (isJSONPrimitive(itemClass)) {
                //primitive array
            	for (Object listValue : values) {
                    newCollection.add(this.convertPrimitive(itemClass, listValue,
                    		customizeInfo, accessor));
            	}
            } else if (Map.class.isAssignableFrom(itemClass)) {
            	for (Object listValue : values) {
            		newCollection.add(convertToMap(itemClass, itemType, listValue, customizeInfo, accessor));
            	}
            } else if (List.class.isAssignableFrom(itemClass)) {
            	// List of list
            	for (Object listValue : values) {
            		newCollection.add(convertToCollection(itemClass, itemType, listValue, customizeInfo, accessor));
            	}
            } else {
            	for (Object listValue : values) {
            		if(listValue instanceof Map){
	            		Object newObject = itemClass.newInstance();
	                    this.populateObject(newObject, (Map<String,Object>) listValue, customizeInfo);
	                    newCollection.add(newObject);
            		}
            	}
            }
            return newCollection;
        } else if (value instanceof Map) {
        	if(traceEnabled){
        		traceLog.info(String.format("Expecting array, but found MAP. for property %s in class %s. Using values as List",
	        			accessor.getName(),
	        			accessor.getDeclaringClass().getSimpleName()));
        	}
        	/*if(listMapKey != null && !itemClass.equals(Object.class)){
        		if(listMapValue != null){
        			// TODO this is logical only value is primitive. Is it required to do this conversion?
        		}
        	}*/
        	return convertToCollection(clazz, type, 
        			((Map<String,Object>)value).values(), customizeInfo, accessor);
        } else{
        	if(traceEnabled){
        		traceLog.error(String.format("Incompatible types for property %s in class %s",
	        			accessor.getName(),
	        			accessor.getDeclaringClass().getSimpleName()));
        	}
        }
        return null;
    }
    
    /**
     * Converts numbers to the desired class, if possible
     *
     * Deserializer Case 2: primitive class 
     * @param method 
     */
    @SuppressWarnings({"unchecked","rawtypes"})
	private Object convertPrimitive(final Class clazz,final  Object value,final JSONWebService customizeInfo,final Method method) {
        if (value == null) {
            if (Short.TYPE.equals(clazz) || Short.class.equals(clazz))
                return (short) 0;
            else if (Byte.TYPE.equals(clazz) || Byte.class.equals(clazz))
                return (byte) 0;
            else if (Integer.TYPE.equals(clazz) || Integer.class.equals(clazz))
                return 0;
            else if (Long.TYPE.equals(clazz) || Long.class.equals(clazz))
                return 0L;
            else if (Float.TYPE.equals(clazz) || Float.class.equals(clazz))
                return 0f;
            else if (Double.TYPE.equals(clazz) || Double.class.equals(clazz))
                return 0d;
            else if (Boolean.TYPE.equals(clazz) || Boolean.class.equals(clazz))
                return Boolean.FALSE;
            else
                return null;
        } else if (value instanceof Number) {
            Number number = (Number) value;

            if (Short.TYPE.equals(clazz))
                return number.shortValue();
            else if (Integer.TYPE.equals(clazz)) // Periority 1
                return number.intValue();
            else if (Integer.class.equals(clazz))
                return new Integer(number.intValue());
            else if (Double.TYPE.equals(clazz)) // Periority 2
                return number.doubleValue();
            else if (Double.class.equals(clazz))
                return new Double(number.doubleValue());
            else if (Long.TYPE.equals(clazz))
                return number.longValue();
            else if (Long.class.equals(clazz))
                return new Long(number.longValue());
            else if (String.class.equals(clazz))
                return value.toString();
            else if(isDateTime(clazz))
            	return handleAsDate(clazz,value,customizeInfo,method);
            else if (Short.class.equals(clazz))
                return new Short(number.shortValue());
            else if (Byte.TYPE.equals(clazz))
                return number.byteValue();
            else if (Byte.class.equals(clazz))
                return new Byte(number.byteValue());
            else if (Float.TYPE.equals(clazz))
                return number.floatValue();
            else if (Float.class.equals(clazz))
                return new Float(number.floatValue());
            else if (BigDecimal.class.equals(clazz))
                return new BigDecimal(number.doubleValue());
            else if (BigInteger.class.equals(clazz))
                return new BigInteger(number.toString());
           // else
            // TODO log warn
            	
        } else if (isDateTime(clazz)) {
        	return handleAsDate(clazz,value,customizeInfo,method);
        } else if (clazz.isEnum()) {
            String sValue = (value instanceof Map) ? ((String)((Map)value).get("_name")) : (String) value;
            return Enum.valueOf(clazz, sValue);
        } else if (value instanceof String) {
            String sValue = (String) value;
            if(sValue.trim().isEmpty() && Number.class.isAssignableFrom(clazz)){
            	sValue = "0";
            	if(traceEnabled)traceLog.warn("Empty string passed for number. Converting it to zero. Field : " + method.getName());
            }
            if (String.class.equals(clazz))
            	return value;
            else if (Boolean.TYPE.equals(clazz))
                return Boolean.parseBoolean(sValue);
            else if (Boolean.class.equals(clazz))
                return Boolean.valueOf(sValue);
            else if (Short.TYPE.equals(clazz) 	|| Short.class.equals(clazz))
                return Short.decode(sValue);
            else if (Byte.TYPE.equals(clazz) 	|| Byte.class.equals(clazz))
                return Byte.decode(sValue);
            else if (Integer.TYPE.equals(clazz) || Integer.class.equals(clazz))
                return Integer.decode(sValue);
            else if (Long.TYPE.equals(clazz)	|| Long.class.equals(clazz))
                return Long.decode(sValue);
            else if (Float.TYPE.equals(clazz))
                return Float.parseFloat(sValue);
            else if (Float.class.equals(clazz))
                return Float.valueOf(sValue);
            else if (Double.TYPE.equals(clazz))
                return Double.parseDouble(sValue);
            else if (Double.class.equals(clazz))
                return Double.valueOf(sValue);
            else if (BigDecimal.class.equals(clazz))
                return new BigDecimal(sValue);
            else if (BigInteger.class.equals(clazz))
                return new BigInteger(sValue);
            else if (Character.TYPE.equals(clazz) || Character.class.equals(clazz)) {
                char charValue = 0;
                if (sValue.length() > 0) {
                    charValue = sValue.charAt(0);
                }
                if (Character.TYPE.equals(clazz))
                    return charValue;
                else
                    return new Character(charValue);
            } else if (clazz.equals(Locale.class)) {
                String[] components = sValue.split("_", 2);
                if (components.length == 2) {
                    return new Locale(components[0], components[1]);
                } else {
                    return new Locale(sValue);
                }
            } else if (Enum.class.isAssignableFrom(clazz)) {
                return Enum.valueOf(clazz, sValue);
            }
        } else if(clazz != null && !clazz.isPrimitive() /*Boolean reaches here*/ && !clazz.isAssignableFrom(value.getClass())){
        	// TODO log. Value not assignable from the value. The ignore value. TODO add trace log 
        	return null;
        }
        return value;
    }
    
    /**
     * Utility methof to handle date 
     * @param clazz
     * @param value
     * @param customizeInfo
     * @param method
     * @return
     */
    private Object handleAsDate(final Class<?> clazz,final  Object dateValue,final JSONWebService customizeInfo,final Method method){
    	Date date = null;
    	String dateStr = dateValue != null ? String.valueOf(dateValue) : null;
    	if(this.dateFormat != DateFormat.PLAIN){
    		String timePattern = (customizeInfo != null && customizeInfo.format().length() > 0) ?
            		customizeInfo.format() : this.dateFormat.getFormat();
    		// ISO, RFC3339 and CUSTOM format handled here. 
            if(timePattern == null || timePattern.trim().isEmpty() ){
            	// ISO format
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
    	        	if(dateStr.length() > 19 && (dateStr.charAt(19) == '+' || dateStr.charAt(19) == '-')){
    	        		timePattern = "yyyy-MM-dd'T'HH:mm:ssZ";// time
    	        	}else{
    	        		timePattern = "yyyy-MM-dd'T'HH:mm:ssz";
    	        	}
    	        } else {
    	            timePattern = "yyyy-MM-ddHH:mm:ssz";
    	        }
    	        if(timePattern.endsWith("Z") && dateStr.length() > 22 && dateStr.charAt(22) == ':'){
    	        	// time zone input follows ":" pattern
    	        	StringBuffer buf = new StringBuffer(dateStr);
    	        	dateStr = buf.replace(22, 23, "").toString();
    	        }
            }
            // Format the current time.
            SimpleDateFormat formatter = new SimpleDateFormat(timePattern);

            Date d = null;
            try {
                d = formatter.parse(dateStr, new ParsePosition(0));
            } catch (Exception e) {
            	if(this.traceEnabled)
            		traceLog.warn(
            				String.format("Failed to parse date string \"%s\" using pattern \"%s\". Cause: %s",dateStr,timePattern,e.getMessage()));
            }
            return d;
    	}else{
    		try{
    			date = new Date(new Long(dateStr));
    		}catch(Exception  e){
    			if(traceEnabled)traceLog.warn( 
    					"PLAIN date format specifed. But input date string is not number.(time in milliseconds): " + method.getName());
    		}
    	}
    	if(date == null){
    		return null;
    	}else if(clazz.equals(Timestamp.class)){
    		return new Timestamp(date.getTime());
    	}else if(clazz.equals(Calendar.class)){
    		Calendar cal = Calendar.getInstance();
    		cal.setTimeInMillis(date.getTime());
    		return cal;
    	}else if(clazz.equals(java.sql.Date.class)){
    		return new java.sql.Date(date.getTime());
    	}else{
    		return date;
    	}
    }
}
