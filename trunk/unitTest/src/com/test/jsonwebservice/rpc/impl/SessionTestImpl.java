package com.test.jsonwebservice.rpc.impl;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.ws.handler.MessageContext;

import com.sun.xml.ws.api.pipe.Fiber;
import com.test.jsonwebservice.rpc.SessionTest;

@WebService(name = "SessionTest", targetNamespace = "http://jsonwebservice.test.com/rpc",
		endpointInterface="com.test.jsonwebservice.rpc.SessionTest")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class SessionTestImpl implements SessionTest {

	@WebMethod
	@WebResult(name = "stringOut", partName = "stringOut")
	public String test1StringInStringOut(
			@WebParam(name = "string", partName = "string") String string) {
		HttpServletRequest request = (HttpServletRequest)Fiber.current().getPacket().get(MessageContext.SERVLET_REQUEST);
		HttpSession session = request.getSession(true);
		if(session != null){
			Object sessionAttr = session.getAttribute("sessionAttr");
			session.setAttribute("sessionAttr", string);
			if(sessionAttr != null){
				return sessionAttr.toString();
			}
		}
		return string;
	}

}
