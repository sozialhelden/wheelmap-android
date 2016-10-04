package org.wheelmap.android.tango.mode;

import android.util.Log;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.Plane;
import org.rajawali3d.math.vector.Vector3;
import org.wheelmap.android.tango.mode.operations.CreateObjectsOperation;
import org.wheelmap.android.tango.mode.operations.OperationsModeRenderer;
import org.wheelmap.android.tango.renderer.objects.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class MeasureAreaModeRenderer extends OperationsModeRenderer {

    private static final String TAG = MeasureAreaModeRenderer.class.getSimpleName();
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

                if (size == 4) {
                    Object3D lastPoint = pointObjects.get(3);
                    projectObjectToPlane(lastPoint, new Plane(
                            pointObjects.get(0).getPosition(),
                            pointObjects.get(1).getPosition(),
                            pointObjects.get(2).getPosition()
                    ));
                }

                if (size > 1) {

                    String text = String.format(Locale.getDefault(), "%.2fm", getLastDistance());
                    getObjectFactory().measureLineBetween(m, pointObjects.get(size - 1).getPosition(), pointObjects.get(size - 2).getPosition(), text);

                }

                if (size == 4) {

                    // close polygon by drawing line between first and last point
                    Vector3 first = pointObjects.get(0).getPosition();
                    Vector3 last = pointObjects.get(size - 1).getPosition();

                    String text = String.format(Locale.getDefault(), "%.2fm", first.distanceTo(last));
                    getObjectFactory().measureLineBetween(m, first, last, text);

                    // finish polygon by filling it
                    Stack<Vector3> areaPoints = new Stack<>();
                    for (int i = 0, pointObjectsSize = pointObjects.size(); i < pointObjectsSize; i++) {
                        Object3D pointObject = pointObjects.get(i);
                        areaPoints.add(pointObject.getPosition());
                    }
                    Polygon area = getObjectFactory().createAreaPolygon(areaPoints);
                    m.addObject(area);
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
        return pointObjects.size() >= 4;
    }

    private double getLastDistance() {
        if (pointObjects.size() < 2) {
            return 0;
        }
        int lastItemPosition = pointObjects.size() - 1;
        Object3D lastItem = pointObjects.get(lastItemPosition);
        int secondLastItemPosition = pointObjects.size() - 2;
        Object3D secondLastItem = pointObjects.get(secondLastItemPosition);
        return lastItem.getPosition().distanceTo(secondLastItem.getPosition());
    }

    private void projectObjectToPlane(Object3D point, Plane plane) {
        Vector3 position = point.getPosition();
        double distance = plane.getDistanceTo(position);
        Vector3 normal = plane.getNormal().clone();
        normal.normalize();
        normal.multiply(-distance);
        position.add(normal);
        point.setPosition(position);
    }

    private Object3D calculateLastPointOfPolygon() {
        if (pointObjects.size() != 3) {
            throw new IllegalStateException();
        }

        Vector3 a = pointObjects.get(0).getPosition();
        Vector3 b = pointObjects.get(1).getPosition();
        Vector3 c = pointObjects.get(2).getPosition();

        Vector3 ba = b.clone().subtract(a);

        Vector3 position = c.clone().subtract(ba);
        Object3D point4 = getObjectFactory().createMeasurePoint();
        point4.setPosition(position);
        return point4;
    }

    public double getArea() {
        Vector3 c;
        // TODO
        return 0;
    }

    @Override
    public void clear() {
        super.clear();
        pointObjects.clear();
    }

}
