package org.wheelmap.android.tango.renderer;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;

import java.util.HashMap;
import java.util.Map;

public class TextureCache {

    public enum MaterialType {
        CIRCLE,
        LINE
    }

    private Map<MaterialType, Material> cache = new HashMap<>();

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
            default:
                return null;
        }

        cache.put(key, material);
        return material;
    }
    private Material createLineMaterial() {
        Material material = new Material();
        material.setColor(WheelmapRajawaliObjectFactory.ELEMENT_COLOR);
        return material;
    }

    private Material createCircleMaterial() {
        Material material = new Material();
        material.setColor(WheelmapRajawaliObjectFactory.ELEMENT_COLOR);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        return material;
    }

}
