package test;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.ws.Holder;

import com.test.jsonwebservice.rpc.XmlElementsSeqObj;
import com.test.jsonwebservice.rpc.XmlElementsSeqWrapperObj;
import com.test.jsonwebservice.rpc.XmlElementsWrapperObj;

public class JAXBAnnotationTest extends JSONCodecTest /*implements
		com.test.jsonwebservice.rpc.JAXBAnnotationTest*/ {

	protected void setUp() throws Exception {
		END_POINT = "http://localhost:8080/unitTest/json/jaxbannotation";
		super.setUp();
	}
	
	public void test1EmptyInXmlElementsOut() throws MalformedURLException, IOException {
		String in 	= "{\"test1EmptyInXmlElementsOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"xmlElementsObj\":{\"mapObject\":[{\"keyProperty1\":1,\"keyProperty2\":\"KK\",\"property1\":true,\"valueProperty1\":2,\"valueProperty2\":\"DD\"}]," +
				"\"objectReserved\":[{\"boolean\":true,\"enum\":\"CONST_2\",\"float\":1.2,\"int\":1,\"String\":\"SS\"}]," +
				"\"object\":[{\"property1\":1},{\"property1\":2}]}}";
		assertEquals(out, expected);
	}

	public void test2XmlElementsInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test2XmlElementsInEmptyOut\":{\"xmlElementsObj\":{\"mapObject\":[{\"keyProperty1\":1,\"keyProperty2\":\"KK\",\"property1\":true,\"valueProperty1\":2,\"valueProperty2\":\"DD\"}]," +
				"\"objectReserved\":[{\"boolean\":true,\"enum\":\"CONST_2\",\"float\":1.2,\"int\":1,\"String\":\"SS\"}]," +
				"\"object\":[{\"property1\":1},{\"property1\":2}]}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test3XmlElementsInXmlElementsOut() throws MalformedURLException, IOException {
		String in 	= "{\"test3XmlElementsInXmlElementsOut\":{\"xmlElementsObj\":{\"mapObject\":[{\"keyProperty1\":1,\"keyProperty2\":\"KK\",\"property1\":true,\"valueProperty1\":2,\"valueProperty2\":\"DD\"}]," +
		"\"objectReserved\":[{\"boolean\":true,\"enum\":\"CONST_2\",\"float\":1.2,\"int\":1},{\"boolean\":true,\"enum\":\"CONST_1\",\"float\":2.2,\"int\":22}]," +
		"\"object\":[{\"property1\":1},{\"property1\":2},{\"property1\":3}]}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true," +
				"\"xmlElementsObj\":{\"mapObject\":[{\"keyProperty1\":1,\"keyProperty2\":\"KK\",\"property1\":true,\"valueProperty1\":2,\"valueProperty2\":\"DD\"}]," +
		"\"object\":[{\"property1\":1},{\"property1\":2},{\"property1\":3}]," +
		"\"objectReserved\":[{\"boolean\":true,\"enum\":\"CONST_2\",\"float\":1.2,\"int\":1},{\"boolean\":true,\"enum\":\"CONST_1\",\"float\":2.2,\"int\":22}]}}";
		assertEquals(out, expected);
	}

	public void test4EmptyInXmlElementsWrapOut() throws MalformedURLException, IOException {
		String in 	= "{\"test4EmptyInXmlElementsWrapOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"xmlElementsWrapperObj\":{\"choiceList\":{\"mapObject\":[{\"keyProperty1\":1,\"keyProperty2\":\"KK\",\"property1\":true,\"valueProperty1\":2,\"valueProperty2\":\"DD\"}],\"objectReserved\":[{\"boolean\":true,\"enum\":\"CONST_2\",\"float\":1.2,\"int\":1,\"String\":\"SS\"}],\"object\":[{\"property1\":1},{\"property1\":2}]}}}";
		assertEquals(out, expected);
	}

	public void test5XmlElementsWrapInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test5XmlElementsWrapInEmptyOut\":{\"xmlElementsWrapperObj\":{\"choiceList\":{\"mapObject\":[{\"keyProperty1\":1,\"keyProperty2\":\"KK\",\"property1\":true,\"valueProperty1\":2,\"valueProperty2\":\"DD\"}],\"objectReserved\":[{\"boolean\":true,\"enum\":\"CONST_2\",\"float\":1.2,\"int\":1,\"String\":\"SS\"}],\"object\":[{\"property1\":1},{\"property1\":2}]}}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test6XmlElmWrapInXmlElmWrapOut(
			Holder<XmlElementsWrapperObj> xmlElementsWrapperObj) {
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

}
