package com.test.jsonwebservice.rpc.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.test.jsonwebservice.rpc.BigObject;
import com.test.jsonwebservice.rpc.GlobalKeyMapObject;
import com.test.jsonwebservice.rpc.InputFieldType;
import com.test.jsonwebservice.rpc.LargeList;
import com.test.jsonwebservice.rpc.Object;
import com.test.jsonwebservice.rpc.ObjectReservedFields;
import com.test.jsonwebservice.rpc.PerformanceTest;
import com.test.jsonwebservice.rpc.XmlElementsObj;

@WebService(name = "PerformanceTest", targetNamespace = "http://jsonwebservice.test.com/rpc",
		endpointInterface="com.test.jsonwebservice.rpc.PerformanceTest")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class PerformanceTestImpl implements PerformanceTest {
	
	static final List<BigObject>  bigObject;
	static{
		bigObject = new ArrayList<BigObject>();
		for (int i = 0; i < 1000000; i++) {
			bigObject.add(getBigObject(true));
		}
	}
	public LargeList test1SizeInLargeListOut(int integer) {
		LargeList largeList = new LargeList();
		largeList.getBigObject().addAll(bigObject.subList(0, integer));
		return largeList;
	}
	
	public int test2LargeListInSizeOut(LargeList largeList){
		return largeList.getBigObject().size();
	}

	private static BigObject getBigObject(boolean root) {
		BigObject ob = new BigObject();
		ob.setProperty1((int)System.currentTimeMillis());
		ob.setProperty2(true);
		ob.setProperty3("Dummy");
		ob.setProperty4(100);
		//XMLGregorianCalendar cal = new XMLGregorianCalendar();
		ob.setProperty5(null);
		ob.setProperty6("Dummy2");
		ob.setProperty7(getObject());
		ob.setProperty8(2);
		ob.setProperty9("Dummpy 3");
		ob.setProperty10("Property 10");
		ob.setProperty11(getReservedObj());
		ob.setProperty12(InputFieldType.TEXT);
		XmlElementsObj xmlElms = new XmlElementsObj();
		xmlElms.getObjectOrObjectReservedOrMapObject().add(getObject());
		xmlElms.getObjectOrObjectReservedOrMapObject().add(getReservedObj());
		ob.setProperty13(xmlElms );
		ob.setProperty14(getGlobalMapKey());
		if(root)
			ob.setProperty15(getBigObject(false));
		return ob;
	}

	private static GlobalKeyMapObject getGlobalMapKey() {
		GlobalKeyMapObject mapKey = new GlobalKeyMapObject();
		mapKey.setKeyProperty1(44);
		mapKey.setKeyProperty2("KEY");
		mapKey.setProperty1(true);
		mapKey.setValueProperty1(88);
		mapKey.setValueProperty2("VALUE");
		return mapKey;
	}

	private static ObjectReservedFields getReservedObj() {
		ObjectReservedFields reservedObje = new ObjectReservedFields();
		reservedObje.setString("Reserved");
		reservedObje.setInt(66);
		reservedObje.setBoolean(true);
		return reservedObje;
	}

	private static com.test.jsonwebservice.rpc.Object getObject() {
		Object obj = new Object();
		obj.setProperty1((int)System.currentTimeMillis());
		return obj;
	}

}
