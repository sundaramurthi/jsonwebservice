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

import com.album.dispatcher.Album;
import com.album.dispatcher.ClientLoginFault;
import com.album.dispatcher.Crediential;
import com.album.dispatcher.EntryType;
import com.album.dispatcher.FeedType;
import com.album.dispatcher.LoginParameter;
import com.album.dispatcher.LoginResponse;
import com.album.dispatcher.Photo;
import com.album.dispatcher.UIElement;
import com.album.dispatcher.UIElements;
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

	/* (non-Javadoc)
	 * @see com.album.dispatcher.Album#clientLogin(org.w3._2005.atom.LoginParameter)
	 */
	@WebMethod (operationName="clientLogin")
	public LoginResponse clientLogin(@WebParam(name = "requestcontext", partName = "requestcontext") LoginParameter loginParameter)
			throws ClientLoginFault {
		if(loginParameter != null && loginParameter.getEmail() != null
				&& !loginParameter.getEmail().isEmpty()
				&& loginParameter.getPasswd() != null){
			LoginResponse respone = new LoginResponse();
			respone.setCaptchaToken("DUMMY");
			respone.setCaptchaUrl("http://dummp.url");
			return respone;
			
		}
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
	
	/**
     * 
     * @param albumId
     * @param photoId
     * @return
     *     returns org.w3._2005.atom.Photo
     */
    @WebMethod
    @WebResult(name = "photo", partName = "photo")
    public Photo getPhotoById(
        @WebParam(name = "albumId", partName = "albumId")
        int albumId,
        @WebParam(name = "photoId", partName = "photoId")
        int photoId){
    	Photo photo = new Photo();
    	photo.setHeight(photoId);
    	photo.setWidth(albumId);
    	return photo;
    }
    
	/* (non-Javadoc)
	 * @see com.album.dispatcher.Album#getAlbumById(int)
	 */
	public EntryType getAlbumById(int albumId) {
		EntryType type = new EntryType();
		type.setId(String.valueOf(albumId));
		return type;
	}
	
	/* (non-Javadoc)
	 * @see com.album.dispatcher.Album#getAlbumByName(java.lang.String)
	 */
	public EntryType getAlbumByName(String albumName) {
		EntryType type = new EntryType();
		type.setTitle(albumName);
		return type;
	}
	
	/* (non-Javadoc)
	 * @see com.album.dispatcher.Album#getAlbumIDByName(java.lang.String)
	 */
	public int getAlbumIDByName(String albumName) {
		return albumName != null ? 1 : 0;
	}
	/* (non-Javadoc)
	 * @see com.album.dispatcher.Album#getAlbumVersion(int)
	 */
	public double getAlbumVersion(int albumId) {
		return albumId > 0 ? 1.1 : 0.0;
	}
	/* (non-Javadoc)
	 * @see com.album.dispatcher.Album#listPhotosByAlbumObject(org.w3._2005.atom.Crediential, org.w3._2005.atom.EntryType)
	 */
	public FeedType listPhotosByAlbumObject(Crediential crediential,
			EntryType album) {
		FeedType ft = new FeedType();
		ft.getEntry().add(album);
		EntryType credEntry = new EntryType();
		credEntry.setSummary(crediential.getToken());
		ft.getEntry().add(credEntry );
		return ft;
	}
	/* (non-Javadoc)
	 * @see com.album.dispatcher.Album#updatePhoto(org.w3._2005.atom.Photo)
	 */
	public void updatePhoto(Photo photo) {
		if(photo == null || photo.getChecksum() == null){
			throw new RuntimeException("Invalid input");
		}
		
	}
}
