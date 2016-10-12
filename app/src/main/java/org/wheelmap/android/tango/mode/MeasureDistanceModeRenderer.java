package org.wheelmap.android.tango.mode;

import org.rajawali3d.Object3D;
import org.wheelmap.android.tango.mode.operations.CreateObjectsOperation;
import org.wheelmap.android.tango.mode.operations.OperationsModeRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class MeasureDistanceModeRenderer extends OperationsModeRenderer {

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
                    String text = String.format(Locale.getDefault(), "%.2fm", getDistance());
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

    public double getDistance() {
        if (pointObjects.size() < 2) {
            return 0;
        }
        int lastItemPosition = pointObjects.size() - 1;
        Object3D lastItem = pointObjects.get(lastItemPosition);
        int secondLastItemPosition = pointObjects.size() - 2;
        Object3D secondLastItem = pointObjects.get(secondLastItemPosition);
        return lastItem.getPosition().distanceTo(secondLastItem.getPosition());
    }

    @Override
    public void clear() {
        super.clear();
        pointObjects.clear();
    }

}
