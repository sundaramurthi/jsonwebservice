package test;

import java.io.IOException;
import java.net.MalformedURLException;

public class CustomizeTest extends JSONCodecTest /*implements
		com.test.jsonwebservice.rpc.CustomizeTest*/ {
	
	protected void setUp() throws Exception {
		END_POINT = "http://localhost:8080/unitTest/json/customize";
		super.setUp();
	}
	
	
	public void test1DefaultMapObjectOut() throws MalformedURLException, IOException {
		String in 	= "{\"test1DefaultMapObjectOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"mapObject\":{\"keyProperty1\":1,\"keyProperty2\":\"KEY1\",\"property1\":true,\"valueProperty1\":1,\"valueProperty2\":\"VALUE1\"},\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test2DefaultMapObjectIn() throws MalformedURLException, IOException {
		String in 	= "{\"test2DefaultMapObjectIn\":{\"mapObject\":{\"keyProperty1\":1,\"keyProperty2\":\"KEY1\",\"property1\":true,\"valueProperty1\":1,\"valueProperty2\":\"VALUE1\"}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
	}
	
	public void test3DefaultMapObjectListOut() throws MalformedURLException, IOException {
		String in 	= "{\"test3DefaultMapObjectListOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"mapObjectList\":{\"map\":[{\"keyProperty1\":1,\"keyProperty2\":\"KEY1\",\"property1\":true,\"valueProperty1\":1,\"valueProperty2\":\"VALUE1\"}," +
				"{\"keyProperty1\":2,\"keyProperty2\":\"KEY2\",\"property1\":false,\"valueProperty1\":2,\"valueProperty2\":\"VALUE2\"}]}}";
		assertEquals(out, expected);
	}


	public void test4DefaultMapObjectListInOut() throws MalformedURLException, IOException {
		String in 	= "{\"test4DefaultMapObjectListInOut\":{\"mapObjectList\":{\"map\":[{\"keyProperty1\":4,\"keyProperty2\":\"KEYyyy\",\"property1\":true,\"valueProperty1\":1,\"valueProperty2\":\"VALUE1\"}," +
				"{\"keyProperty1\":2,\"keyProperty2\":\"KEY2\",\"property1\":false,\"valueProperty1\":2,\"valueProperty2\":\"VALUE2\"}]}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"mapObjectList\":{\"map\":[{\"keyProperty1\":4,\"keyProperty2\":\"KEYyyy\",\"property1\":true,\"valueProperty1\":1,\"valueProperty2\":\"VALUE1\"}," +
				"{\"keyProperty1\":2,\"keyProperty2\":\"KEY2\",\"property1\":false,\"valueProperty1\":2,\"valueProperty2\":\"VALUE2\"}]}}";
		assertEquals(out, expected);
		
	}
	
	public void test5GlobalKeyMapObjectListOut() throws MalformedURLException, IOException {
		String in 	= "{\"test5GlobalKeyMapObjectListOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"globalKeyMapObjectList\":{\"map\":{\"1\":{\"keyProperty1\":1,\"keyProperty2\":\"KEY1\",\"property1\":true,\"valueProperty1\":1,\"valueProperty2\":\"VALUE1\"}," +
				"\"2\":{\"keyProperty1\":2,\"keyProperty2\":\"KEY2\",\"property1\":false,\"valueProperty1\":2,\"valueProperty2\":\"VALUE2\"}}},\"statusFlag\":true}";
		assertEquals(out, expected);
	}


	public void test6GlobalKeyMapObjectListInOut() throws MalformedURLException, IOException {
		String in 	= "{\"test6GlobalKeyMapObjectListInOut\":{\"globalKeyMapObjectList\":{\"map\":{\"1\":{\"keyProperty1\":1,\"keyProperty2\":\"KEY1\",\"property1\":true,\"valueProperty1\":1,\"valueProperty2\":\"VALUE1\"}," +
				"\"2\":{\"keyProperty1\":2,\"keyProperty2\":\"KEY2\",\"property1\":false,\"valueProperty1\":2,\"valueProperty2\":\"VALUE2\"}}}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"globalKeyMapObjectList\":{\"map\":{\"2\":{\"keyProperty1\":2,\"keyProperty2\":\"KEY2\",\"property1\":false,\"valueProperty1\":2,\"valueProperty2\":\"VALUE2\"},\"1\":{\"keyProperty1\":1,\"keyProperty2\":\"KEY1\",\"property1\":true,\"valueProperty1\":1,\"valueProperty2\":\"VALUE1\"}}},\"statusFlag\":true}";
		assertEquals(out, expected);
		
	}

    public void test7GlobalKeyMapObjectListAnatEmptyOut() throws MalformedURLException, IOException{
    	String in 	= "{\"test7GlobalKeyMapObjectListAnatEmptyOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"globalKeyMapObjectList\":{\"map\":[{\"keyProperty1\":11,\"keyProperty2\":\"KEY22\",\"property1\":true,\"valueProperty1\":11,\"valueProperty2\":\"VALUE11\"},{\"keyProperty1\":22,\"keyProperty2\":\"KEY22\",\"property1\":false,\"valueProperty1\":22,\"valueProperty2\":\"VALUE22\"}]},\"statusFlag\":true}";
		assertEquals(out, expected);
    }

   
    public void test8GlobalKeyMapObjectListAnatEmptyInOut() throws MalformedURLException, IOException{
    	String in 	= "{\"test8GlobalKeyMapObjectListAnatEmptyInOut\":{\"globalKeyMapObjectList\":{\"map\":[{\"keyProperty1\":66,\"keyProperty2\":\"KEY66\",\"property1\":true,\"valueProperty1\":11,\"valueProperty2\":\"VALUE11\"},{\"keyProperty1\":22,\"keyProperty2\":\"KEY22\",\"property1\":false,\"valueProperty1\":22,\"valueProperty2\":\"VALUE22\"}]}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"globalKeyMapObjectList\":{\"map\":[{\"keyProperty1\":66,\"keyProperty2\":\"KEY66\",\"property1\":true,\"valueProperty1\":11,\"valueProperty2\":\"VALUE11\"},{\"keyProperty1\":22,\"keyProperty2\":\"KEY22\",\"property1\":false,\"valueProperty1\":22,\"valueProperty2\":\"VALUE22\"}]},\"statusFlag\":true}";
		assertEquals(out, expected);
    }

   
    public void test9GlobalKeyMapObjectListAnatOut() throws MalformedURLException, IOException{
    	String in 	= "{\"test9GlobalKeyMapObjectListAnatOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"globalKeyMapObjectList\":{\"map\":{\"KEY111\":{\"keyProperty1\":111,\"keyProperty2\":\"KEY111\",\"property1\":true,\"valueProperty1\":111,\"valueProperty2\":\"VALUE111\"},\"KEY222\":{\"keyProperty1\":222,\"keyProperty2\":\"KEY222\",\"property1\":false,\"valueProperty1\":222,\"valueProperty2\":\"VALUE222\"}}},\"statusFlag\":true}";
		assertEquals(out, expected);
    }

    public void test10GlobalKeyMapObjectListAnatInOut() throws MalformedURLException, IOException{
    	String in 	= "{\"test10GlobalKeyMapObjectListAnatInOut\":{\"globalKeyMapObjectList\":{\"map\":{\"KEY111\":{\"keyProperty1\":111,\"keyProperty2\":\"KEYXXX\",\"property1\":true,\"valueProperty1\":111,\"valueProperty2\":\"VALUE111\"},\"KEY222\":{\"keyProperty1\":222,\"keyProperty2\":\"KEY222\",\"property1\":false,\"valueProperty1\":222,\"valueProperty2\":\"VALUE222\"}}}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"globalKeyMapObjectList\":{\"map\":{\"KEY222\":{\"keyProperty1\":222,\"keyProperty2\":\"KEY222\",\"property1\":false,\"valueProperty1\":222,\"valueProperty2\":\"VALUE222\"},\"KEYXXX\":{\"keyProperty1\":111,\"keyProperty2\":\"KEYXXX\",\"property1\":true,\"valueProperty1\":111,\"valueProperty2\":\"VALUE111\"}}},\"statusFlag\":true}";
		assertEquals(out, expected);
    }
}
