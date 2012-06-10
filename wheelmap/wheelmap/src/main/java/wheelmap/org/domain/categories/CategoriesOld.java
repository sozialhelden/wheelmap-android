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
package wheelmap.org.domain.categories;

import java.util.HashMap;
import java.util.Map;

public enum CategoriesOld {
	PUBLIC_TRANSFER(1),
	FOOD(2),
	LEISURE(3),
	MONEY_POST(4),
	EDUCATION(5),
	SHOPPING(6),
	SPORT(7),
	TOURISM(8),
	ACCOMMODATION(9),
	MISC(10),
	GOVERNMENT(11);
	
	private final int id;
	private static Map<Integer,CategoriesOld> id2Category;
	
	private CategoriesOld(int id) {
		this.id = id;
		register();
	}

	public int getId() {
		return id;
	}
	
	public static CategoriesOld valueOf(int id) {
		return id2Category.get(id);
	}
	
	private void register() {
		if (id2Category==null) {
			id2Category= new HashMap<Integer, CategoriesOld>();
		}
		id2Category.put(id, this);		
	}	
}
