package com.test.jsonwebservice.rpc.impl;

import java.util.GregorianCalendar;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import com.test.jsonwebservice.rpc.DateObject;
import com.test.jsonwebservice.rpc.EnumConst;
import com.test.jsonwebservice.rpc.Object;
import com.test.jsonwebservice.rpc.ObjectReservedFields;
import com.test.jsonwebservice.rpc.ParameterTest;

@SOAPBinding(style=Style.RPC)
@WebService(name = "ParameterTest", targetNamespace = "http://jsonwebservice.test.com/parameter",
		endpointInterface="com.test.jsonwebservice.rpc.ParameterTest")
public class ParameterTestImpl implements ParameterTest {

	public void test1EmptyInOut() {
		System.out.println("server process executed");

	}

	public void test2StringInEmptyOut(String string) {
		System.out.println(string);
		
	}

	public String test3EmptyInStringOut() {
		return "OK";
	}

	public String test4StringInStringOut(String string) {
		return string;
	}

	public void test5IntInEmptyOut(int integer) {
		if(integer == 0)
			throw new RuntimeException();
		
	}

	public int test6EmptyInIntOut() {
		return 1;
	}

	public int test7IntInIntOut(int integer) {
		return integer;
	}

	public String test8IntInStringOut(int integer) {
		return String.valueOf(integer);
	}

	public int test9StringInIntOut(String string) {
		return Integer.decode(string);
	}

	public void test10BooleanInEmptyOut(boolean bool) {
		if(!bool)
			throw new RuntimeException();
		
	}

	public boolean test11EmptyInBooleanOut() {
		return true;
	}

	public boolean test12BooleanInBooleanOut(boolean bool) {
		return bool;
	}

	public EnumConst test13EmptyInEnumOut() {
		return EnumConst.CONST_2;
	}

	public void test14EnumInEmptyOut(EnumConst enumConst) {
		if(enumConst == null)
			throw new RuntimeException();
		
	}

	public EnumConst test15EnumInEnumOut(EnumConst enumConst) {
		return enumConst;
	}

	public Object test16EmptyInObjectOut() {
		Object obj = new Object();
		obj.setProperty1(1);
		return obj;
	}

	public void test17ObjectInEmptyOut(Object object) {
		if(object == null || object.getProperty1() == 0)
			throw new RuntimeException();
		
	}

	public Object test18ObjectInObjectOut(Object object) {
		return object;
	}

	public String test19ObjectInStringOut(Object object) {
		return String.valueOf(object.getProperty1());
	}

	public Object test20StringInObjectOut(String string) {
		Object obj = new Object();
		obj.setProperty1(Integer.parseInt(string));
		return obj;
	}

	public int test21ObjectInNumberOut(Object object) {
		return object.getProperty1();
	}

	public Object test22NumberInObjectOut(int integer) {
		Object obj = new Object();
		obj.setProperty1(integer);
		return obj;
	}

	public EnumConst test23ObjectInEnumOut(Object object) {
		if(object == null || object.getProperty1() == 0){
			throw new RuntimeException();
		}
		return EnumConst.CONST_2;
	}

	public Object test24EnumInObjectOut(EnumConst enumConst) {
		if(enumConst == null){
			throw new RuntimeException();
		}
		return new Object();
	}

	public void test25StringInStringOut(Holder<String> string) {
		if(string == null || string.value == null){
			throw new RuntimeException();
		}
		
	}

	public void test26IntInIntOut(Holder<Integer> integer) {
		if(integer == null || integer.value == null){
			throw new RuntimeException();
		}
	}

	public void test27EnumInEnumOut(Holder<EnumConst> enumConst) {
		if(enumConst == null || enumConst.value == null){
			throw new RuntimeException();
		}
		
	}

	public void test28ObjectInObjectOut(Holder<Object> object) {
		if(object == null || object.value == null){
			throw new RuntimeException();
		}
	}

	public int test29ReservedInIntOut(int _int) {
		return _int;
	}

	public int test30IntInReservedOut(int integer) {
		return integer;
	}

	public boolean test31ReservedInBooleanOut(boolean _boolean) {
		return _boolean;
	}

	public boolean test32ObjectReservedInBooleanOut(
			ObjectReservedFields objectReservedFields) {
		if(!objectReservedFields.isBoolean() || objectReservedFields.getFloat() == 0.0f ||
				objectReservedFields.getEnum() == null || objectReservedFields.getInt() == 0
				/* TODO || objectReservedFields.getString() == null*/){
			throw new RuntimeException();
			
		}
		return objectReservedFields.isBoolean();
	}

	public void test33NumNumInEmptyOut(int integer1, int integer2) {
		if(integer1 == 0 || integer2 == 0)
			throw new RuntimeException();
	}

	public void test34EmptyInNumNumOut(Holder<Integer> integer1,
			Holder<Integer> integer2) {
		if(integer1 == null || integer2 == null)
			throw new RuntimeException();
		
		integer1.value 	= 1;
		integer2.value	= 2;
		
	}

	public void test35NumStringInEmptyOut(int integer, String string) {
		if(string == null || integer == 0)
			throw new RuntimeException();
		
	}

	public void test36EmptyInNumStringOut(Holder<Integer> integer,
			Holder<String> string) {
		if(string == null || integer == null )
			throw new RuntimeException();
		integer.value = 1;
		string.value 	="SS";
	}

	public void test37StringStringInEmptyOut(String string1, String string2) {
		if(string1 == null || string2 == null )
			throw new RuntimeException();
		
	}

	public void test38EmptyInStringStringOut(Holder<String> string1,
			Holder<String> string2) {
		if(string1 == null || string2 == null )
			throw new RuntimeException();
		string1.value	= "SS1";
		string2.value	= "SS2";
		
	}

	public void test39ObjectStringInEmptyOut(Object object, String string) {
		if(object == null || string == null || object.getProperty1() == 0)
			throw new RuntimeException();
	}

	public void test40EmptyInObjectStringOut(Holder<Object> object,
			Holder<String> string) {
		if(object == null || string == null)
			throw new RuntimeException();
		Object o = new Object();
		o.setProperty1(1);
		object.value	= o;
		string.value	= "SSS";
	}

	public void test41StringObjectInEmptyOut(String string, Object object) {
		if(object == null || string == null || object.getProperty1() == 0)
			throw new RuntimeException();
	}

	public void test42EmptyInStringObjectOut(Holder<String> string,
			Holder<Object> object) {
		if(object == null || string == null)
			throw new RuntimeException();
		string.value	= "DD";
		Object o = new Object();
		o.setProperty1(1);
		object.value	= o;
		
	}

	public void test43StringNumInEmptyOut(String string, int integer) {
		if(string == null || integer == 0)
			throw new RuntimeException();
	}

	public void test44DateTimeInEmptyOut(XMLGregorianCalendar dateTime) {
		if(dateTime == null || dateTime.getDay() == 0)
			throw new RuntimeException();
	}

	public XMLGregorianCalendar test45EmptyInDateTimeOut() {
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(1, 4, 2011));
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void test46DateTimeInDateTimeOut(
			Holder<XMLGregorianCalendar> dateTime) {
		if(dateTime == null || dateTime.value == null)
			throw new RuntimeException();
		
	}

	public void test47DateObjectInDateObjectOut(Holder<DateObject> dateObject) {
		if(dateObject == null || dateObject.value == null || dateObject.value.getDate() == null)
			throw new RuntimeException();
	}

}
