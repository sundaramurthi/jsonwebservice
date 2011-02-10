package test;

import java.io.IOException;
import java.net.MalformedURLException;


public class PerformanceTest extends JSONCodecTest/* implements
		com.test.jsonwebservice.rpc.PerformanceTest*/ {

	protected void setUp() throws Exception {
		END_POINT = "http://localhost:8080/unitTest/json/performance";
		super.setUp();
		// make sure objects cached in server
		String in 	= "{\"test1SizeInLargeListOut\":{\"integer\":100}}";
		postOnEndPoint(in);
		postOnEndPoint(in);
		postOnEndPoint(in);
	}
	int mode = 2;
	public void test1SizeInLargeListOut() throws MalformedURLException, IOException {
		String in 	= "{\"test1SizeInLargeListOut\":{\"integer\":100}}";
		System.out.println("IN: " + in);
		long request = System.currentTimeMillis();
		String out = postOnEndPoint(in);
		long response = System.currentTimeMillis();
		System.out.println("OUT: " + out);
		long total = response - request;
		System.out.println("Time : " + total/1000.0 + "Sec");
		System.out.println("Size : " +(out.getBytes().length / 1024) + " KB");
		assertEquals(true,total < (50 * mode));
		
		in 	= "{\"test1SizeInLargeListOut\":{\"integer\":1000}}";
		System.out.println("IN: " + in);
		request = System.currentTimeMillis();
		out = postOnEndPoint(in);
		response = System.currentTimeMillis();
		total = response - request;
		System.out.println("Time : " + total/1000.0 + "Sec");
		System.out.println("Size : " + (out.getBytes().length / 1024) + " KB");
		assertEquals(true,total < (500 * mode));
		
		in 	= "{\"test1SizeInLargeListOut\":{\"integer\":10000}}";
		System.out.println("IN: " + in);
		request = System.currentTimeMillis();
		out = postOnEndPoint(in);
		response = System.currentTimeMillis();
		total = response - request;
		System.out.println("Time : " + total/1000.0 + "Sec");
		System.out.println("Size : " +((out.getBytes().length / 1024)/ 1024) + " MB");
		assertEquals(true,total < (4000 * mode));
		
		/*in 	= "{\"test1SizeInLargeListOut\":{\"integer\":10000}}";
		System.out.println("IN: " + in);
		request = System.currentTimeMillis();
		out = postOnEndPoint(in);
		//System.out.println("OUT: " + out);
		response = System.currentTimeMillis();
		total = response - request;
		System.out.println("Time : " + total/1000.0 + "Sec");
		System.out.println("Size : " +((out.getBytes().length / 1024)/ 1024) + " MB");
		assertEquals(true,total < (40000 * mode));*/
	}

	public void test2LargeListInSizeOut() throws MalformedURLException, IOException {
		String in 	= "{\"test1SizeInLargeListOut\":{\"integer\":100}}";
		String out = postOnEndPoint(in);
		
		in 	= "{\"test2LargeListInSizeOut\":" + out + "}";
		System.out.println("IN: " + in);
		long request = System.currentTimeMillis();
		out = postOnEndPoint(in);
		long response = System.currentTimeMillis();
		System.out.println("OUT: " + out);
		long total = response - request;
		System.out.println("Time : " + total/1000.0 + "Sec");
		System.out.println("Size : " +(out.getBytes().length / 1024) + " KB");
		assertEquals(true,total < (5000 * mode));
		
		in 	= "{\"test1SizeInLargeListOut\":{\"integer\":1000}}";
		out = postOnEndPoint(in);
		
		in 	= "{\"test2LargeListInSizeOut\":" + out + "}";
		request = System.currentTimeMillis();
		out = postOnEndPoint(in);
		response = System.currentTimeMillis();
		System.out.println("OUT: " + out);
		total = response - request;
		System.out.println("Time : " + total/1000.0 + "Sec");
		System.out.println("Size : " +(out.getBytes().length / 1024) + " KB");
		assertEquals(true,total < (50000 * mode));
	}
}
