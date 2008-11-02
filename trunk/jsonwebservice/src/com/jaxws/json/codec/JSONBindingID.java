package com.jaxws.json.codec;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Codec;

/**
 * @author Sundaramurthi
 * @version 1.0
 * @mail sundaramurthis@gmail.com
 */
public class JSONBindingID extends BindingID {

    public static final String JSON_BINDING = "http://jsonplugin.googlecode.com/json/";

    public SOAPVersion getSOAPVersion() {
        return SOAPVersion.SOAP_11;
    }

    public @NotNull Codec createEncoder(@NotNull WSBinding binding) {
        return new JSONCodec(binding);
    }

    public String toString() {
        return JSON_BINDING;
    }

    @Override
    public boolean canGenerateWSDL() {
        return true;
    }

}
