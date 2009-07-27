package com.jaxws.json.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
@Retention(value=RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(value= ElementType.METHOD)
public @interface JSONWebService {
	String	listMapKey() default "";
	String	listMapValue() default "";
	String[] includeProperties() default {};
	String[] excludeProperties() default {};
}
