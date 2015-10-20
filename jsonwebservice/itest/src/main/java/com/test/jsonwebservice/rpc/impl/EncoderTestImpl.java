package com.test.jsonwebservice.rpc.impl;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import com.test.jsonwebservice.rpc.EncoderTest;
import com.test.jsonwebservice.rpc.Object;

@SOAPBinding(style=Style.RPC)
@WebService(name="EncoderPort",serviceName = "RPCTestService", portName = "EncoderPort",
		targetNamespace = "http://jsonwebservice.test.com/rpc",
		endpointInterface="com.test.jsonwebservice.rpc.EncoderTest",
		wsdlLocation="WEB-INF/wsdl/rpc.wsdl")
		/*
		 * IMPORTANT wsdlLocation important to read mime type declaration from wsdl.
		 * Absence of wsdlLocation lead to jax-ws generation wsdl. jax-ws genearted wsdl dont havespecified mime content in wsdl.
		 * 
		 * Also it is important to match service name and portname with wsdl
		 */
public class EncoderTestImpl implements EncoderTest {

	public Object test1JSONInHtmlOut(Object object) {
		return object;
	}

}
