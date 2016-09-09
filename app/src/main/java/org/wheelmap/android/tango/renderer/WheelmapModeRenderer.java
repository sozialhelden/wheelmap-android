package org.wheelmap.android.tango.renderer;

import android.support.annotation.CallSuper;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

public abstract class WheelmapModeRenderer {

    public interface Consumer<T> {
        void consule(T t);
    }

    private List<Object3D> object3DList = new ArrayList<>();
    private List<Object3D> textObjectsList = new ArrayList<>();

    private WheelmapTangoRajawaliRenderer renderer;

    private Manipulator manipulator = new Manipulator();

    public final void setRajawaliRenderer(WheelmapTangoRajawaliRenderer renderer) {
        this.renderer = renderer;
    }

    public final WheelmapTangoRajawaliRenderer getRajawaliRenderer() {
        return renderer;
    }

    public final void manipulateScene(Consumer<Manipulator> consumer) {
        renderer.invokeAction(() -> consumer.consule(manipulator));
    }

    @CallSuper
    public void clear() {
        renderer.invokeAction(() -> {

            for (Object3D object3D : object3DList) {
                renderer.getCurrentScene().removeChild(object3D);
            }
            object3DList.clear();

            for (Object3D object3D : textObjectsList) {
                renderer.getCurrentScene().removeChild(object3D);
            }
            textObjectsList.clear();

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

    public class Manipulator {
        public final void addObject(Object3D object3D) {
            renderer.getCurrentScene().addChild(object3D);
            object3DList.add(object3D);
        }

        /**
         * text objects are always faced to the camera
         */
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
