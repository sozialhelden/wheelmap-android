package org.wheelmap.android.tango.mode.operations;

import android.support.annotation.CallSuper;

import org.rajawali3d.Object3D;
import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

import java.util.ArrayList;
import java.util.List;

public abstract class CreateObjectsOperation implements Operation {

    private List<Object3D> createdObjects = new ArrayList<>();

    protected void addObject(Object3D object3D) {
        createdObjects.add(object3D);
    }

    protected List<Object3D> getCreatedObjects() {
        return createdObjects;
    }

    @CallSuper
    @Override
    public void run(WheelmapModeRenderer.Manipulator manipulator) {
        createdObjects.clear();
    }

    @Override
    public void undo(WheelmapModeRenderer.Manipulator manipulator) {
        for (Object3D createdObject : createdObjects) {
            manipulator.removeObject(createdObject);
        }
    }

}
