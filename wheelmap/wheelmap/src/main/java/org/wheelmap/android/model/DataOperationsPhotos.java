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
package org.wheelmap.android.model;

import org.wheelmap.android.mapping.node.Image;
import org.wheelmap.android.mapping.node.Photo;
import org.wheelmap.android.mapping.node.Photos;
import org.wheelmap.android.model.Wheelmap.POIs;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import java.net.URL;

import de.akquinet.android.androlog.Log;

public class DataOperationsPhotos extends DataOperations<Photos, Photo> {

    public DataOperationsPhotos(ContentResolver resolver) {
        super(resolver);
    }

    @Override
    protected Photo getItem(Photos items, int i) {
        return items.getPhotos().get(i);
    }

    @Override
    public void copyToValues(Photo photo, ContentValues values) {
        values.clear();
        values.put(POIs.PHOTO_ID, photo.getId().longValue());

        values.put(POIs.TAKEN_ON, photo.getTakenOn().longValue());


        // ADD LOOP HERE: for images
        //copyImagesToValues()


       /*
        // TEST LOADING IMAGES FROM WEB ----------------------------
        for(int i = 0; i < photo.getImages().size(); i++){
            try{
                Image image = photo.getImages().get(i);
                URL newurl = new URL(image.getUrl());
                Object obj = BitmapFactory
                    .decodeStream(newurl.openConnection().getInputStream());
                image.setImage((ImageView)obj);

            }catch(Exception ex){
                Log.d(ex.getMessage());
            }
        }  */


        // TEST LOADING IMAGES FROM WEB ----------------------------



        values.put(POIs.TAG, POIs.TAG_RETRIEVED);
    }
     /*
    public void copyImagesToValues(Image image, ContentValues values){

    }   */



    @Override
    protected Uri getUri() {
        return POIs.createNoNotify(POIs.CONTENT_URI_RETRIEVED);
    }
}

