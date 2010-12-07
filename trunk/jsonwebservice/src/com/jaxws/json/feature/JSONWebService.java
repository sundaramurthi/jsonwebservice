package com.jaxws.json.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
@Retention(value=RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(value= ElementType.METHOD)
public @interface JSONWebService {
	/**
	 * JSON notation name
	 * @return
	 */
	String name() default "";

    /**
     * @return
     */
    boolean serialize() default true;

    /**
     * @return
     */
    boolean deserialize() default true;

    /**
     * Applicable for date format, (TODO support with custom number format if nessary)
     * @return
     */
    String format() default "";
    
	/**
	 * Applicable of Object returns which has list 
	 * @return
	 */
	String	listMapKey() default "";
	
	/**
	 * Applicable of Object returns which has list 
	 * @return
	 */
	String	listMapValue() default "";
	
	/**
	 * Include properties
	 * @return
	 */
	String[] includeProperties() default {};
	
	/**
	 * Exclude properties
	 * @return
	 */
	String[] excludeProperties() default {};
}
