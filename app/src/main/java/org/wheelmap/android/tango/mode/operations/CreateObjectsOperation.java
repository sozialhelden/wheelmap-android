package org.wheelmap.android.tango.mode.operations;

import android.support.annotation.CallSuper;

import org.rajawali3d.Object3D;
import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

import java.util.ArrayList;
import java.util.List;

public abstract class CreateObjectsOperation implements Operation {

    private List<Object3D> createdObjects = new ArrayList<>();

    private CreateObjectManipulator manipulatorWrapper = new CreateObjectManipulator();

    void addObject(Object3D object3D) {
        createdObjects.add(object3D);
    }

    protected List<Object3D> getCreatedObjects() {
        return createdObjects;
    }

    @CallSuper
    @Override
    public final void run(WheelmapModeRenderer.Manipulator manipulator) {
        createdObjects.clear();
        manipulatorWrapper.setWrappedManipulator(manipulator);
        execute(manipulatorWrapper);
    }

    public abstract void execute(WheelmapModeRenderer.Manipulator manipulator);

    @Override
    public void undo(WheelmapModeRenderer.Manipulator manipulator) {
        for (Object3D createdObject : createdObjects) {
            manipulator.removeObject(createdObject);
        }
    }

    private class CreateObjectManipulator implements WheelmapModeRenderer.Manipulator {

        private WheelmapModeRenderer.Manipulator impl;

        void setWrappedManipulator(WheelmapModeRenderer.Manipulator impl) {
            this.impl = impl;
        }

        @Override
        public void addObject(Object3D object3D) {
            impl.addObject(object3D);
            CreateObjectsOperation.this.addObject(object3D);
        }

        @Override
        public void addTextObject(Object3D object3D) {
            impl.addTextObject(object3D);
            CreateObjectsOperation.this.addObject(object3D);
        }

        @Override
        public void removeObject(Object3D object3D) {
            impl.addTextObject(object3D);
        }
    }

}
