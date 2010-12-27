package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

public class JSONCodecTest extends TestCase {
	public static String END_POINT = "http://localhost:8080/unitTest/json/parameter";
	
	protected String postOnEndPoint(String postBody) throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(END_POINT).openConnection();
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setDoOutput(true);
		connection.getOutputStream().write(postBody.getBytes());
		BufferedReader bufReader = new BufferedReader(new InputStreamReader(connection.getResponseCode() < 300 ? connection.getInputStream() :
			connection.getErrorStream()));
		StringBuffer buf = new StringBuffer();
		String str = bufReader.readLine();
		while(str != null){
			buf.append(str);
			str = bufReader.readLine();
		}
		return buf.toString();
	}
}
