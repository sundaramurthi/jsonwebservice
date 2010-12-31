package com.jaxws.json.codec.encode;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.jaxws.json.codec.DateFormat;
import com.jaxws.json.codec.JSONCodec;
import com.jaxws.json.codec.JSONFault;
import com.jaxws.json.codec.PublicFieldPropertyDescriptor;
import com.jaxws.json.codec.decode.WSJSONPopulator;
import com.jaxws.json.feature.JSONObject;
import com.jaxws.json.feature.JSONWebService;
import com.jaxws.json.serializer.JSONObjectCustomizer;

/**
 * @author ssaminathan
 *
 */
public class WSJSONWriter {
    private static final Logger LOG = Logger.getLogger(WSJSONWriter.class.getName());

	private static final String XML_DEFAULT = "##default";
	private static final String NULL 		= "\u0000";
	
	/**
	 * HEX character list for unicode generation.
	 */
	final static char[] 	hex 			= "0123456789ABCDEF".toCharArray();
    
    /**
     * JSON output stream. Initialized while object construction.
     * 
     */
    private final 	OutputStream		output;
    
    /**
     * rootObject 
     */
    private final 	Object 			rootObject;
    
    /**
     * Cyclic finder Object stack
     */
    private final 	Stack<Object> 	stack;
    
    /**
     * List of user registered JSON customizer.
     */
    private final 	Map<Class<? extends Object>,JSONObjectCustomizer> objectCustomizers;
    
    /**
	 * Date format used to serialize JSON
	 * 
	 * User may customize this property using JSON web service configuration property file. 
	 */
	private DateFormat dateFormat = DateFormat.RFC3339;
	
	/**
	 * Either include or exclude property specified this expression flag turn true. 
	 * As a result JSON path expression constructed to handle include and/or exclude
	 * 
	 * If flag goes true there is miner performance delay may happen
	 */
	private 		boolean 		buildExpr 				= false;
	
	
	/**
	 * Currently processing JSON expression stack. Used to match include and/or exclude configuration. 
	 */
	private 		String 			exprStack 				= "";

	/**
	 * List of properties that excluded from serialization
	 */
	private Collection<Pattern> excludeProperties;

	/**
	 * List of properties that included from serialization
	 */
	private Collection<Pattern> includeProperties;

	/**
	 * 
	 */
	private Pattern listMapKey;

	/**
	 * 
	 */
	private Pattern listMapValue;
	
	/**
	 * Flag which enable write object as possible JSON document format or not.
	 */
	private boolean metaDataMode	= false;

	/**
	 * List of response attachments.
	 */
	private List<Map<String,Object>> attachments	 = new ArrayList<Map<String,Object>>();
    
    /**
     * Writer instance with parameter passed writer object.
     * @param writer
     */
    public WSJSONWriter(OutputStream output, Object rootObject, Map<Class<? extends Object>, JSONObjectCustomizer> objectCustomizers){
    	if(output == null){
    		throw new RuntimeException("Writer can't be null");
    	}
    	if(rootObject == null){
    		throw new RuntimeException("rootObject can't be null");
    	}
		this.output		= output;
		this.rootObject = rootObject;
		this.stack	= new Stack<Object>();
		this.objectCustomizers = objectCustomizers != null ? 
				objectCustomizers : 
				new HashMap<Class<? extends Object>, JSONObjectCustomizer>();
	}
    
    /**
     * Serialize passed object to JSON string, writes into constructor passed writer object.
     * @param object Map object to serialize.
     */
    public void write(DateFormat dateFormat,
    		Collection<Pattern> excludeProperties, Collection<Pattern> includeProperties,
    		Pattern listMapKey,Pattern listMapValue){
    	this.initValues(dateFormat, excludeProperties, includeProperties, listMapKey, listMapValue);
    	// Convert passed object to value.
    	this.process(rootObject, null);
    }
    /**
     * Serialize passed object to JSON string, writes into constructor passed writer object. 
     * For null properties new instance created and serialized as meta data.
     * @param object Map object to serialize.
     */
    public void writeMetadata(DateFormat dateFormat,
    		Collection<Pattern> excludeProperties, Collection<Pattern> includeProperties,
    		Pattern listMapKey,Pattern listMapValue){
    	this.initValues(dateFormat, excludeProperties, includeProperties, listMapKey, listMapValue);
    	this.metaDataMode	= true;
    	// Convert passed object to value.
    	this.process(rootObject, null);
    }
    
    /**
     * Utility method to serialize object.
     * @param rootObject
     * @param objectCustomizers
     * @return
     */
    public static final String writeMetadata(Object rootObject,
    		Map<Class<? extends Object>, JSONObjectCustomizer> objectCustomizers){
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	WSJSONWriter	writter = new WSJSONWriter(out, rootObject, objectCustomizers);
    	writter.writeMetadata(JSONCodec.dateFormat, JSONCodec.excludeProperties, 
    			JSONCodec.includeProperties, JSONCodec.globalMapKeyPattern, 
    			JSONCodec.globalMapValuePattern);
    	return out.toString();
	}

    
    /**
     * Pass write iniatated values.
     * @param dateFormat
     * @param excludeProperties
     * @param includeProperties
     * @param listMapKey
     * @param listMapValue
     */
    private void initValues(DateFormat dateFormat,
    		Collection<Pattern> excludeProperties, Collection<Pattern> includeProperties,
    		Pattern listMapKey,Pattern listMapValue){
    	// TODO if passed ROOT object is primitive ??
    	if(dateFormat != null){
    		this.dateFormat = dateFormat;
    	}
    	this.excludeProperties 	= excludeProperties;
    	this.includeProperties 	= includeProperties;
    	this.listMapKey			= listMapKey;
    	this.listMapValue		= listMapValue;
    	
    	this.buildExpr = ((excludeProperties != null) && 
        		!excludeProperties.isEmpty()) || 
        		((includeProperties != null) && 
        				!includeProperties.isEmpty());
    }
    
    /**
     * Object processing entry.
     * Serialize object into json
     * @param object
     * @param method
     * @throws JSONFault
     */
    private void process(Object object, Method method) throws JSONFault {
    	/*
    	 * Step 1. If object is null write null and return.
    	 * Detect cyclic references
    	 */
    	 if (object == null) {
             this.add("null");
             return;
         }
    	 Class<?> 			clazz 			= object.getClass();
    	 JSONWebService 	customInfo 		= method != null ? method.getAnnotation(JSONWebService.class) : null;
    	/*
     	 * Step 2. Find is it Object and cyclic reference.
     	 * Detect cyclic references
     	 * Object is not in cyclic reference the add it to cyclic stack.
     	 */
         if (this.stack.contains(object) && !(clazz.isPrimitive() || clazz.equals(String.class))) {
             // Step 2.1 : object is cyclic reference and not primitive write as null 
        	 LOG.log(Level.FINE, "Cyclic reference detected on " + object);
             this.add("null");
             return;
         }else{
        	 this.stack.push(object);
         }
        /*
         * Step 3. Check is this class handled by Object Customizer. If yes. pass it to Object customizer.
         */
         if(this.objectCustomizers.containsKey(clazz)){
        	 this.objectCustomizers.get(clazz).encode(this.output, object);
        	 return;
         }
         
        /*
      	 * Step 4. Check is skipListWrapper enabled if yes validate and get wrapped object.
      	 */
        if(JSONCodec.listWrapperSkip && !(clazz.isPrimitive() || clazz.isEnum())){// definitely not a list
        	Object 	wrapperContent = getWrapperList(object,clazz);
        	if(wrapperContent != null){ 
        		object = wrapperContent;
        		//this.stack.push(object); Since object pop after process not nice to push wrapper.
        	}
        }
        /*
      	 * Step 5. convert given object to JSON.
      	 */
        if (object instanceof String || 
        		object instanceof Character || 
        		object instanceof Locale ||
        		object instanceof Class) {
        	// Step 5.1: If given object instance of String,Character,Locale or Class write class to string using convert to string
            this.string(object);
        } else if (object instanceof Number) {// Big integer and Big double handled here
        	// Step 5.2: If given object instance of number add to json.
            this.add(object);
        } else if (object instanceof Boolean) {
        	// Step 5.3: If given object instance of boolean add to json.
            this.bool(((Boolean) object).booleanValue());
        } else if (object instanceof Date) {
        	// Step 5.4: If given object instance of date add to JSON with date customizer. util and sql date and timestamp handled here 
            this.date((Date) object, method, customInfo);
        } else if (object instanceof Calendar) {
        	// Step 5.5: If given object instance of Calendar convert to date and add to JSON with date customizer. 
            this.date(((Calendar) object).getTime(), method, customInfo);
        } else if (object instanceof Map) {
        	// Step 5.6: handle has Map
            this.map((Map<?,?>) object, method, customInfo);
        } else if (object.getClass().isArray()) {
        	// Step 5.7: handle has array
            this.array(object, method, customInfo);
        } else if (object instanceof Iterable) {
        	// Step 5.8: handle has List
            this.array(((Iterable<?>) object).iterator(), method, customInfo);
        }  else if (object instanceof Enum) {
        	// Step 5.9: handle has Enumeration
            this.enumeration((Enum<?>) object, clazz);
        } else {
        	// Step 5.10: handle has Object
            this.bean(object, clazz);
        }
        this.stack.pop();
    }
    
    
    /**
     * Step 5.1:  Convert to string with escape.
     * escape characters
     */
    private void string(Object obj) {
        this.add('"');

        CharacterIterator it = new StringCharacterIterator(obj.toString());

        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            if (c == '"') {
                this.add("\\\"");
            } else if (c == '\\') {
                this.add("\\\\");
            } else if (c == '/') {
                this.add("\\/");
            } else if (c == '\b') {
                this.add("\\b");
            } else if (c == '\f') {
                this.add("\\f");
            } else if (c == '\n') {
                this.add("\\n");
            } else if (c == '\r') {
                this.add("\\r");
            } else if (c == '\t') {
                this.add("\\t");
            } else if (Character.isISOControl(c)) {
                this.unicode(c);
            } else {
                this.add(c);
            }
        }
        
        this.add('"');
    }
    
    
    /**
     * Step 5.2:  Add directly to writer, 
     * Add object to buffer
     */
    private void add(Object obj) {
        try {
			this.output.write(String.valueOf(obj).getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
    }
    
    /**
     * Step 5.3: add boolean
     * Add boolean to buffer
     */
    private void bool(boolean b) {
        this.add(b ? "true" : "false");
    }
    
    /**
     * Step 5.4, 5.5: add date
     * Add date to buffer
     */
    private void date(Date date, Method method, JSONWebService 	customInfo) {
    	assert date != null;
    	
    	/*
    	 *  Step 5.4.1: check is custom date format annotation added.
    	 */
    	DateFormat 		currentFormat 	= (customInfo != null && customInfo.format().length() > 0) ?
    			DateFormat.CUSTOM : this.dateFormat;
        
    	/*
    	 *  Step 5.4.2: serialize date based on format.
    	 */
    	switch(currentFormat){
    	case PLAIN:
    		this.add(date.getTime());
    		break;
    	case CUSTOM:
    		this.string(this.date2String(date,customInfo.format()));
    		return;
    	default:
    		this.string(this.date2String(date,currentFormat.getFormat()));
    	}
    }
    
    /**
     * Step 5.6: add date
     * Add map to buffer
     */
    private void map(Map<?,?> map, Method method, JSONWebService customInfo) throws JSONFault {
        this.add("{");

        Iterator<?> it = map.entrySet().iterator();

        boolean hasData = false;
        while (it.hasNext()) {
            Map.Entry<?,?> 	entry 	= (Map.Entry<?,?>) it.next();
            Object 			key 	= entry.getKey();
            String expr = null;
            if (this.buildExpr) {
                if (key == null) {
                    LOG.log(Level.WARNING, "Cannot build expression for null key in " + this.exprStack);
                    continue;
                } else {
                    expr = this.expandExpr(key.toString());
                    if (this.shouldExcludeProperty(expr, customInfo)) {
                        continue;
                    }
                    expr = this.setExprStack(expr);
                }
            }
            if (hasData) {
                this.add(',');
            }
            hasData = true;
            // Process key
            // TODO if key is not primitive, it is not valid JSON output. 
            this.process(key, method);
            this.add(":");
            // Process value
            this.process(entry.getValue(), method);
            if (this.buildExpr) {
                this.setExprStack(expr);
            }
        }
        
        this.add("}");
    }
    
    
    /**
     * Step 5.7: add as array.
     * Add array to buffer
     */
    private void array(Object object, Method method, JSONWebService customInfo) throws JSONFault {
        this.add("[");

        int length = Array.getLength(object);

        boolean hasData = false;
        for (int i = 0; i < length; ++i) {
            String expr = null;
            if (this.buildExpr) {
                expr = this.expandExpr(i);
                if (this.shouldExcludeProperty(expr, customInfo)) {
                    continue;
                }
                expr = this.setExprStack(expr);
            }
            if (hasData) {
                this.add(',');
            }
            hasData = true;
            this.process(Array.get(object, i), method);
            if (this.buildExpr) {
                this.setExprStack(expr);
            }
        }

        this.add("]");
    }
    
    /**
     * Step 5.8: add as list array.
     * Add array to buffer
     */
    private void array(Iterator<?> iterator, Method method, JSONWebService customInfo) throws JSONFault {
    	if(listMapKey != null && method != null && method.getGenericReturnType() != null
    			&& method.getGenericReturnType() instanceof ParameterizedType){
    		Type[] types = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments();
    		if(types.length == 1){
    			try{
	    			Class<?> clazz = (Class<?>) types[0]; 
	    			BeanInfo info = (clazz.isAnnotationPresent(JSONObject.class) && 
	            			clazz.getAnnotation(JSONObject.class).ignoreHierarchy()) ? Introspector
	                        .getBeanInfo(clazz, clazz.getSuperclass()) : Introspector
	                        .getBeanInfo(clazz);
	
	                PropertyDescriptor[] props = info.getPropertyDescriptors();
	                PropertyDescriptor	keyProperty	= null;
	                for(PropertyDescriptor prop : props){
	                	if(listMapKey.matcher(clazz.getName() + "." + prop.getName()).find()){
	                		keyProperty = prop;
	                		break;
	                	}
	                }
	                if(keyProperty != null){
	                	Method readMethod = keyProperty.getReadMethod();
	                	HashMap<String,Object> map = new LinkedHashMap<String,Object>();
	                	while(iterator.hasNext()){
	                		Object ob = iterator.next();
	                		map.put(String.valueOf(readMethod.invoke(ob)), ob);
	                	}
	                	map(map, readMethod, customInfo);
	                	return;
	                }
    			}catch(Throwable th){
    				// Continue with out Map
    			}
    		}
         } 
    	
        this.add("[");
        if(this.metaDataMode && method != null && method.getGenericReturnType() != null){
        	try {
        		Class<?> parameterType  = (Class<?>)((ParameterizedType)method.getGenericReturnType()).
						getActualTypeArguments()[0];
        		if(parameterType.equals(Object.class) || WSJSONPopulator.isJSONPrimitive(parameterType)){
        			if(method != null){
        				this.process(getMetaDataInstance(parameterType, customInfo,
        						method.getDeclaringClass().getDeclaredField(
        								Introspector.decapitalize(method.getName().substring(3)))),null);
        			}
        			// JAXB Choice
        		} else {
        			this.process(parameterType.newInstance(),method);
        		}
			} catch (Throwable e) {}
        }
        boolean hasData = false;
        for (int i = 0; iterator.hasNext(); i++) {
            String expr = null;
            if (this.buildExpr) {
                expr = this.expandExpr(i);
                if (this.shouldExcludeProperty(expr, customInfo)) {
                	iterator.next();
                    continue;
                }
                expr = this.setExprStack(expr);
            }
            if (hasData) {
                this.add(',');
            }
            hasData = true;
            this.process(iterator.next(), method);
            if (this.buildExpr) {
                this.setExprStack(expr);
            }
        }

        this.add("]");
    }
    
    
    /**
     * Step 5.9: add as Enumeration.
     * Instrospect an Enum and serialize it as a name/value pair or as a bean including all its own properties
     */
    private void enumeration(Enum<?> enumeration, Class<?> clazz) throws JSONFault {
    	try {
    		/*
    		 *  Step 5.9.1: If enumeration contains more than name declaration serialize as bean.
    		 */
			if(Introspector.getBeanInfo(clazz, 
					Enum.class).getPropertyDescriptors().length != 0){
				this.bean(enumeration, clazz);
			} else {
				this.string(enumeration.name());
			}
		} catch (IntrospectionException e) {
			this.string(enumeration.name());
		}
    }
    
    
    /**
     * Step 5.10: serialize as Object
     * Instrospect bean and serialize its properties
     */
    private void bean(Object object, Class<?> clazz) throws JSONFault {
    	assert object != null && clazz != null && !clazz.isPrimitive();

    	this.add("{");

        try {
        	/*
        	 *  Step 5.10.1: If class level JSON annotation present and ask for ignore parent level, then ignore it.
        	 */
        	BeanInfo info = (clazz.isAnnotationPresent(JSONObject.class) && 
        			clazz.getAnnotation(JSONObject.class).ignoreHierarchy()) ? Introspector
                    .getBeanInfo(clazz, clazz.getSuperclass()) : Introspector
                    .getBeanInfo(clazz,Object.class);

            PropertyDescriptor[] props = info.getPropertyDescriptors();
            if(props.length == 0){
            	// There is no property descriptor, then use public fields, RPC document require this
            	props	= PublicFieldPropertyDescriptor.getDiscriptors(clazz.getFields(),clazz);
            }
            boolean hasData = false;
            
            /*
        	 *  Step 5.10.2: Process all properties in Bean
        	 */
            nextProperty:
            for(PropertyDescriptor property : props){
            	 Class<?> 	propertyType  	= property.getPropertyType();
            	 String 	name 			= property.getName();
            	/*
              	 *  Step 5.10.2.1: If this property hard coded exclude list, exclude it. 
              	 */
                 if (this.shouldHardExcludeProperty(name)) {
                     continue;
                 }
                 
            	 Method 	accessor 	= property.getReadMethod();
            	/*
             	 *  Step 5.10.2.2: Handle special case, When property is Boolean object (not boolean primitive) and getter method starts with "is"
             	 *  This is not standard boolean declaration. But logical to do in hand written bean. Then support it.
             	 */
                 if(accessor == null && propertyType.isAssignableFrom(Boolean.class)){
                 	// for Boolean Objet is method issue
                 	try{
                 		accessor = clazz.getMethod("is"+name.substring(0, 1).toUpperCase()+name.substring(1),((Class[])null));
                 	}catch(Throwable th){/*Not an issue if not read method*/}
                 }
                 
                 /*
                  * 
             	  *  Step 5.10.2.3:
                  * TODO is it required to support cglib?
                  * if (clazz.getName().indexOf("$$EnhancerByCGLIB$$") > -1) {
                    try {
                        baseAccessor = Class.forName(
                                clazz.getName().substring(0, clazz.getName().indexOf("$$")))
                                .getMethod(accessor.getName(), accessor.getParameterTypes());
                    } catch (Exception ex) {
                        log.debug(ex.getMessage(), ex);
                    }
                }
                 **/
                 
                /*
             	 *  Step 5.10.2.4: If property accessible process it.
             	 *  
             	 */  
                 if(accessor != null){
                	 
                     Object value = null;
                     try{
                    	 value	= accessor.invoke(object, new Object[0]);
                     }catch(Throwable th){
                    	 if(property instanceof PublicFieldPropertyDescriptor){
                    		 value = ((PublicFieldPropertyDescriptor)property).getValue(object);
                    	 }
                    	 /*TODO trace*/
                     }
                    /*
                   	 *  Step 5.10.2.4.1: Read property value from object. If value null and exclude is true continue next property.
                   	 */
                     if(value == null){
                    	 if(JSONCodec.excludeNullProperties && !this.metaDataMode){
                    		 continue nextProperty;
                    	 }else{
                    		/*
     	                	 *  Step 5.10.2.4.2: Read property value from object. If value null attempt to get it from default value.
     	                	 */
                     		 try{
      	                    	Field declaredField = getDeclaredField(clazz,name);
      	                    	XmlElement 	xmlElm 	=  declaredField.getAnnotation(XmlElement.class);
      	                        if(xmlElm != null){
      	                        	if(!xmlElm.defaultValue().equals(NULL) && WSJSONPopulator.isJSONPrimitive(propertyType)){
      	                        		value = xmlElm.defaultValue();
      	                        	} else if(!xmlElm.nillable()){
      	                        		// TODO throw exception to user
      	                        	}
      	                        }
      	                    }catch(Throwable th){}
                    	 }
                    	 if(this.metaDataMode){
	      	                // In meta data mode always get data via meta provider. 
	      	                value = getMetaDataInstance(propertyType, accessor.getAnnotation(JSONWebService.class),
	      	                		getDeclaredField(clazz,name));
	                     }
                 	 }else if(this.metaDataMode && propertyType.isPrimitive()){
                 		 // Primitive meta data. In case like int value become 0. But it may be from default
                 		value = getMetaDataInstance(propertyType, accessor.getAnnotation(JSONWebService.class),
  	                			getDeclaredField(clazz,name));
                 	 }
                      
                	/*
                  	 *  Step 5.10.2.4.3: read property custom config. If it is serializable continue process with specified name if any
                  	 */ 
                	 JSONWebService properyConfig = accessor.getAnnotation(JSONWebService.class);
                	 if (properyConfig != null) {
                         if (!properyConfig.serialize())
                             continue;
                         else if (properyConfig.name().length() > 0)
                             name = properyConfig.name();
                     }else{
                    /*
                     *  Step 5.10.2.4.4: JSON config not present. Then read XML configuration.
                     */ 	 
                    	 try{
                    		
                    		// XML annotation present at field level.
 	                    	Field declaredField = getDeclaredField(clazz,name);
 	                    	
 	                       /*
 	                        *  Step 5.10.2.4.5: If XML transient ignore property.
 	                        */
 	                    	if(accessor.isAnnotationPresent(XmlTransient.class) || 
 	                    			(declaredField != null && declaredField.isAnnotationPresent(XmlTransient.class))){
 	                    		continue nextProperty;
 	                    	}else if(declaredField.isAnnotationPresent(XmlMimeType.class)){
 	                    		if(this.metaDataMode){
 	                    			value	= declaredField.getAnnotation(XmlMimeType.class).value();
 	                    		}else{
 	                    			Map<String,Object> attachment = new HashMap<String, Object>();;
 	                    			attachment .put("name",name);
 	                    			attachment.put("value",value);
 	                    			attachment.put("mimeType",declaredField.getAnnotation(XmlMimeType.class).value());
 	                    			attachments	 .add(attachment);
 	                    			continue nextProperty;
 	                    		}
 	                    	}
 	                    	if(declaredField != null){
	 	                    	// XML choice list
 	                    	   /*
 	                            *  Step 5.10.2.4.4.1: Is it XML choice list?. If assign find right element name.
 	                            */ 
	 	                    	if(declaredField.isAnnotationPresent(XmlElements.class) && Collection.class.isAssignableFrom(declaredField.getType())
	 	                    			&& value instanceof Collection){
	 	                    		XmlElements xmlElms =  declaredField.getAnnotation(XmlElements.class);
	 	                    		Collection<?> valueList = (Collection<?>)value;
	 	                    		if(!valueList.isEmpty()){
	 	                    			// use first object to identify type
	 	                    			Map<String,List<Object>> group = new HashMap<String,List<Object>>();
	 	                    			for(Object ob : valueList){
	 	                    				for(XmlElement elm : xmlElms.value()){
		 	                    				if(((Class<?>)elm.type()).isAssignableFrom(ob.getClass())){
		 	                    					name = elm.name();
		 	                    					if(!group.containsKey(name))
		 	                    						group.put(name, new ArrayList<Object>());
		 	                    					group.get(name).add(ob);
		 	                    				}
		 	                    			}
	 	                    			}
	 	                    			for(Map.Entry<String, List<Object>> entry : group.entrySet()){
	 	                    				hasData = this.add(entry.getKey(), entry.getValue(), null, hasData) || hasData;
	 	                                    // TODO this.setExprStack(expr);
	 	                    			}
	 	                    			continue nextProperty;
	 	                    		} else {
	 	                    			// If choice element id empty, don't print it at all
	 	                    			if(!this.metaDataMode){
	 	                    				continue nextProperty;
	 	                    			} else {
	 	                    				name	= "CHOICE";
	 	                    				Map<String,Object> choices 		= new HashMap<String, Object>(); 
	 	                    				for(XmlElement elm : xmlElms.value()){
	 	                    					try{
	 	                    						choices.put(elm.name(), getMetaDataInstance(elm.type(),null,null));
	 	                    					}catch(Throwable th){
	 	                    						// 
	 	                    						choices.put(elm.name(),"object");
	 	                    					}
	 	                    				}
	 	                    				value = choices;
	 	                    			}
	 	                    		}
	 	                    	}else if(declaredField.isAnnotationPresent(XmlElement.class)){
	 	                    		/*
	  	                            *  Step 5.10.2.4.4.2: Xml elements .
	  	                            */ 
	 	                    		if(!declaredField.getAnnotation(XmlElement.class).name().equals(XML_DEFAULT)){
	 	                    			name = declaredField.getAnnotation(XmlElement.class).name();
	 	                    		}
	 	                    		
	 	                    	}else if(declaredField.isAnnotationPresent(XmlAttribute.class)){
	 	                    		/*
	  	                            *  Step 5.10.2.4.4.3: Xml attribute.
	  	                            */ 
	 	                    		if(!declaredField.getAnnotation(XmlAttribute.class).name().equals(XML_DEFAULT)){
	 	                    			name = declaredField.getAnnotation(XmlAttribute.class).name();
	 	                    		}
		 	                    		
		 	                    }
 	                    	}
                     	}catch(Throwable th){
                     		LOG.log(Level.FINER,"Processing xml annatation failed for field: "+name);
                     	}
                     }
                	 
                	/*
                   	 *  Step 5.10.2.4.5: if custom exclude and include present create JSON expression to handle it.
                   	 */ 
                	 String expr = null;
                     if (this.buildExpr) {
                         expr = this.expandExpr(name);
                         if (this.shouldExcludeProperty(expr, properyConfig)) {
                             continue;
                         }
                         expr = this.setExprStack(expr);
                     }
                     
                     /*
                	 *  Step 5.10.2.4.6: write value
                	 */ 
                     boolean propertyPrinted = this.add(name, value, accessor, hasData);
                     hasData = hasData || propertyPrinted;
                     if (this.buildExpr) {
                         this.setExprStack(expr);
                     }
                 }
            }

            // special-case handling for an Enumeration - include the name() as a property */
            if (object instanceof Enum) {
                Object value = ((Enum<?>) object).name();
                this.add("_name", value, object.getClass().getMethod("name"), hasData);
            }
        } catch (Exception e) {
            throw new JSONFault("Server.json", "Failed to serialize object "+clazz.getName(), "JSONCodec", null);
        }

        this.add("}");
    }
    
    
    /**
     * Step 5.10.1: add object again
     * Add name/value pair to buffer
     */
    private boolean add(String name, Object value, Method method, boolean hasData) throws JSONFault {
        if (!JSONCodec.excludeNullProperties || value != null || this.metaDataMode) {
            if (hasData) {
                this.add(',');
            }
            this.add('"');
            this.add(name);
            this.add("\":");
            this.process(value, method);
            return true;
        }

        return false;
    }
  

    /**
     * Private method to chack exclude or include.
     * @param expr
     * @return
     */
    private boolean shouldExcludeProperty(String expr,JSONWebService config) {
    	if(config != null && config.excludeProperties().length > 0){
    		for(String match : config.excludeProperties()){
    			if (Pattern.compile(match).matcher(expr).matches()) {
                    LOG.log(Level.FINEST, "Ignoring property because of exclude set to true in annotation: " + expr);
                    return true;
                }
    		}
    	}
    	
        if (this.excludeProperties != null) {
            for (Pattern pattern : this.excludeProperties) {
                if (pattern.matcher(expr).matches()) {
                    LOG.log(Level.FINEST, "Ignoring property because of exclude rule: " + expr);
                    return true;
                }
            }
        }

        if(config != null && config.includeProperties().length > 0){
    		for(String match : config.includeProperties()){
    			if (Pattern.compile(match).matcher(expr).matches()) {
                    return false;
                }
    		}
    		LOG.log(Level.FINEST, "Ignoring property because of include rule set to true in annotation: " + expr);
    	}
        
        if (this.includeProperties != null) {
            for (Pattern pattern : this.includeProperties) {
                if (pattern.matcher(expr).matches()) {
                    return false;
                }
            }
            LOG.log(Level.FINEST, "Ignoring property because of exclude rule: " + expr);
            return true;
        }
        return false;
    }

    
    ///// JSON Expression stack private method  Start
    
    private String expandExpr(int i) {
        return this.exprStack + "[" + i + "]";
    }

    private String expandExpr(String property) {
        if (this.exprStack.length() == 0)
            return property;
        return this.exprStack + "." + property;
    }

    private String setExprStack(String expr) {
        String s = this.exprStack;
        this.exprStack = expr;
        return s;
    }
    
    ///// JSON Expression stack private method end
    
    /**
     * Utility V  write as character
     * Add char to buffer
     */
    private void add(char c) {
        try {
			this.output.write(c);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
    }

   

    /**
     * Utility IV  write as unicode
     * Represent as unicode
     *
     * @param c character to be encoded
     */
    private void unicode(char c) {
        this.add("\\u");

        int n = c;

        for (int i = 0; i < 4; ++i) {
            int digit = (n & 0xf000) >> 12;

            this.add(hex[digit]);
            n <<= 4;
        }
    }
    
    /**
     * Utility III  never serializable properties. 
     * Ignore "class" field
     */
    private boolean shouldHardExcludeProperty(String name)
            throws SecurityException, NoSuchFieldException {
        if (name.equals("class") || name.equals("declaringClass") || name.equals("serialVersionUID")) {
            return true;
        }
        return false;
    }
    
    
    /**
     * Utility II to convert date to string.
     *
     * @param date
     * @return
     * @see
     */
     private String date2String(Date date,String timePattern) {
    	 if(timePattern == null || timePattern.trim().isEmpty() ){
    		 timePattern = "yyyy-MM-dd'T'HH:mm:ssZ";
    	 }
    	 SimpleDateFormat 	formatter 	= new SimpleDateFormat(timePattern);
    	 if(JSONCodec.useTimezoneSeparator){
    	   StringBuffer 	dateStr 	= new StringBuffer(formatter.format(date));
    	   return dateStr.length() > 22 ? dateStr.insert(22, ':').toString() : dateStr.toString();
    	 }else{
    	   return formatter.format(date);
    	 }
    }
     
     /**
 	 * Utility I method to read Object list from Wrapper 
 	 * @param object
 	 * @param clazz
 	 * @return
 	 */
 	private Object getWrapperList(Object object,Class<?> clazz){
     	/* JSON webservice strip List wrapper  parameter 
 		 * Read by XmlType and number of property.
 		 *
 		 * @XmlAccessorType(XmlAccessType.FIELD)
         	@XmlType(name = "xxxxItems", propOrder = {
         	    "xxxxItem"
         	})
            public class ReportItems implements Serializable {
          * 
          */	
         try {
         	if(object == null || clazz == null || clazz.isPrimitive())
         		return null;
         	XmlType xmlType = clazz.getAnnotation(XmlType.class);
     		if(xmlType != null && xmlType.propOrder().length == 1 &&
     				xmlType.name().equals(xmlType.propOrder()[0]+"s")){
     			PropertyDescriptor[] properties = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
     			if(properties.length == 1 &&
     					Collection.class.isAssignableFrom(properties[0].getReadMethod().getReturnType())){
     				return properties[0].getReadMethod().invoke(object, (Object[])null);
     			}
     		}
         } catch (Throwable e) {/*Dont mind*/}
         return null;
         // End
     }
 	
 	
 	/**
 	 * Utility method return instance from class. For meta data document generation.
 	 * @param propertyType
 	 * @param field 
 	 * @param webService 
 	 * @return
 	 */
 	private Object getMetaDataInstance(Class<?> propertyType, JSONWebService webService, Field field){
 		String defaultVal = null;
 		if(field != null && field.isAnnotationPresent(XmlElement.class)){
 			XmlElement element = field.getAnnotation(XmlElement.class);
			if(!element.defaultValue().equals(NULL)){
				if(field != null && Collection.class.isAssignableFrom(field.getType())){
					return element.defaultValue().replace(",", "\",\"");
				} else if(propertyType.isEnum()){
					defaultVal	= element.defaultValue();
					// In case of enum meta data is decided list
				} else if(Boolean.TYPE.equals(propertyType) || Boolean.class.equals(propertyType)){
					return Boolean.valueOf(element.defaultValue());
				} else {
					return element.defaultValue();
				}
			}
		}
 		
 		if(WSJSONPopulator.isJSONPrimitive(propertyType)){
  			// Go with null
  			if(Number.class.isAssignableFrom(propertyType) 
  					|| Integer.TYPE.equals(propertyType)
  					|| Byte.TYPE.equals(propertyType)
  					|| Short.TYPE.equals(propertyType)
  					|| Long.TYPE.equals(propertyType)){
  				return 0;	
  			}else if(propertyType.isAssignableFrom(String.class)){
  				return "";
  			}else if(propertyType.isAssignableFrom(Boolean.class)
  					|| Boolean.TYPE.equals(propertyType)){
  				return false;
  			}else if(propertyType.isAssignableFrom(Date.class)){
  				return new Date();
  			}else if(propertyType.isEnum()){
  				StringBuffer b = new StringBuffer();
  				if(defaultVal != null){
  					// Write default value as first constant in meta data.
  					b.append(defaultVal);
  				}
  				for(Object cont: propertyType.getEnumConstants()){
  					String name = ((Enum<?>)cont).name();
  					if(name.equals(defaultVal))continue;
  					b.append((b.length() != 0 ? "|" :"") + name);
  				}
  				return b.toString();
  			}else if(Float.TYPE.equals(propertyType) || Double.TYPE.equals(propertyType)){
  				return 0.0;	
  			}else {
  				return null;
  			}
  		}else{
  			for(Object ob : stack){
  				if(ob.getClass().equals(propertyType)){
  					return ob;
  				}
  			}
			try{
				return propertyType.newInstance();
			}catch(Exception e){// Datahandler
				return propertyType.getSimpleName();
			}
  		}
 	}

 	/**
	 * Utility method to read declaring field including private scope.
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	private java.lang.reflect.Field getDeclaredField(Class<?> clazz, String fieldName){
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (Throwable e) {
			if(!Object.class.equals(clazz.getSuperclass())){
				return getDeclaredField(clazz.getSuperclass(),fieldName);
			}
		}
		return null;
	}
	/**
	 * Getter to return attachments
	 * @return
	 */
	public List<Map<String, Object>> getAttachments() {
		return attachments;
	}
}
