package com.test.jsonwebservice.rpc.impl;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;

import com.jaxws.json.feature.JSONWebService;
import com.test.jsonwebservice.rpc.CustomizeTest;
import com.test.jsonwebservice.rpc.GlobalKeyMapObject;
import com.test.jsonwebservice.rpc.GlobalKeyMapObjectList;
import com.test.jsonwebservice.rpc.GlobalKeyValueMapObject;
import com.test.jsonwebservice.rpc.GlobalKeyValueMapObjectList;
import com.test.jsonwebservice.rpc.MapObject;
import com.test.jsonwebservice.rpc.MapObjectList;

@WebService(name = "CustomizeTest", targetNamespace = "http://jsonwebservice.test.com/parameter",
		endpointInterface="com.test.jsonwebservice.rpc.CustomizeTest")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class CustomizeTestImpl implements CustomizeTest {

	public MapObject test1DefaultMapObjectOut() {
		MapObject mapObject = new MapObject();
		mapObject.setKeyProperty1(1);
		mapObject.setKeyProperty2("KEY1");
		mapObject.setValueProperty1(1);
		mapObject.setValueProperty2("VALUE1");
		mapObject.setProperty1(true);
		return mapObject;
	}

	public void test2DefaultMapObjectIn(MapObject mapObject) {
		if(mapObject == null || mapObject.getKeyProperty1() == 0 || mapObject.getValueProperty2() == null){
			throw new RuntimeException();
		}
	}
	
	public MapObjectList test3DefaultMapObjectListOut() {
		MapObjectList list = new MapObjectList();
		
		MapObject mapObject = new MapObject();
		mapObject.setKeyProperty1(1);
		mapObject.setKeyProperty2("KEY1");
		mapObject.setValueProperty1(1);
		mapObject.setValueProperty2("VALUE1");
		mapObject.setProperty1(true);
		list.getMap().add(mapObject);
		
		mapObject = new MapObject();
		mapObject.setKeyProperty1(2);
		mapObject.setKeyProperty2("KEY2");
		mapObject.setValueProperty1(2);
		mapObject.setValueProperty2("VALUE2");
		mapObject.setProperty1(false);
		list.getMap().add(mapObject);
		
		return list;
	}
	
	public void test4DefaultMapObjectListInOut(
			Holder<MapObjectList> mapObjectList) {
		if(mapObjectList == null || mapObjectList.value == null){
			throw new RuntimeException();
		}
	}


	public GlobalKeyMapObjectList test5GlobalKeyMapObjectListOut() {
		GlobalKeyMapObjectList list = new GlobalKeyMapObjectList();
		
		GlobalKeyMapObject mapObject = new GlobalKeyMapObject();
		mapObject.setKeyProperty1(1);
		mapObject.setKeyProperty2("KEY1");
		mapObject.setValueProperty1(1);
		mapObject.setValueProperty2("VALUE1");
		mapObject.setProperty1(true);
		list.getMap().add(mapObject);
		
		mapObject = new GlobalKeyMapObject();
		mapObject.setKeyProperty1(2);
		mapObject.setKeyProperty2("KEY2");
		mapObject.setValueProperty1(2);
		mapObject.setValueProperty2("VALUE2");
		mapObject.setProperty1(false);
		list.getMap().add(mapObject);
		
		return list;
	}

	public void test6GlobalKeyMapObjectListInOut(
			Holder<GlobalKeyMapObjectList> globalKeyMapObjectList) {
		if(globalKeyMapObjectList == null || globalKeyMapObjectList.value == null){
			throw new RuntimeException();
		}
		
	}

	@JSONWebService(listMapKey="NONE")
	public GlobalKeyMapObjectList test7GlobalKeyMapObjectListAnatEmptyOut() {
		GlobalKeyMapObjectList list = new GlobalKeyMapObjectList();
		
		GlobalKeyMapObject mapObject = new GlobalKeyMapObject();
		mapObject.setKeyProperty1(11);
		mapObject.setKeyProperty2("KEY22");
		mapObject.setValueProperty1(11);
		mapObject.setValueProperty2("VALUE11");
		mapObject.setProperty1(true);
		list.getMap().add(mapObject);
		
		mapObject = new GlobalKeyMapObject();
		mapObject.setKeyProperty1(22);
		mapObject.setKeyProperty2("KEY22");
		mapObject.setValueProperty1(22);
		mapObject.setValueProperty2("VALUE22");
		mapObject.setProperty1(false);
		list.getMap().add(mapObject);
		return list;
	}

	@JSONWebService(listMapKey="NONE")
	public void test8GlobalKeyMapObjectListAnatEmptyInOut(
			Holder<GlobalKeyMapObjectList> globalKeyMapObjectList) {
		if(globalKeyMapObjectList == null || globalKeyMapObjectList.value == null){
			throw new RuntimeException();
		}
	}

	@JSONWebService(listMapKey="com\\.test\\.jsonwebservice\\.rpc\\.GlobalKeyMapObject\\.keyProperty2")
	public GlobalKeyMapObjectList test9GlobalKeyMapObjectListAnatOut() {
		GlobalKeyMapObjectList list = new GlobalKeyMapObjectList();
		
		GlobalKeyMapObject mapObject = new GlobalKeyMapObject();
		mapObject.setKeyProperty1(111);
		mapObject.setKeyProperty2("KEY111");
		mapObject.setValueProperty1(111);
		mapObject.setValueProperty2("VALUE111");
		mapObject.setProperty1(true);
		list.getMap().add(mapObject);
		
		mapObject = new GlobalKeyMapObject();
		mapObject.setKeyProperty1(222);
		mapObject.setKeyProperty2("KEY222");
		mapObject.setValueProperty1(222);
		mapObject.setValueProperty2("VALUE222");
		mapObject.setProperty1(false);
		list.getMap().add(mapObject);
		return list;
	}
	
	@JSONWebService(listMapKey="com\\.test\\.jsonwebservice\\.rpc\\.GlobalKeyMapObject\\.keyProperty2")
	public void test10GlobalKeyMapObjectListAnatInOut(
			Holder<GlobalKeyMapObjectList> globalKeyMapObjectList) {
		if(globalKeyMapObjectList == null || globalKeyMapObjectList.value == null){
			throw new RuntimeException();
		}
	}
	
	@JSONWebService(listMapKey="com\\.test\\.jsonwebservice\\.rpc\\.GlobalKeyValueMapObject\\.keyProperty2",
			listMapValue="com\\.test\\.jsonwebservice\\.rpc\\.GlobalKeyValueMapObject\\.valueProperty2")
    public com.test.jsonwebservice.rpc.GlobalKeyValueMapObjectList test11EmptyInKeyValueMapObjectOut(){
    	GlobalKeyValueMapObjectList list = new GlobalKeyValueMapObjectList();
    	GlobalKeyValueMapObject arg0 = new GlobalKeyValueMapObject();
    	arg0.setKeyProperty2("ss");
    	arg0.setKeyProperty1(11);
    	arg0.setProperty1(true);
    	arg0.setValueProperty1(111);
    	arg0.setValueProperty2("DDD");
		list.getMap().add(arg0 );
		arg0 = new GlobalKeyValueMapObject();
		arg0.setKeyProperty2("ff");
    	arg0.setKeyProperty1(22);
    	arg0.setProperty1(true);
    	arg0.setValueProperty1(222);
    	arg0.setValueProperty2("kkk");
		list.getMap().add(arg0 );
    	return list;
    }

	
    public com.test.jsonwebservice.rpc.GlobalKeyValueMapObjectList test12EmptyInGlobalKeyValueMapObjectOut(){
    	GlobalKeyValueMapObjectList list = new GlobalKeyValueMapObjectList();
    	GlobalKeyValueMapObject arg0 = new GlobalKeyValueMapObject();
    	arg0.setKeyProperty2("ss");
    	arg0.setKeyProperty1(11);
    	arg0.setProperty1(true);
    	arg0.setValueProperty1(111);
    	arg0.setValueProperty2("DDD");
		list.getMap().add(arg0 );
		arg0 = new GlobalKeyValueMapObject();
		arg0.setKeyProperty2("ff");
    	arg0.setKeyProperty1(22);
    	arg0.setProperty1(true);
    	arg0.setValueProperty1(222);
    	arg0.setValueProperty2("kkk");
		list.getMap().add(arg0 );
    	return list;
    }


}
