/*
 * #%L
 * Wheelmap-it - Integration tests
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wheelmap.android.test;

import junit.framework.TestSuite;

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.utils.UtilsMisc;

import android.test.InstrumentationTestRunner;

public class MyInstrumentationTestRunner extends InstrumentationTestRunner {

	public TestSuite getAllTests() {

		TestSuite suite = new TestSuite(
				MyInstrumentationTestRunner.class.getName());

		suite.addTestSuite(GeocoordinatesMathTest.class);
		suite.addTestSuite(LoginTest.class);
		suite.addTestSuite(POIContentProviderTest.class);
		suite.addTestSuite(POIServiceDatabaseTest.class);

		suite.addTestSuite(SupportDataTest.class);
		if (UtilsMisc.isTablet(getContext())) {
			suite.addTestSuite(MainMultiPaneTest.class);
		} else {
			suite.addTestSuite(MainSinglePaneTest.class);
		}
		return suite;
	}

}
