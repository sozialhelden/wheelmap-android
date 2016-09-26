package org.wheelmap.android.tango.renderer.objects;

import android.graphics.Color;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.vector.Vector3;

import java.util.Stack;

public class Triangle extends Object3D {
    private Stack<Vector3> mPoints;
    private int mColor;
 
    public Triangle(Stack<Vector3> points, int color) {
        super();
        mPoints = points;
        mColor = color;
        init();
    }
 
    private void init() {
        setDoubleSided(true);
 
        int numVertices = mPoints.size();
 
        float[] vertices = new float[numVertices * 3];
        float[] textureCoords = new float[numVertices * 2];
        float[] normals = new float[numVertices * 3];
        float[] colors = new float[numVertices * 4];
        int[] indices = new int[numVertices];
        float r = Color.red(mColor) / 255f;
        float g = Color.green(mColor) / 255f;
        float b = Color.blue(mColor) / 255f;
        float a = Color.alpha(mColor) / 255f;
 
        for(int i=0; i<numVertices; i++) {
            Vector3 point = mPoints.get(i);
            int index = i * 3;
            vertices[index] = (float) point.x;
            vertices[index+1] = (float)point.y;
            vertices[index+2] = (float) point.z;
            normals[index] = 0;
            normals[index+1] = 0;
            normals[index+2] = 1;
            index = i * 2;
            textureCoords[index] = 0;
            textureCoords[index+1] = 0;
            index = i * 4;
            colors[index] = r;
            colors[index+1] = g;
            colors[index+2] = b;
            colors[index+3] = a;
            indices[i] = (short)i;
        }
 
        setData(vertices, normals, textureCoords, colors, indices, false);
    }
 
}