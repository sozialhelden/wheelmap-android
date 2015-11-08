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
package org.wheelmap.android.modules;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.BuildConfig;
import org.wheelmap.android.utils.UtilsMisc;

import android.app.Application;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Provides to lookup resources which are need for basic usage like the discovery uri.
 */

@Singleton
public class AppProperties implements IAppProperties {

    private static final String LOG_TAG = AppProperties.class.getSimpleName();

    private Properties properties;

    private static AppProperties instance;

    public static AppProperties getInstance(Application app){
       if(instance == null){
          synchronized (AppProperties.class){
              if(instance == null){
                    instance = new AppProperties(app);
              }
          }
       }
       return instance;
    }

    @Inject
    public AppProperties(Provider<Application> applicationProvider) {
        final BufferedInputStream stream;

        {
            final Application app = applicationProvider.get();
            try {
                stream = new BufferedInputStream(app.getAssets().open(
                        Extra.APP_PROPERTIES_ASSETS_FILE_NAME));
                try {
                    properties = new Properties();
                    properties.load(stream);
                } finally {
                    UtilsMisc.closeSilently(stream);
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "exception by instatiating of app properties" + e);
            }
        }
        instance = this;
    }

    public AppProperties(Application app){
        final BufferedInputStream stream;

        {
            try {
                stream = new BufferedInputStream(app.getAssets().open(
                        Extra.APP_PROPERTIES_ASSETS_FILE_NAME));
                try {
                    properties = new Properties();
                    properties.load(stream);
                } finally {
                    UtilsMisc.closeSilently(stream);
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "exception by instatiating of app properties" + e);
            }
        }
        instance = this;
    }


    /**
     * Returns an {@link String} value for the lookup key.
     */
    public String get(final String key) {
        if (key == null) {
            return null;
        }

        switch (key) {
            case IAppProperties.KEY_HOCKEY_URI:
                return BuildConfig.HOCKEY_APP_ID;
            case IAppProperties.KEY_WHEELMAP_URI:
                return BuildConfig.API_BASE_URL;
        }

        if (properties == null) {
            Log.w(LOG_TAG, "properties are not initialized - returning null");
            return null;
        }

        final Object value = properties.get(key);
        if (value == null) {
            Log.w(LOG_TAG, "key " + key + " not found - returning null");
            return null;
        } else {
            return value.toString();
        }

    }
}
