package org.wheelmap.android.mapping.node;

/**
 * Created by torstenlemm on 13.03.14.
 */
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import android.widget.ImageView;

import java.math.BigDecimal;
import java.math.BigInteger;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {

    @JsonProperty(value = "type")
    protected String type;

    @JsonProperty(value = "url")
    protected String url;

    @JsonProperty(value = "width")
    protected int width;

    @JsonProperty(value = "height")
    protected int height;


    //protected ImageView image;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /*public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }*/


    @Override
    public String toString() {
        return "Image [type=" + type + ", url= {" + url + "}, width="
                + width + ", height=" + height + "]";
    }


}
