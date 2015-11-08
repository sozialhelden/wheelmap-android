package org.wheelmap.android.adapter;

/**
 * Created by SMF on 21/03/14.
 */
public class Item{
    public final String text;
    public final int icon;
    public Item(String text, Integer icon) {
        this.text = text;
        this.icon = icon;
    }
    @Override
    public String toString() {
        return text;
    }
}