package com.mycompany;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;


@WebService (name="HelloService", targetNamespace="http://album.jsonplugin.com/json/")
public class HelloWorldImpl {

	@WebMethod (operationName="sayHello")
	public @WebResult(name="message") String sayHello(String name){
		return "Hello "+name;
	}
}
