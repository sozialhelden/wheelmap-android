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

package wheelmap.org;

public class Locale {
	public final static Locale DEFAULT_LOCALE = new Locale("de");
	
	private String value;
	
	public Locale( String value ) {
		this.value = value;
	}

	public String asRequestParameter() {
		return value;
	}
}
