package test;

import java.io.IOException;
import java.net.MalformedURLException;

public class ParameterTest extends JSONCodecTest /*implements com.test.jsonwebservice.parameter.ParameterTest*/{

	public void test1EmptyInOut() throws MalformedURLException, IOException {
		String in 	= "{\"test1EmptyInOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
		
	}

	public void test2StringtInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test2StringInEmptyOut\":{\"string\":\"ok\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
		
	}

	public void test3EmptyInStringOut() throws MalformedURLException, IOException {
		String in 	= "{\"test3EmptyInStringOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"string\":\"OK\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test4StringInStringOut() throws MalformedURLException, IOException {
		String in 	= "{\"test4StringInStringOut\":{\"string\":\"TESTOK\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"stringOut\":\"TESTOK\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test5IntInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test5IntInEmptyOut\":{\"integer\":1}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
		
		in 	= "{\"test5IntInEmptyOut\":{\"integer\":\"1\"}}";
		System.out.println("IN: " + in);
		out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
		
	}

	public void test6EmptyInIntOut() throws MalformedURLException, IOException {
		String in 	= "{\"test6EmptyInIntOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"integer\":1,\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test7IntInIntOut() throws MalformedURLException, IOException {
		String in 	= "{\"test7IntInIntOut\":{\"integer\":1}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"intOut\":1,\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test8IntInStringOut() throws MalformedURLException, IOException {
		String in 	= "{\"test8IntInStringOut\":{\"integer\":12}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"string\":\"12\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test9StringInIntOut() throws MalformedURLException, IOException {
		String in 	= "{\"test9StringInIntOut\":{\"string\":\"456\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"integer\":456,\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test10BooleanInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test10BooleanInEmptyOut\":{\"bool\":true}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
		
		in 	= "{\"test10BooleanInEmptyOut\":{\"bool\":\"true\"}}";
		System.out.println("IN: " + in);
		out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test11EmptyInBooleanOut() throws MalformedURLException, IOException {
		String in 	= "{\"test11EmptyInBooleanOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"bool\":true}";
		assertEquals(out, expected);
	}

	public void test12BooleanInBooleanOut() throws MalformedURLException, IOException {
		String in 	= "{\"test12BooleanInBooleanOut\":{\"bool\":true}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"boolOut\":true,\"statusFlag\":true}";
		assertEquals(out, expected);
		
		in 	= "{\"test12BooleanInBooleanOut\":{\"bool\":\"true\"}}";
		System.out.println("IN: " + in);
		out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		expected 	= "{\"boolOut\":true,\"statusFlag\":true}";
		assertEquals(out, expected);
		
		in 	= "{\"test12BooleanInBooleanOut\":{\"bool\":\"false\"}}";
		System.out.println("IN: " + in);
		out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		expected 	= "{\"boolOut\":false,\"statusFlag\":true}";
		assertEquals(out, expected);

	}

	public void test13EmptyInEnumOut() throws MalformedURLException, IOException {
		String in 	= "{\"test13EmptyInEnumOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"enumConst\":\"CONST_2\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test14EnumInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test14EnumInEmptyOut\":{\"enumConst\":\"CONST_2\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test15EnumInEnumOut() throws MalformedURLException, IOException {
		String in 	= "{\"test15EnumInEnumOut\":{\"enumConst\":\"CONST_3\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"enumConstOut\":\"CONST_3\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test16EmptyInObjectOut() throws MalformedURLException, IOException {
		String in 	= "{\"test16EmptyInObjectOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"object\":{\"property1\":1}}";
		assertEquals(out, expected);
	}

	public void test17ObjectInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test17ObjectInEmptyOut\":{\"object\":{\"property1\":12}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test18ObjectInObjectOut() throws MalformedURLException, IOException {
		String in 	= "{\"test18ObjectInObjectOut\":{\"object\":{\"property1\":12}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"objectOut\":{\"property1\":12}}";
		assertEquals(out, expected);
	}

	public void test19ObjectInStringOut() throws MalformedURLException, IOException {
		String in 	= "{\"test19ObjectInStringOut\":{\"object\":{\"property1\":123}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"string\":\"123\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test20StringInObjectOut() throws MalformedURLException, IOException {
		String in 	= "{\"test20StringInObjectOut\":{\"string\":\"675\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"object\":{\"property1\":675}}";
		assertEquals(out, expected);
	}

	public void test21ObjectInNumberOut() throws MalformedURLException, IOException {
		String in 	= "{\"test21ObjectInNumberOut\":{\"object\":{\"property1\":13}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"integer\":13,\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test22NumberInObjectOut() throws MalformedURLException, IOException {
		String in 	= "{\"test22NumberInObjectOut\":{\"integer\":43}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"object\":{\"property1\":43}}";
		assertEquals(out, expected);
	}

	public void test23ObjectInEnumOut() throws MalformedURLException, IOException {
		String in 	= "{\"test23ObjectInEnumOut\":{\"object\":{\"property1\":3}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"enumConst\":\"CONST_2\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test24EnumInObjectOut() throws MalformedURLException, IOException {
		String in 	= "{\"test24EnumInObjectOut\":{\"enumConst\":\"CONST_1\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"object\":{\"property1\":0}}";
		assertEquals(out, expected);
	}

	public void test25StringInStringOut() throws MalformedURLException, IOException {
		String in 	= "{\"test25StringInStringOut\":{\"string\":\"HOLDER-STRING\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"string\":\"HOLDER-STRING\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test26IntInIntOut() throws MalformedURLException, IOException {
		// HOLDER
		String in 	= "{\"test26IntInIntOut\":{\"integer\":87}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"integer\":87,\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test27EnumInEnumOut() throws MalformedURLException, IOException {
		String in 	= "{\"test27EnumInEnumOut\":{\"enumConst\":\"CONST_1\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"enumConst\":\"CONST_1\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test28ObjectInObjectOut() throws MalformedURLException, IOException {
		String in 	= "{\"test28ObjectInObjectOut\":{\"object\":{\"property1\":102}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"object\":{\"property1\":102}}";
		assertEquals(out, expected);
	}

	public void test29ReservedInIntOut() throws MalformedURLException, IOException {
		String in 	= "{\"test29ReservedInIntOut\":{\"int\":12}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"integer\":12,\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test30IntInReservedOut() throws MalformedURLException, IOException {
		String in 	= "{\"test29ReservedInIntOut\":{\"int\":123}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"integer\":123,\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test31ReservedInBooleanOut() throws MalformedURLException, IOException {
		String in 	= "{\"test31ReservedInBooleanOut\":{\"boolean\":true}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"bool\":true}";
		assertEquals(out, expected);
	}

	public void test32ObjectReservedInBooleanOut() throws MalformedURLException, IOException {
		String in 	= "{\"test32ObjectReservedInBooleanOut\":{\"objectReservedFields\":{\"boolean\":true,\"int\":12,\"float\":1.2,\"String\":\"STR\",\"enum\":\"CONST_1\"}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true,\"bool\":true}";
		assertEquals(out, expected);
	}

	public void test33NumNumInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test33NumNumInEmptyOut\":{\"integer1\":34,\"integer2\":56}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
		
	}

	public void test34EmptyInNumNumOut() throws MalformedURLException, IOException {
		String in 	= "{\"test34EmptyInNumNumOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"integer1\":1,\"integer2\":2,\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test35NumStringInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test35NumStringInEmptyOut\":{\"integer\":24,\"string\":\"KKK\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test36EmptyInNumStringOut() throws MalformedURLException, IOException {
		String in 	= "{\"test36EmptyInNumStringOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"integer\":1,\"string\":\"SS\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test37StringStringInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test37StringStringInEmptyOut\":{\"string1\":\"SS\",\"string2\":\"SS2\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test38EmptyInStringStringOut() throws MalformedURLException, IOException {
		String in 	= "{\"test38EmptyInStringStringOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"string2\":\"SS2\",\"string1\":\"SS1\",\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test39ObjectStringInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test39ObjectStringInEmptyOut\":{\"object\":{},\"string\":\"DD\"}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test40EmptyInObjectStringOut() throws MalformedURLException, IOException {
		String in 	= "{\"test40EmptyInObjectStringOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"string\":\"SSS\",\"statusFlag\":true,\"object\":{\"property1\":1}}";
		assertEquals(out, expected);
	}

	public void test41StringObjectInEmptyOut() throws MalformedURLException, IOException {
		String in 	= "{\"test39ObjectStringInEmptyOut\":{\"string\":\"DD\",\"object\":{}}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"statusFlag\":true}";
		assertEquals(out, expected);
	}

	public void test42EmptyInStringObjectOut() throws MalformedURLException, IOException {
		String in 	= "{\"test42EmptyInStringObjectOut\":{}}";
		System.out.println("IN: " + in);
		String out 		= postOnEndPoint(in);
		System.out.println("OUT: " + out);
		String expected 	= "{\"string\":\"DD\",\"statusFlag\":true,\"object\":{\"property1\":1}}";
		assertEquals(out, expected);
	}

}
