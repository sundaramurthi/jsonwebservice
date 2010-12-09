package com.jsonws.codec;

import java.io.IOException;
import java.net.MalformedURLException;

import com.jsonws.JSONCodecTest;

/**
 * @author ssaminathan
 *
 */
public class ParameterTest extends JSONCodecTest {
	/**
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void testOnePrimitiveReqIntegerParameter() throws MalformedURLException, IOException{
		// Test valid integer success
		String getUiElements 	= "{\"getAlbumById\":{\"albumId\":1}}";
		System.out.println("IN: " + getUiElements);
		String response 		= postOnEndPoint(getUiElements);
		System.out.println("OUT: " + response);
		String expectedRespone 	= "{\"entryType\":{\"id\":\"1\",\"link\":[]},\"statusFlag\":true}";
		assertEquals(response, expectedRespone);
		
		// Test valid integer string success
		getUiElements 	= "{\"getAlbumById\":{\"albumId\":\"1\"}}";
		System.out.println("IN: " + getUiElements);
		response 		= postOnEndPoint(getUiElements);
		System.out.println("OUT: " + response);
		expectedRespone 	= "{\"entryType\":{\"id\":\"1\",\"link\":[]},\"statusFlag\":true}";
		assertEquals(response, expectedRespone);
	}
	
	/**
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void testOnePrimitiveReqStringParameter() throws MalformedURLException, IOException{
		String getUiElements 	= "{\"getAlbumByName\":{\"albumName\":\"test\"}}";
		System.out.println("IN: " + getUiElements);
		String response 		= postOnEndPoint(getUiElements);
		System.out.println("OUT: " + response);
		String expectedRespone 	= "{\"entryType\":{\"link\":[],\"title\":\"test\"},\"statusFlag\":true}";
		assertEquals(response, expectedRespone);
	}
	
	/**
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void testOneObjectReqParameter() throws MalformedURLException, IOException{
		String getUiElements 	= "{\"clientLogin\":{\"requestcontext\":{\"email\":\"test@tt.com\",\"passwd\":\"xx\"}}}";
		System.out.println("IN: " + getUiElements);
		String response 		= postOnEndPoint(getUiElements);
		System.out.println("OUT: " + response);
		String expectedRespone 	= "{\"statusFlag\":true,\"requestcontext\":{\"CaptchaToken\":\"DUMMY\",\"CaptchaUrl\":\"http:\\/\\/dummp.url\"}}";
		assertEquals(response, expectedRespone);
	}
	
	/**
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void testTwoObjectReqParameter() throws MalformedURLException, IOException{
		String getUiElements 	= "{\"listPhotosByAlbumObject\":{\"crediential\":{\"token\":\"xxxx\",\"username\":\"xx\"}," +
				"\"album\":{\"id\":\"1\",\"title\":\"title\"}}}}";
		System.out.println("IN: " + getUiElements);
		String response 		= postOnEndPoint(getUiElements);
		System.out.println("OUT: " + response);
		String expectedRespone 	= "{\"response\":{\"entry\":[{\"id\":\"1\",\"link\":[],\"title\":\"title\"},{\"link\":[],\"summary\":\"xxxx\"}]},\"statusFlag\":true}";
		assertEquals(response, expectedRespone);
	}
	
	
	
	/**
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void testTwoPrimitiveIntegerReqParameter() throws MalformedURLException, IOException{
		String photoId 	= "{\"getPhotoById\":{\"albumId\":123,\"photoId\":1}}";
		System.out.println("IN: " + photoId);
		String response 		= postOnEndPoint(photoId);
		System.out.println("OUT: " + response);
		String expectedRespone 	= "{\"statusFlag\":true,\"photo\":{\"commentCount\":0,\"height\":1,\"rotation\":0,\"size\":0,\"timestamp\":0,\"width\":123}}";
		assertEquals(response, expectedRespone);
	}
	
	/**
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void testEmptyResParameter() throws MalformedURLException, IOException{
		String photoId 	= "{\"updatePhoto\":{\"photo\":{\"checksum\":\"xx\"}}}";
		System.out.println("IN: " + photoId);
		String response 		= postOnEndPoint(photoId);
		System.out.println("OUT: " + response);
		String expectedRespone 	= "{\"statusFlag\":true}";
		assertEquals(response, expectedRespone);
	}
	
	/**
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void testOneIntegerResParameter() throws MalformedURLException, IOException{
		String photoId 	= "{\"getAlbumIDByName\":{\"albumName\":\"xx\"}}";
		System.out.println("IN: " + photoId);
		String response 		= postOnEndPoint(photoId);
		System.out.println("OUT: " + response);
		String expectedRespone 	= "{\"albumID\":1,\"statusFlag\":true}";
		assertEquals(response, expectedRespone);
	}
}
