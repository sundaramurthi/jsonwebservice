package test;

import java.io.IOException;
import java.net.MalformedURLException;

public class EncoderTest extends JSONCodecTest/*implements com.test.jsonwebservice.rpc.EncoderTest*/ {

	protected void setUp() throws Exception {
		END_POINT = "http://localhost:8080/unitTest/json/encoder";
		super.setUp();
	}
	
	public void test1JSONInHtmlOut() throws MalformedURLException, IOException {
		String in 	= "{\"test1JSONInHtmlOut\":{\"object\":{\"property1\":12}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "<html><body>object</body></html>";
		assertEquals(out, expected);
		
	}

}
