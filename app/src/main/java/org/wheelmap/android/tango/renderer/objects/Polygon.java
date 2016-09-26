package org.wheelmap.android.tango.renderer.objects;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;

import java.util.Stack;

public class Polygon extends Object3D {
    private Stack<Vector3> points;
    private int color;

    public Polygon(Stack<Vector3> points, int color) {
        super();
        this.points = points;
        this.color = color;
        init();
    }
 
    private void init() {
        isContainer(true);


        if (points.size() < 3) {
            throw new IllegalStateException("points must have at least 3 elements");
        }

        Vector3 first = points.get(0);

        for (int i = 1, pointsSize = points.size(); i < pointsSize - 1; i++) {
            Stack<Vector3> stack = new Stack<>();
            stack.add(first);
            stack.add(points.get(i));
            stack.add(points.get(i + 1));
            addChild(new Triangle(stack, color));
        }
    }

    @Override
    public void setMaterial(Material material) {
        super.setMaterial(material);
        for (int i = 0; i < getNumChildren(); i++) {
            getChildAt(i).setMaterial(material);
        }
    }

    @Override
    public void setColor(int color) {
        super.setColor(color);
        for (int i = 0; i < getNumChildren(); i++) {
            getChildAt(i).setColor(color);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);
        for (int i = 0; i < getNumChildren(); i++) {
            getChildAt(i).setAlpha(alpha);
        }
    }

    @Override
    public void setTransparent(boolean value) {
        super.setTransparent(value);
        for (int i = 0; i < getNumChildren(); i++) {
            getChildAt(i).setTransparent(value);
        }
    }
}