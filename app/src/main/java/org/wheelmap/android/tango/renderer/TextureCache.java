package org.wheelmap.android.tango.renderer;

import android.graphics.Color;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;

import java.util.HashMap;
import java.util.Map;

public class TextureCache {

    public enum MaterialType {
        CIRCLE,
        LINE,
        AREA
    }

    private Map<MaterialType, Material> cache = new HashMap<MaterialType, Material>();

    public synchronized Material get(MaterialType key) {

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Material material;
        switch (key) {
            case CIRCLE:
                material = createCircleMaterial();
                break;
            case LINE:
                material = createLineMaterial();
                break;
            case AREA:
                material = createAreaMaterial();
                break;
            default:
                return null;
        }

        cache.put(key, material);
        return material;
    }

    private Material createAreaMaterial() {
        Material material = new Material();
        material.setColor(Color.argb(0, 128, 0, 0));
        return material;
    }

    private Material createLineMaterial() {
        Material material = new Material();
        material.setColor(Color.RED);
        return material;
    }

    private Material createCircleMaterial() {
        Material material = new Material();
        material.setColor(Color.RED);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        return material;
    }

}
