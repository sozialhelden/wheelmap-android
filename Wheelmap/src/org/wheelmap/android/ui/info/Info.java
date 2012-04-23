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
