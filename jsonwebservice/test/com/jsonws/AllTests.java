package com.jsonws;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.jsonws.codec.CodecTestDefault;
import com.jsonws.codec.ParameterTest;
import com.jsonws.doc.TestCheckEndPonitDocument;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.jsonws");
		//$JUnit-BEGIN$
		
		suite.addTestSuite(CodecTestDefault.class);
		suite.addTestSuite(ParameterTest.class);
		
		suite.addTestSuite(TestCheckEndPonitDocument.class);
		//$JUnit-END$
		return suite;
	}

}
