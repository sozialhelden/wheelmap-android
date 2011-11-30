/*
Copyright (C) 2011 Michal Harakal and Michael Kroez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package org.wheelmap.android.test;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;

public class MyInstrumentationTestRunner extends InstrumentationTestRunner {

	public TestSuite getAllTests() {
		TestSuite suite = new TestSuite(MyInstrumentationTestRunner.class.getName());
		// $JUnit-BEGIN$
//		suite.addTestSuite( GeocoordinatesMathTest.class );
//		suite.addTestSuite( ModelTest.class );
//		suite.addTestSuite( MapFileInfoProviderTest.class );
//		suite.addTestSuite( MapFileServiceTest.class );
//		suite.addTestSuite( TestPOIContentProvider.class );
//		suite.addTestSuite( SupportDataTest.class );
//		suite.addTestSuite( LoginTest.class );
		
		// $JUnit-END$
		return suite;
	}

	
}
