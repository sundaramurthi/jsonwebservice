import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import com.album.dispatcher.Album;
import com.album.dispatcher.AlbumService;
import com.album.dispatcher.UIElement;
import com.album.dispatcher.UIElements;


public class ListMapKeyTest {
	public static void main(String[] args) throws MalformedURLException {
		AlbumService albm = new AlbumService(new URL("http://localhost:8080/album/json/picasa?wsdl"), new QName("http://album.com/dispatcher", "AlbumService"));
		Album port = albm.getAlbumServicePort();
		UIElements uiElementsProxy = new UIElements();
		UIElement elm1    = new UIElement();
		elm1.setName("LOGIN");
		uiElementsProxy.getElements().add(elm1    );
		UIElement elm2= new UIElement();
		elm2.setName("PASS");
		uiElementsProxy.getElements().add(elm2    );
		port.getUIElements(uiElementsProxy );
	}
}
