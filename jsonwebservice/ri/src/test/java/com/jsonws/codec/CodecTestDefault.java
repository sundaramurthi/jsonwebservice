package com.jsonws.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.jaxws.json.codec.encode.WSJSONWriter;
import com.jsonws.JSONCodecTest;

public class CodecTestDefault extends JSONCodecTest {
	
	public void testCodecValidJSONStringInput() throws MalformedURLException, IOException{
		String getUiElements 	= "{\"clientLogin\":{\"requestcontext\":{\"email\":\"test@tt.com\",\"passwd\":\"xx\"}}}";
		System.out.println("IN: " + getUiElements);
		String response 		= postOnEndPoint(getUiElements);
		System.out.println("OUT: " + response);
		String expectedRespone 	= "{\"statusFlag\":true,\"requestcontext\":{\"CaptchaToken\":\"DUMMY\",\"CaptchaUrl\":\"http:\\/\\/dummp.url\"}}";
		//assertEquals(response, expectedRespone);
	}
	
	public void testCodecValidMapObjectInput() throws MalformedURLException, IOException{
		Map<String,Object> 	logInfo		= new HashMap<String,Object>();
		Map<String,Object> 	requestcontext 	= new HashMap<String,Object>();
		requestcontext.put("email", "test@tt.com");
		requestcontext.put("passwd", "xx");
		logInfo.put("requestcontext", requestcontext );
		Map<String,Object> 	clientLogin		= new HashMap<String,Object>();
		clientLogin.put("clientLogin", logInfo );
		ByteArrayOutputStream  JSON 		= new ByteArrayOutputStream();
		new WSJSONWriter(JSON, clientLogin, null).write(null, null, null, null, null);
		String getUiElements 	= JSON.toString();
		System.out.println("IN: " + getUiElements);
		String response 		= postOnEndPoint(getUiElements);
		System.out.println("OUT: " + response);
		String expectedRespone 	= "{\"statusFlag\":true,\"requestcontext\":{\"CaptchaToken\":\"DUMMY\",\"CaptchaUrl\":\"http:\\/\\/dummp.url\"}}";
		//assertEquals(response, expectedRespone);
	}
	
	public void testCodecJSONListNoMAPStringInput() throws MalformedURLException, IOException{
		String getUiElements 	= "{\"getUIElements\":{\"uiElementsProxy\":{\"elements\":[{\"name\":\"LOGIN\"}]}}}";
		String response 		= postOnEndPoint(getUiElements);
		System.out.println(response);
		String expectedRespone 	= "{\"uiElements\":{\"elements\":[{\"name\":\"LOGIN\",\"value\":\"Login\"}]},\"statusFlag\":true}";
		//assertEquals(response, expectedRespone);
	}

	
}
