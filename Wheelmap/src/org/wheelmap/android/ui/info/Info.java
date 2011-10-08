package org.wheelmap.android.ui.info;

// titel with one clickable text
public class Info {
	// resources ids
	private int title;
	private int text;
	private int link;
	private  InfoTypes infotype;

	public Info( int title, int text, int link,  InfoTypes infotype) {
		this.title = title;
		this.text = text;
		this.link = link;
		this.infotype = infotype;
	}

	public int getTitle() {
		return title;
	}
	
	public int getText() {
		return text;
	}
	
	public InfoTypes getInfoType() {
		return infotype;
	}


}
