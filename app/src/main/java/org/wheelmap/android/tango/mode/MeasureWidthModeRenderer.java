package org.wheelmap.android.tango.mode;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.wheelmap.android.tango.mode.operations.CreateObjectsOperation;
import org.wheelmap.android.tango.mode.operations.OperationsModeRenderer;
import org.wheelmap.android.tango.renderer.TextureCache;
import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class MeasureWidthModeRenderer extends OperationsModeRenderer {

    private List<Object3D> pointObjects = new ArrayList<>();

    @Override
    public void onClickedAt(float[] transform) {

        addOperation(new CreateObjectsOperation() {
            @Override
            public void run(Manipulator m) {
                super.run(m);

                Object3D createdPoint = getObjectFactory().createMeasurePoint();
                setObjectPose(createdPoint, transform);

                addObject(createdPoint);
                pointObjects.add(createdPoint);
                m.addObject(createdPoint);

                int size = pointObjects.size();
                if (size > 1) {
                    Stack<Vector3> linePoints = new Stack<Vector3>();
                    linePoints.add(pointObjects.get(size - 2).getPosition());
                    linePoints.add(pointObjects.get(size - 1).getPosition());
                    Line3D line = new Line3D(linePoints, 50);
                    line.setMaterial(getObjectFactory().getTextureCache().get(TextureCache.MaterialType.LINE));
                    m.addObject(line);
                    addObject(line);
                    try {
                        Vector3 textPosition = linePoints.get(0).clone()
                                .add(linePoints.get(1)).multiply(0.5)
                                .add(0, 0.1f, 0);

                        String text = String.format(Locale.getDefault(), "%.2f", getLastDistance());
                        Object3D distanceText = getObjectFactory().createTextObject(text);
                        distanceText.setPosition(textPosition);
                        m.addTextObject(distanceText);
                        addObject(distanceText);
                    } catch (ATexture.TextureException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void undo(Manipulator manipulator) {
                super.undo(manipulator);
                pointObjects.removeAll(getCreatedObjects());
            }
        });
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
