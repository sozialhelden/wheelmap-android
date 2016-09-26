package org.wheelmap.android.tango.mode;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.awd.BlockSimpleMaterial;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.wheelmap.android.tango.mode.operations.CreateObjectsOperation;
import org.wheelmap.android.tango.mode.operations.OperationsModeRenderer;
import org.wheelmap.android.tango.renderer.TextureCache;
import org.wheelmap.android.tango.renderer.objects.Polygon;
import org.wheelmap.android.tango.renderer.objects.Triangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class MeasureAreaModeRenderer extends OperationsModeRenderer {

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

                    String text = String.format(Locale.getDefault(), "%.2fm", getLastDistance());
                    getObjectFactory().measureLineBetween(m, pointObjects.get(size - 1).getPosition(), pointObjects.get(size - 2).getPosition(), text);

                }

                if (size == 4) {

                    Vector3 first = pointObjects.get(0).getPosition();
                    Vector3 last = pointObjects.get(size - 1).getPosition();

                    String text = String.format(Locale.getDefault(), "%.2fm", first.distanceTo(last));
                    getObjectFactory().measureLineBetween(m, first, last, text);

                    Stack<Vector3> areaPoints = new Stack<>();
                    for (int i = 0, pointObjectsSize = pointObjects.size(); i < pointObjectsSize; i++) {
                        Object3D pointObject = pointObjects.get(i);
                        areaPoints.add(pointObject.getPosition());
                    }
                    Polygon area = new Polygon(areaPoints, Color.argb(128, 128, 0, 0));
                    area.setMaterial(new Material());
                    area.setColor(Color.argb(128, 128, 0, 0));
                    area.setTransparent(true);
                    area.setAlpha(0.5f);
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

    public double getLastDistance() {
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
