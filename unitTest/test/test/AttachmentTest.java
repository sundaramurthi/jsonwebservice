package test;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.ws.Holder;

public class AttachmentTest extends JSONCodecTest /*implements
		com.test.jsonwebservice.rpc.AttachmentTest*/ {

	protected void setUp() throws Exception {
		END_POINT = "http://localhost:8080/unitTest/json/attachment";
		super.setUp();
	}
	
	public void test1EmptyInImageOut() throws MalformedURLException, IOException {
		String in 	= "{\"test1EmptyInImageOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"mapObject\":{\"keyProperty1\":1,\"keyProperty2\":\"KEY1\",\"property1\":true,\"valueProperty1\":1,\"valueProperty2\":\"VALUE1\"},\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test2ImageInEmptyOut(byte[] image) {
		// TODO Auto-generated method stub

	}

	public void test3ImageInImageOut(Holder<byte[]> arg0) {
		// TODO Auto-generated method stub

	}

	public void test4EmptyInImageWithInfoOut(Holder<String> arg0,
			Holder<byte[]> arg1) {
		// TODO Auto-generated method stub

	}

	public void test5ImageWithInfoInEmptyOut(String info, byte[] image) {
		// TODO Auto-generated method stub

	}

	public void test6ImageWithInfoInImageWithInfoOut(Holder<String> arg0,
			Holder<byte[]> arg1) {
		// TODO Auto-generated method stub

	}

}
