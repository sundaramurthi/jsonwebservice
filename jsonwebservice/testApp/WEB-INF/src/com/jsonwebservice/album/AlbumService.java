package com.jsonwebservice.album;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.w3._2005.atom.Crediential;
import org.w3._2005.atom.FeedType;
import org.w3._2005.atom.LoginParameter;
import org.w3._2005.atom.LoginResponse;

import com.album.dispatcher.Album;
import com.album.dispatcher.ClientLoginFault;

/**
 * @author Sundaramurthi
 * @version 0.1
 */
@WebService (name="AlbumService", targetNamespace="http://album.jsonplugin.com/json/")
public class AlbumService implements Album{
	
	static 	JAXBContext		context;
	
	static{
		try {
			context = JAXBContext.newInstance(FeedType.class);
		} catch (JAXBException e) {
			//TODO log
		}
	}
	
	/**
	 * Hard code test methid
	 * @return
	 */
	@WebMethod (operationName="listAlbums2")
	public List<String> listAlbums2(){
		List<String> albums = new ArrayList<String>();
		albums.add("Albim 1");
		albums.add("Albim 2");
		albums.add("Albim 3");
		return albums;
	}

	public LoginResponse clientLogin(LoginParameter requestcontext)
			throws ClientLoginFault {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@WebMethod (operationName="listAlbums")
	public @WebResult(name="albums") FeedType listAlbums(@WebParam(name = "crediential") Crediential crediential) {
		try {
			//TODO from JNDI property
			URL 			albumURL 	= new URL("http://picasaweb.google.com/data/feed/api/user/"+crediential.getUsername()+"?kind=album");
			Unmarshaller 	um 			= context.createUnmarshaller();
			FeedType ob = um.unmarshal(new StreamSource(albumURL.openStream()),FeedType.class).getValue();
			return ob;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Album request failed",e);
		}
	}
}
