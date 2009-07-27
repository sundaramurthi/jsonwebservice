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
import org.w3._2005.atom.UIElement;
import org.w3._2005.atom.UIElements;

import com.album.dispatcher.Album;
import com.album.dispatcher.ClientLoginFault;
import com.jaxws.json.feature.JSONWebService;

/**
 * @author Sundaramurthi
 * @version 0.1
 */
@WebService (name="AlbumService",
		serviceName="AlbumService",
		targetNamespace="http://album.com/dispatcher",
		portName="AlbumServicePort",
		wsdlLocation="WEB-INF/wsdl/picasa.wsdl",
		endpointInterface="com.album.dispatcher.Album")
public class AlbumImpl implements Album{
	
	static 	JAXBContext		context;
	
	static{
		try {
			context = JAXBContext.newInstance(FeedType.class);
		} catch (JAXBException e) {
			//TODO log
		}
	}
	/**
     * 
     * @param uiElementsProxy
     * @return
     *     returns org.w3._2005.atom.UIElements
     */
    @WebMethod(action = "http://code.google.com/p/jsonwebservice/album/getUIElements")
    @WebResult(name = "uiElements", partName = "uiElements")
    @JSONWebService(listMapKey = "Name",listMapValue = "Value")
    public UIElements getUIElements(
        @WebParam(name = "uiElementsProxy", partName = "uiElementsProxy")
        UIElements uiElementsProxy){
    	for(UIElement elm :uiElementsProxy.getElements()){
    		if(elm.getName().equals("LOGIN")){
    			elm.setValue("Login");
    		}else{
    			elm.setValue("Password");
    		}
    	}
    	return uiElementsProxy;
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

	@WebMethod (operationName="listPhotos")
	public @WebResult(name="photos") FeedType listPhotos(
	        @WebParam(name = "crediential", partName = "crediential")
	        Crediential crediential,
	        @WebParam(name = "albumUrl", partName = "albumUrl")
	        String albumUrl) {
		try {
			URL 			albumURL 	= new URL(albumUrl);
			Unmarshaller 	um 			= context.createUnmarshaller();
			FeedType ob = um.unmarshal(new StreamSource(albumURL.openStream()),FeedType.class).getValue();
			return ob;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Album request failed",e);
		}
	}
}
