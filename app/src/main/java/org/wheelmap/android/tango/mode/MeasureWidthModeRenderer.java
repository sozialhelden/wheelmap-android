package org.wheelmap.android.tango.mode;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

public class MeasureWidthModeRenderer extends WheelmapModeRenderer {

    @Override
    public void onClickedAt(float[] transform) {
        Object3D object = getObjectFactory().createMeasurePoint();
        setObjectPose(object, transform);
    }

    /**
     * Save the updated plane fit pose to update the AR object on the next render pass.
     * This is synchronized against concurrent access in the render loop above.
     */
    private void setObjectPose(Object3D object, float[] planeFitTransform) {
        Matrix4 transform = new Matrix4(planeFitTransform);
        object.setPosition(transform.getTranslation());
        object.setOrientation(new Quaternion().fromMatrix(transform).conjugate());
    }

}
