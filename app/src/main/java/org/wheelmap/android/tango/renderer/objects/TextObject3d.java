package org.wheelmap.android.tango.renderer.objects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.RectangularPrism;

public class TextObject3d extends RectangularPrism {

    private String text;
    private int textColor;

    public TextObject3d(String text, int textColor, float width, float height) throws ATexture.TextureException {
        super(width, height, 0);
        this.text = text;
        this.textColor = textColor;
        setTransparent(true);
        setMaterial(createMaterial());
    }

    private Material createMaterial() throws ATexture.TextureException {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(80);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int height = bounds.height() + 20;
        int width = bounds.width() + 20;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        paint.setColor(Color.TRANSPARENT);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, paint);

        canvas.save();
        canvas.rotate(180, width / 2, height / 2);
        paint.setColor(textColor);
        canvas.drawText(text, 0, height - 10, paint);

        canvas.restore();

        Material material = new Material();
        material.setColor(Color.TRANSPARENT);
        material.addTexture(new Texture("dummy" + System.currentTimeMillis(), bitmap));
        return material;
    }
}
