package com.jaxws.json.builder;

import com.sun.xml.ws.model.ParameterImpl;

import javax.jws.WebParam;

/**
 * {@link ValueGetterFactory} is used to create {@link ValueGetter} objects.
 *
 */
public abstract class ValueGetterFactory {

    public abstract ValueGetter get(ParameterImpl p);

    public static final ValueGetterFactory SYNC = new ValueGetterFactory() {
        public ValueGetter get(ParameterImpl p) {
            return (p.getMode()== WebParam.Mode.IN || p.getIndex() == -1)
                    ? ValueGetter.PLAIN : ValueGetter.HOLDER;
        }
    };

    /**
     * In case of SEI async signatures, there are no holders. The OUT
     * parameters go in async bean class
     */
    static final ValueGetterFactory ASYNC = new ValueGetterFactory() {
    	public ValueGetter get(ParameterImpl p) {
            return ValueGetter.PLAIN;
        }
    };

}
