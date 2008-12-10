package com.jaxws.json.builder;

import com.sun.xml.ws.model.ParameterImpl;

import javax.xml.ws.WebServiceException;


public abstract class ValueSetterFactory {

    public abstract ValueSetter get(ParameterImpl p);

    public static final ValueSetterFactory SYNC = new ValueSetterFactory() {
    	public ValueSetter get(ParameterImpl p) {
            return ValueSetter.getSync(p);
        }
    };

    static final ValueSetterFactory NONE = new ValueSetterFactory() {
    	public ValueSetter get(ParameterImpl p) {
            throw new WebServiceException("This shouldn't happen. No response parameters.");
        }
    };

    static final ValueSetterFactory SINGLE = new ValueSetterFactory() {
    	public ValueSetter get(ParameterImpl p) {
            return ValueSetter.SINGLE_VALUE;
        }
    };

    static final class AsyncBeanValueSetterFactory extends ValueSetterFactory {
        private Class asyncBean;

        AsyncBeanValueSetterFactory(Class asyncBean) {
            this.asyncBean = asyncBean;
        }

        public  ValueSetter get(ParameterImpl p) {
            return new ValueSetter.AsyncBeanValueSetter(p, asyncBean);
        }
    }

}
