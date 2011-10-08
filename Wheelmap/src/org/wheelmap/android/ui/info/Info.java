package org.wheelmap.android.ui.info;

// titel with one clickable text
public class Info {
	// resources ids
	private int title;
	private int text;
	private String url;
	private  InfoTypes infotype;

	public Info( int title, int text, String url,  InfoTypes infotype) {
		this.title = title;
		this.text = text;
		this.url = url;
		this.infotype = infotype;
	}

	public int getTitle() {
		return title;
	}
	
	public int getText() {
		return text;
	}
	
	public String getUrl() {
		return url;
	}
	
	public InfoTypes getInfoType() {
		return infotype;
	}


}
