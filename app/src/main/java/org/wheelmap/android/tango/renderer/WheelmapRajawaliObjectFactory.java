package org.wheelmap.android.tango.renderer;

import android.graphics.Color;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Sphere;
import org.wheelmap.android.tango.renderer.objects.Polygon;
import org.wheelmap.android.tango.renderer.objects.TextObject3d;

import java.util.Stack;

public class WheelmapRajawaliObjectFactory {

    private static final float MEASURE_POINT_RADIUS = 0.01f;

    public static final int TEXT_COLOR = Color.YELLOW;
    public static final int ELEMENT_COLOR = Color.YELLOW;


    private TextureCache textureCache = new TextureCache();

    public Sphere createMeasurePoint() {
        Sphere object = new Sphere(MEASURE_POINT_RADIUS, 50, 50);
        object.setMaterial(textureCache.get(TextureCache.MaterialType.CIRCLE));
        return object;
    }

    public TextObject3d createTextObject(String text) throws ATexture.TextureException {
        return new TextObject3d(text, TEXT_COLOR, 0.1f, 0.1f);
    }

    public void measureLineBetween(WheelmapModeRenderer.Manipulator m, Vector3 first, Vector3 second, String text) {
        measureLineBetween(m, first, second, text, Vector3.Z);
    }

    public void measureLineBetween(WheelmapModeRenderer.Manipulator m, Vector3 first, Vector3 second, String text, Vector3 axis) {
        Stack<Vector3> linePoints = new Stack<>();
        linePoints.add(first);
        linePoints.add(second);

        Line3D line = new Line3D(linePoints, 50);
        line.setMaterial(getTextureCache().get(TextureCache.MaterialType.LINE));
        m.addObject(line);
        try {

            //calculate normal vector
            Vector3 n = linePoints.get(0).clone()
                    .subtract(linePoints.get(1))
                    .cross(axis)
                    .absoluteValue();

            // place text 10cm above the line

            n.normalize();
            n.multiply(0.1);

            Vector3 textPosition = linePoints.get(0).clone()
                    .add(linePoints.get(1)).multiply(0.5)
                    .add(n);

            Object3D distanceText = createTextObject(text);
            distanceText.setPosition(textPosition);
            m.addObject(distanceText);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
    }

    public Polygon createAreaPolygon(Stack<Vector3> positions) {

        int color = Color.argb((int)(0.3f * 255), Color.red(ELEMENT_COLOR), Color.green(ELEMENT_COLOR), Color.blue(ELEMENT_COLOR));

        Polygon area = new Polygon(positions, color);
        area.setMaterial(new Material());
        area.setColor(color);
        area.setTransparent(true);
        area.setAlpha(0.3f);
        return area;
    }

    public TextureCache getTextureCache() {
        return textureCache;
    }


}
