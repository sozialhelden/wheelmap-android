package org.wheelmap.android.tango.renderer;

import android.support.annotation.CallSuper;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

public abstract class WheelmapModeRenderer {

    public interface Consumer<T> {
        void consume(T t);
    }

    private List<Object3D> object3DList = new ArrayList<>();
    private List<Object3D> textObjectsList = new ArrayList<>();

    private WheelmapTangoRajawaliRenderer renderer;

    private Manipulator manipulator = new ManipulatorImpl();

    final void setRajawaliRenderer(WheelmapTangoRajawaliRenderer renderer) {
        this.renderer = renderer;
    }

    public final WheelmapTangoRajawaliRenderer getRajawaliRenderer() {
        return renderer;
    }

    protected final void manipulateScene(final Consumer<Manipulator> consumer) {
        renderer.invokeAction(new Runnable() {
            @Override
            public void run() {
                consumer.consume(manipulator);
            }
        });
    }

    @CallSuper
    public void clear() {
        renderer.invokeAction(new Runnable() {
            @Override
            public void run() {
                for (Object3D object3D : object3DList) {
                    renderer.getCurrentScene().removeChild(object3D);
                }
                object3DList.clear();

                for (Object3D object3D : textObjectsList) {
                    renderer.getCurrentScene().removeChild(object3D);
                }
                textObjectsList.clear();
            }
        });
    }

    public WheelmapRajawaliObjectFactory getObjectFactory() {
        return renderer.getWheelmapRajawaliObjectFactory();
    }

    @CallSuper
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        Quaternion rotation = renderer.getCurrentCamera().getOrientation();
        Vector3 position = renderer.getCurrentCamera().getPosition();
        for (Object3D textElement : textObjectsList) {
            textElement.setOrientation(rotation);
            textElement.setLookAt(position);
        }
    }

    public abstract void onClickedAt(float[] transform);

    public abstract void undo();

    public abstract boolean isReady();

    public interface Manipulator {
        void addObject(Object3D object3D);

        /**
         * text objects are always faced to the camera
         */
        void addTextObject(Object3D object3D);

        void removeObject(Object3D object3D);
    }

    private class ManipulatorImpl implements Manipulator {
        public final void addObject(Object3D object3D) {
            renderer.getCurrentScene().addChild(object3D);
            object3DList.add(object3D);
        }

        public final void addTextObject(Object3D object3D) {
            renderer.getCurrentScene().addChild(object3D);
            textObjectsList.add(object3D);
        }

        public final void removeObject(Object3D object3D) {
            renderer.getCurrentScene().removeChild(object3D);
            textObjectsList.remove(object3D);
            object3DList.remove(object3D);
        }
    }

}
