package com.test.jsonwebservice.rpc.impl;

import java.util.List;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;

import com.test.jsonwebservice.rpc.EnumConst;
import com.test.jsonwebservice.rpc.JAXBAnnotationTest;
import com.test.jsonwebservice.rpc.MapObject;
import com.test.jsonwebservice.rpc.Object;
import com.test.jsonwebservice.rpc.ObjectReservedFields;
import com.test.jsonwebservice.rpc.XmlElementsObj;
import com.test.jsonwebservice.rpc.XmlElementsSeqObj;
import com.test.jsonwebservice.rpc.XmlElementsSeqWrapperObj;
import com.test.jsonwebservice.rpc.XmlElementsWrapperObj;

@WebService(name = "JAXBAnnotationTest", targetNamespace = "http://jsonwebservice.test.com/rpc",
		endpointInterface="com.test.jsonwebservice.rpc.JAXBAnnotationTest")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class JAXBAnnotationTestImpl implements JAXBAnnotationTest {

	public XmlElementsObj test1EmptyInXmlElementsOut() {
		XmlElementsObj elements = new XmlElementsObj();
		List<java.lang.Object> objectList = elements.getObjectOrObjectReservedOrMapObject();
		Object ob = new Object();ob.setProperty1(1);
		objectList.add(ob);
		ob = new Object();ob.setProperty1(2);
		objectList.add(ob);
		ObjectReservedFields obRes = new ObjectReservedFields();
		obRes.setBoolean(true);obRes.setEnum(EnumConst.CONST_2);obRes.setFloat(1.2f);obRes.setInt(1);obRes.setString("SS");
		objectList.add(obRes);
		MapObject	mapOb = new MapObject();
		mapOb.setKeyProperty1(1);mapOb.setKeyProperty2("KK");mapOb.setProperty1(true);mapOb.setValueProperty1(2);mapOb.setValueProperty2("DD");
		objectList.add(mapOb);
		return elements;
	}

	public void test2XmlElementsInEmptyOut(XmlElementsObj xmlElementsObj) {
		if(xmlElementsObj == null || xmlElementsObj.getObjectOrObjectReservedOrMapObject().isEmpty()
				|| xmlElementsObj.getObjectOrObjectReservedOrMapObject().size() < 3){
			throw new RuntimeException();
		}
	}

	public void test3XmlElementsInXmlElementsOut(
			Holder<XmlElementsObj> xmlElementsObj) {
		if(xmlElementsObj == null || xmlElementsObj.value == null){
			throw new RuntimeException();
		}

	}

	public XmlElementsWrapperObj test4EmptyInXmlElementsWrapOut() {
		XmlElementsWrapperObj elementsWrap = new XmlElementsWrapperObj();
		XmlElementsObj elements = new XmlElementsObj();
		List<java.lang.Object> objectList = elements.getObjectOrObjectReservedOrMapObject();
		Object ob = new Object();ob.setProperty1(1);
		objectList.add(ob);
		ob = new Object();ob.setProperty1(2);
		objectList.add(ob);
		ObjectReservedFields obRes = new ObjectReservedFields();
		obRes.setBoolean(true);obRes.setEnum(EnumConst.CONST_2);obRes.setFloat(1.2f);obRes.setInt(1);obRes.setString("SS");
		objectList.add(obRes);
		MapObject	mapOb = new MapObject();
		mapOb.setKeyProperty1(1);mapOb.setKeyProperty2("KK");mapOb.setProperty1(true);mapOb.setValueProperty1(2);mapOb.setValueProperty2("DD");
		objectList.add(mapOb);
		elementsWrap.setChoiceList(elements);
		return elementsWrap;
	}

	public void test5XmlElementsWrapInEmptyOut(
			XmlElementsWrapperObj xmlElementsWrapperObj) {
		if(xmlElementsWrapperObj == null || xmlElementsWrapperObj.getChoiceList() == null
				|| xmlElementsWrapperObj.getChoiceList().getObjectOrObjectReservedOrMapObject().isEmpty() ){
			throw new RuntimeException();
		}
	}

	public void test6XmlElmWrapInXmlElmWrapOut(
			Holder<XmlElementsWrapperObj> xmlElementsWrapperObj) {
		// TODO Auto-generated method stub

	}

	public XmlElementsSeqWrapperObj test10EmptyInXmlElementsSeqWrapOut() {
		// TODO Auto-generated method stub
		return null;
	}

	public void test11XmlElementsSeqWrapInEmptyOut(
			XmlElementsSeqWrapperObj xmlElementsSeqWrapperObj) {
		// TODO Auto-generated method stub
		
	}

	public void test12XmlElmWrapSeqInXmlElmSeqWrapOut(
			Holder<XmlElementsSeqWrapperObj> xmlElementsSeqWrapperObj) {
		// TODO Auto-generated method stub
		
	}

	public XmlElementsSeqObj test7EmptyInXmlElementsSeqOut() {
		// TODO Auto-generated method stub
		return null;
	}

	public void test8XmlElementsSeqInEmptyOut(
			XmlElementsSeqObj xmlElementsSeqObj) {
		// TODO Auto-generated method stub
		
	}

	public void test9XmlElementsSeqInXmlElementsSeqOut(
			Holder<XmlElementsSeqObj> xmlElementsSeqObj) {
		// TODO Auto-generated method stub
		
	}

}
