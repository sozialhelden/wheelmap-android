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
package org.wheelmap.android.ui.info;

// titel with one clickable text
public class Info {
	// resources ids
	private int title;
	private int text;
	private int second_text;

	private String url;
	private String second_url;
	
	private  InfoTypes infotype;
	private  Class<?> cls = null;

	public Info( int title, int text, String url,  InfoTypes infotype) {
		this.title = title;
		this.text = text;
		this.second_text = -1;
		this.url = url;
		this.infotype = infotype;
	}
	
	public Info( int title, int text, int second_text, String url,  InfoTypes infotype) {
		this.title = title;
		this.text = text;
		this.second_text = second_text;
		this.url = url;
		this.infotype = infotype;		
	}
	
	public Info( int title, int text, String url, int second_text, String second_url,  InfoTypes infotype) {
		this.title = title;
		this.text = text;
		this.second_text = second_text;
		this.url = url;
		this.second_url = second_url;
		this.infotype = infotype;		
	}
	
	// Info with intent
	public Info( int title,  Class<?> cls,  InfoTypes infotype) {
		this.title = title;
		this.text = -1;
		this.second_text = -1;
		this.url = null; 
		this.cls = cls;
		this.infotype = infotype;		
	}

	public int getTitle() {
		return title;
	}
	
	public int getText() {
		return text;
	}
	
	public int getSecondText() {
		return second_text;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getSecondUrl() {
		return second_url;
	}
	
	public InfoTypes getInfoType() {
		return infotype;
	}
	
	public Class<?> getActivityClass() {
		return cls;
	}
}
