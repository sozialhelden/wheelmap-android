package org.wheelmap.android.tango.mode;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.vector.Vector3;
import org.wheelmap.android.tango.mode.operations.CreateObjectsOperation;
import org.wheelmap.android.tango.mode.operations.OperationsModeRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class MeasureSlopeAngleModeRenderer extends OperationsModeRenderer {

    private List<Object3D> pointObjects = new ArrayList<>();

    @Override
    public void onClickedAt(final float[] transform) {

        if (isOperationRunning()) {
            return;
        }

        addOperation(new CreateObjectsOperation() {
            @Override
            public void execute(Manipulator m) {
                if (isReady()) {
                    return;
                }

                Object3D createdPoint = getObjectFactory().createMeasurePoint();
                Object3dUtils.setObjectPose(createdPoint, transform);

                pointObjects.add(createdPoint);
                m.addObject(createdPoint);

                int size = pointObjects.size();
                if (size > 1) {
                    String text = String.format(Locale.getDefault(), "%.1f\u00B0", getAngle());
                    getObjectFactory().measureLineBetween(m, pointObjects.get(size - 1).getPosition(), pointObjects.get(size - 2).getPosition(), text);
                }

            }

            @Override
            public void undo(Manipulator manipulator) {
                super.undo(manipulator);
                pointObjects.removeAll(getCreatedObjects());
            }

        });
    }

    @Override
    public boolean isReady() {
        return pointObjects.size() >= 2;
    }

    @Override
    public void clear() {
        super.clear();
        pointObjects.clear();
    }

    /**
     * calculates the angle of the line between the z surface
     */
    public double getAngle() {
        if (pointObjects.size() < 2) {
            return 0;
        }
        Vector3 one = pointObjects.get(0).getPosition();
        Vector3 two = pointObjects.get(1).getPosition();
        Vector3 u = one.clone().subtract(two);
        // normal to z surface
        Vector3 n = new Vector3(0, 1, 0);
        double top = n.clone().multiply(u).normalize();
        double bottom = n.clone().normalize() * u.clone().normalize();
        return Math.toDegrees(Math.asin(top/bottom));
    }

}
