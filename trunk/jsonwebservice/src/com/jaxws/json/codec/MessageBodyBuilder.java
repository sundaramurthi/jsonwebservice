package com.jaxws.json.codec;

import java.util.ArrayList;
import java.util.List;

import com.jaxws.json.builder.BodyBuilder;
import com.jaxws.json.builder.ResponseBuilder;
import com.jaxws.json.builder.ValueGetterFactory;
import com.jaxws.json.builder.ValueSetter;
import com.jaxws.json.builder.ValueSetterFactory;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;

public class MessageBodyBuilder {

	final protected SOAPVersion 	soapVersion;

	public MessageBodyBuilder(SOAPVersion soapVersion) {
		super();
		this.soapVersion = soapVersion;
	}

	protected final BodyBuilder getRequestBodyBuilder(List<ParameterImpl> parameters) {
		BodyBuilder bodyBuilder = null;
		for (ParameterImpl param : parameters) {
			com.jaxws.json.builder.ValueGetter getter = com.jaxws.json.builder.ValueGetterFactory.SYNC
					.get(param);
			switch (param.getInBinding().kind) {
			case BODY:
				if (param.isWrapperStyle()) {
					WrapperParameter wrappedParam = (WrapperParameter) param;
					if (param.getParent().getBinding().isRpcLit())
						bodyBuilder = new BodyBuilder.RpcLit(wrappedParam,
								soapVersion, ValueGetterFactory.SYNC);
					else
						bodyBuilder = new BodyBuilder.DocLit(wrappedParam,
								soapVersion, ValueGetterFactory.SYNC);
				} else {
					bodyBuilder = new BodyBuilder.Bare(param, soapVersion,
							getter);
				}
				break;
			/*
			 * case HEADER: fillers.add(new MessageFiller.Header(
			 * param.getIndex(), param.getBridge(), getter )); break; case
			 * ATTACHMENT:
			 * fillers.add(MessageFiller.AttachmentFiller.createAttachmentFiller
			 * (param, getter)); break;
			 */
			case UNBOUND:
				break;
			default:
				throw new AssertionError(); // impossible
			}
		}

		if (bodyBuilder == null) {
			// no parameter binds to body. we create an empty message
			switch (soapVersion) {
			case SOAP_11:
				bodyBuilder = BodyBuilder.EMPTY_SOAP11;
				break;
			case SOAP_12:
				bodyBuilder = BodyBuilder.EMPTY_SOAP12;
				break;
			default:
				throw new AssertionError();
			}
		}
		return bodyBuilder;
	}
	
	protected final ResponseBuilder getResponseBuilder(List<ParameterImpl> parameters) {
		List<ResponseBuilder> builders = new ArrayList<ResponseBuilder>();
		for (ParameterImpl param : parameters) {
			ValueSetter setter;
			switch (param.getOutBinding().kind) {
			case BODY:
				if (param.isWrapperStyle()) {
					WrapperParameter wParam = (WrapperParameter) param;
					if (param.getParent().getBinding().isRpcLit())
						builders.add(new ResponseBuilder.RpcLit(wParam,
								ValueSetterFactory.SYNC));
					else
						builders.add(new ResponseBuilder.DocLit(wParam,
								ValueSetterFactory.SYNC));
				} else {
					setter = ValueSetterFactory.SYNC.get(param);
					builders.add(new ResponseBuilder.Body(param.getBridge(),
							setter));
				}

				break;
			case HEADER:
				setter = ValueSetterFactory.SYNC.get(param);
				builders.add(new ResponseBuilder.Header(soapVersion, param,
						setter));
				break;
			case ATTACHMENT:
				setter = ValueSetterFactory.SYNC.get(param);
				builders.add(ResponseBuilder.AttachmentBuilder
						.createAttachmentBuilder(param, setter));
				break;
			case UNBOUND:
				setter = ValueSetterFactory.SYNC.get(param);
				builders.add(new ResponseBuilder.NullSetter(setter,
						ResponseBuilder.getVMUninitializedValue(param
								.getTypeReference().type)));
				break;
			default:
				throw new AssertionError();
			}
		}
		ResponseBuilder rb;
		switch (builders.size()) {
		case 0:
			rb = ResponseBuilder.NONE;
			break;
		case 1:
			rb = builders.get(0);
			break;
		default:
			rb = new ResponseBuilder.Composite(builders);
		}
		return rb;
	}

}
