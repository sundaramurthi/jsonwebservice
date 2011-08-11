package com.mycompany;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService (name="HelloService", targetNamespace="http://album.jsonplugin.com/json/")
public class HelloWorldImpl {

	@WebMethod (operationName = "sayHello")
	public @WebResult(name="message") String sayHello(
			@WebParam(name="name") String name){
		return "Hello "+name;
	}
}
