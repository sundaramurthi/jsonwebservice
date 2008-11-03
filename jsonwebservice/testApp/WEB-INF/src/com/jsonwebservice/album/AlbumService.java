package com.jsonwebservice.album;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * @author Sundaramurthi
 * @version 0.1
 */
@WebService (name="AlbumService", targetNamespace="http://album.jsonplugin.com/json/")
public class AlbumService {
	
	@WebMethod (operationName="listAlbums")
	public @WebResult(name="albums") List<String> listAlbums(){
		List<String> albums = new ArrayList<String>();
		albums.add("Albim 1");
		albums.add("Albim 2");
		albums.add("Albim 3");
		return albums;
	}
}
