package com.test.jsonwebservice.rpc.impl;

import java.util.List;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import com.test.jsonwebservice.rpc.EnumConst;
import com.test.jsonwebservice.rpc.JAXBAnnotationTest;
import com.test.jsonwebservice.rpc.MapObject;
import com.test.jsonwebservice.rpc.NillablesObj;
import com.test.jsonwebservice.rpc.Object;
import com.test.jsonwebservice.rpc.ObjectReservedFields;
import com.test.jsonwebservice.rpc.XmlElementRefsObj;
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
		// Object order test
		ob = new Object();ob.setProperty1(3);
		objectList.add(ob);
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
		if(xmlElementsWrapperObj == null || xmlElementsWrapperObj.value == null
				|| xmlElementsWrapperObj.value.getChoiceList() == null ||
				xmlElementsWrapperObj.value.getChoiceList().getObjectOrObjectReservedOrMapObject().isEmpty()){
			throw new RuntimeException();
		}
	}

	public XmlElementsSeqObj test7EmptyInXmlElementsSeqOut() {
		XmlElementsSeqObj xmlElementSeq = new XmlElementsSeqObj();
		List<java.lang.Object> objectList = xmlElementSeq.getObjectAndObjectReservedAndMapObject();
		Object ob = new Object();ob.setProperty1(1);
		objectList.add(ob);
		ob = new Object();ob.setProperty1(2);
		objectList.add(ob);
		ObjectReservedFields obRes = new ObjectReservedFields();
		obRes.setBoolean(true);obRes.setEnum(EnumConst.CONST_1);obRes.setFloat(1.2f);obRes.setInt(1);obRes.setString("rr");
		objectList.add(obRes);
		MapObject	mapOb = new MapObject();
		mapOb.setKeyProperty1(1);mapOb.setKeyProperty2("tt77");mapOb.setProperty1(true);mapOb.setValueProperty1(2);mapOb.setValueProperty2("FF");
		objectList.add(mapOb);
		return xmlElementSeq;
	}

	public void test8XmlElementsSeqInEmptyOut(
			XmlElementsSeqObj xmlElementsSeqObj) {
		if(xmlElementsSeqObj == null || xmlElementsSeqObj.getObjectAndObjectReservedAndMapObject().isEmpty()){
			throw new RuntimeException();
		}
	}

	public void test9XmlElementsSeqInXmlElementsSeqOut(
			Holder<XmlElementsSeqObj> xmlElementsSeqObj) {
		if(xmlElementsSeqObj == null || xmlElementsSeqObj.value == null){
			throw new RuntimeException();
		}
	}
	
	public XmlElementsSeqWrapperObj test10EmptyInXmlElementsSeqWrapOut() {
		XmlElementsSeqWrapperObj warpObj = new XmlElementsSeqWrapperObj();
		XmlElementsSeqObj xmlElementSeq = new XmlElementsSeqObj();
		List<java.lang.Object> objectList = xmlElementSeq.getObjectAndObjectReservedAndMapObject();
		Object ob = new Object();ob.setProperty1(1);
		objectList.add(ob);
		ob = new Object();ob.setProperty1(2);
		objectList.add(ob);
		ObjectReservedFields obRes = new ObjectReservedFields();
		obRes.setBoolean(true);obRes.setEnum(EnumConst.CONST_1);obRes.setFloat(1.2f);obRes.setInt(1);obRes.setString("DD");
		objectList.add(obRes);
		MapObject	mapOb = new MapObject();
		mapOb.setKeyProperty1(1);mapOb.setKeyProperty2("FF");mapOb.setProperty1(true);mapOb.setValueProperty1(2);mapOb.setValueProperty2("FF");
		objectList.add(mapOb);
		warpObj.setSequenceList(xmlElementSeq );
		return warpObj;
	}

	public void test11XmlElementsSeqWrapInEmptyOut(
			XmlElementsSeqWrapperObj xmlElementsSeqWrapperObj) {
		if(xmlElementsSeqWrapperObj == null || xmlElementsSeqWrapperObj.getSequenceList() == null
				|| xmlElementsSeqWrapperObj.getSequenceList().getObjectAndObjectReservedAndMapObject().isEmpty()){
			throw new RuntimeException();
		}
	}

	public void test12XmlElmWrapSeqInXmlElmSeqWrapOut(
			Holder<XmlElementsSeqWrapperObj> xmlElementsSeqWrapperObj) {
		if(xmlElementsSeqWrapperObj == null || xmlElementsSeqWrapperObj.value == null
				|| xmlElementsSeqWrapperObj.value.getSequenceList().getObjectAndObjectReservedAndMapObject().isEmpty()){
			throw new RuntimeException();
		}
	}

	public XmlElementRefsObj test13EmptyInXmlElementRefsOut() {
		XmlElementRefsObj obj = new XmlElementRefsObj();
		JAXBElement<String> elem = new JAXBElement<String>(new QName("name"), String.class, "testStr");
		obj.getNameAndTypeAndLable().add(elem );
		elem = new JAXBElement<String>(new QName("lable"), String.class, "lable");
		obj.getNameAndTypeAndLable().add(elem );
		return obj;
	}

	public void test14XmlElementRefsInEmptyOut(
			XmlElementRefsObj xmlElementRefsObj) {
		if(xmlElementRefsObj == null || xmlElementRefsObj.getNameAndTypeAndLable().isEmpty())
			throw new UnsupportedOperationException();
	}

	public void test15XmlElementRefsInXmlElementRefsOut(
			Holder<XmlElementRefsObj> xmlElementRefsObj) {
		if(xmlElementRefsObj == null || xmlElementRefsObj.value == null)
			throw new UnsupportedOperationException();
	}

	public void test16XmlForceNillableInxmlForceNillableOut(
			Holder<NillablesObj> nillables) {
		if(nillables.value == null){
			throw new UnsupportedOperationException();
		}
	}

	public XmlElementsObj test17EmptyInXmlElementsSingleObjOut() {
		XmlElementsObj elements = new XmlElementsObj();
		List<java.lang.Object> objectList = elements.getObjectOrObjectReservedOrMapObject();
		Object ob = new Object();ob.setProperty1(1);
		objectList.add(ob);
		return elements;
	}

	public void test18XmlElementsSingleObjInEmptyOut(
			XmlElementsObj xmlElementsObj) {
		if(xmlElementsObj == null || xmlElementsObj.getObjectOrObjectReservedOrMapObject() == null
				|| xmlElementsObj.getObjectOrObjectReservedOrMapObject().isEmpty()){
			throw new RuntimeException();
		}
		
	}

	public void test19XmlElementsSingleObjInXmlElementsSingleObjOut(
			Holder<XmlElementsObj> xmlElementsObj) {
		if(xmlElementsObj == null || xmlElementsObj.value.getObjectOrObjectReservedOrMapObject() == null
				|| xmlElementsObj.value.getObjectOrObjectReservedOrMapObject().isEmpty()){
			throw new RuntimeException();
		}
	}
}
