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
package wheelmap.org;

import java.util.HashMap;
import java.util.Map;

public enum WheelchairState {
  UNKNOWN(0), YES(1), LIMITED(2), NO(3), NO_PREFERENCE(4);
  
  	public static final WheelchairState DEFAULT;
  
  	private final int id;
	private static Map<Integer,WheelchairState> id2State;
	private static Map<String, WheelchairState> string2State;
	
	
	private WheelchairState(int id) {
		this.id = id;
		register();
	}

	public int getId() {
		return id;
	}
	
	public static WheelchairState valueOf(int id) {
		return id2State.get(id);
	}
	
	public static WheelchairState myValueOf( String string ) {
		return string2State.get( string.toLowerCase() );
	}
	
	private void register() {
		if (id2State==null) {
			id2State= new HashMap<Integer, WheelchairState>();
		}
		
		id2State.put(id, this);		
		
		if ( string2State == null )
			string2State = new HashMap<String, WheelchairState>();

		string2State.put( this.toString().toLowerCase(), this );
	}	
  
	public String asRequestParameter() {
		if ( this == NO_PREFERENCE )
			return "";
		
		return this.name().toLowerCase();
	}
	
	static {
		DEFAULT = NO_PREFERENCE;
	}
}
