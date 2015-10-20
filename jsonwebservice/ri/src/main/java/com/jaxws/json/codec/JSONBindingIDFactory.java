package com.jaxws.json.codec;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.BindingIDFactory;

/**
 * @author Sundaramurthi
 * @version 1.0
 * @mail sundaramurthis@gmail.com
 */
public class JSONBindingIDFactory extends BindingIDFactory {

	@Override
	public BindingID parse(String lexical) throws WebServiceException {
		if(lexical.equals(JSONBindingID.JSON_BINDING))
			return new JSONBindingID();
		else
			return null;
	}

}
