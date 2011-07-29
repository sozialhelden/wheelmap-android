package wheelmap.org.domain.categories;

import java.util.HashMap;
import java.util.Map;

public enum Category {
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
	private static Map<Integer,Category> id2Category;
	
	private Category(int id) {
		this.id = id;
		register();
	}

	public int getId() {
		return id;
	}
	
	public static Category valueOf(int id) {
		return id2Category.get(id);
	}
	
	private void register() {
		if (id2Category==null) {
			id2Category= new HashMap<Integer, Category>();
		}
		id2Category.put(id, this);		
	}	
}
