package wheelmap.org;

import java.util.HashMap;
import java.util.Map;

public enum WheelchairState {
  UNKNOWN(0), YES(1), LIMITED(2), NO(3), NO_PREFERENCE(4);
  
  	public static final WheelchairState DEFAULT;
  
  	private final int id;
	private static Map<Integer,WheelchairState> id2State;
	
	
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
	
	private void register() {
		if (id2State==null) {
			id2State= new HashMap<Integer, WheelchairState>();
		}
		id2State.put(id, this);		
	}	
  
	public String asRequestParameter() {
		return this.name().toLowerCase();
	}
	
	static {
		DEFAULT = YES;
	}
}
