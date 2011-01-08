package test;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.jaxws.json.codec.decode.JSONReader;
import com.jaxws.json.codec.decode.WSJSONPopulator;

public class WSJSONPopulatorTest extends TestCase {

	public void testPopulatePrimitive() throws Exception{
		
		WSJSONPopulator populator = new WSJSONPopulator(null, null, null, null, null);
		JSONReader r = new JSONReader();
		TestClass object = new TestClass();
		populator.populateObject(object, (Map) r.read("{\"name\":\"nm\"}"), null, null);
		assertEquals(object.name, "nm");
		assertEquals(object.values, null);
		
		populator.populateObject(object, (Map) r.read("{\"integer\":\"24\"}"), null, null);
		assertEquals(object.integer, 24);
		assertEquals(object.values, null);
	}
	
	public void testList() throws Exception{
		
		WSJSONPopulator populator = new WSJSONPopulator(null, null, null, null, null);
		JSONReader r = new JSONReader();
		TestClass object = new TestClass();
		populator.populateObject(object, (Map) r.read("{\"values\":[\"nm\"]}"), null, null);
		assertEquals(object.name, null);
		assertEquals(object.values.size(), 1);
		assertEquals(object.values.get(0), "nm");
		
		// empty list
		populator.populateObject(object, (Map) r.read("{\"values\":[]}"), null, null);
		assertNotNull(object.values);
		assertEquals(object.values.size(), 0);
	}
	
	// Test date plain format with date as number / date as string
	
	
	public static class TestClass{
		private String name;
		private int integer;
		
		private List<String> values;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<String> getValues() {
			return values;
		}
		public void setValues(List<String> values) {
			this.values = values;
		}
		public int getInteger() {
			return integer;
		}
		public void setInteger(int integer) {
			this.integer = integer;
		}
	}
}

