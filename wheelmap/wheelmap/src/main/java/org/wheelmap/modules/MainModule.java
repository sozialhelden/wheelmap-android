/*
 * #%L
 * Wheelmap - App
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
package org.wheelmap.modules;

import org.wheelmap.android.app.AppProperties;
import org.wheelmap.android.app.IAppProperties;
import org.wheelmap.request.IHttpUserAgent;

import com.google.inject.AbstractModule;

public final class MainModule extends AbstractModule
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure()
	{
		bind(IAppProperties.class).to(AppProperties.class).asEagerSingleton();
		bind(IHttpUserAgent.class).to(HttpUserAgent.class).asEagerSingleton();;
		
	}
}
