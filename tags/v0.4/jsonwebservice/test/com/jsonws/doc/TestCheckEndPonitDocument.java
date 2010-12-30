package com.jsonws.doc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.jsonws.JSONCodecTest;

public class TestCheckEndPonitDocument extends JSONCodecTest{
	
	public void testEndpointDocument() throws MalformedURLException, IOException{
		HttpURLConnection connection = (HttpURLConnection) new URL(END_POINT).openConnection();
		assertEquals(connection.getResponseCode(), 200);
		
		connection = (HttpURLConnection) new URL(END_POINT + "?config").openConnection();
		assertEquals(connection.getResponseCode(), 200);
	}
}
