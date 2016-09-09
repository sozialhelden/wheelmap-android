package org.wheelmap.android.tango.renderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.RectangularPrism;
import org.rajawali3d.primitives.Sphere;
import org.wheelmap.android.tango.mode.TextureCache;

public class WheelmapRajawaliObjectFactory {

    private static final float MEASURE_POINT_RADIUS = 0.015f;

    private TextureCache textureCache = new TextureCache();

    public Sphere createMeasurePoint() {
        Sphere object = new Sphere(MEASURE_POINT_RADIUS, 50, 50);
        object.setMaterial(textureCache.get(TextureCache.MaterialType.CIRCLE));
        return object;
    }

    public Object3D createTextObject(String text) throws ATexture.TextureException {
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
        paint.setColor(Color.BLACK);
        canvas.drawText(text, 0, height - 10, paint);

        canvas.restore();

        Material material = new Material();
        material.setColor(Color.TRANSPARENT);
        material.addTexture(new Texture("dummy" + System.currentTimeMillis(), bitmap));

        Object3D textObject = new RectangularPrism(0.1f, 0.1f, 0);
        textObject.setMaterial(material);
        textObject.setTransparent(true);
        return textObject;
    }

}
