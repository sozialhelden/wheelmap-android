package org.wheelmap.android.test;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;

public class MyInstrumentationTestRunner extends InstrumentationTestRunner {

	public TestSuite getAllTests() {
		TestSuite suite = new TestSuite(MyInstrumentationTestRunner.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite( ModelTest.class );
//		suite.addTestSuite( GeocoordinatesMathTest.class );
		// $JUnit-END$
		return suite;
	}

	
}
