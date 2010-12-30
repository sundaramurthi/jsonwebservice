package test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.jsonws");
		//$JUnit-BEGIN$
		suite.addTestSuite(ParameterTest.class);
		suite.addTestSuite(CustomizeTest.class);
		suite.addTestSuite(JAXBAnnotationTest.class);
		suite.addTestSuite(AttachmentTest.class);
		//$JUnit-END$
		return suite;
	}

}
