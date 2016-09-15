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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class MeasureDistanceModeRenderer extends OperationsModeRenderer {

    private List<Object3D> pointObjects = new ArrayList<>();

    @Override
    public void onClickedAt(final float[] transform) {

        if (isReady()) {
            return;
        }

        addOperation(new CreateObjectsOperation() {
            @Override
            public void execute(Manipulator m) {

                if (isReady()) {
                    return;
                }

                Object3D createdPoint = getObjectFactory().createMeasurePoint();
                setObjectPose(createdPoint, transform);

                pointObjects.add(createdPoint);
                m.addObject(createdPoint);

                int size = pointObjects.size();
                if (size > 1) {
                    Stack<Vector3> linePoints = new Stack<>();
                    linePoints.add(pointObjects.get(size - 2).getPosition());
                    linePoints.add(pointObjects.get(size - 1).getPosition());
                    Line3D line = new Line3D(linePoints, 50);
                    line.setMaterial(getObjectFactory().getTextureCache().get(TextureCache.MaterialType.LINE));
                    m.addObject(line);
                    try {

                        //calculate normal vector
                        Vector3 n = linePoints.get(0).clone()
                                .subtract(linePoints.get(1))
                                .cross(new Vector3(0, 0, 1))
                                .absoluteValue();

                        // place text 10cm above the line
                        
                        n.normalize();
                        n.multiply(0.1);

                        Vector3 textPosition = linePoints.get(0).clone()
                                .add(linePoints.get(1)).multiply(0.5)
                                .add(n);

                        String text = String.format(Locale.getDefault(), "%.2f", getLastDistance());
                        Object3D distanceText = getObjectFactory().createTextObject(text);
                        distanceText.setPosition(textPosition);
                        m.addTextObject(distanceText);
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

    @Override
    public boolean isReady() {
        return pointObjects.size() >= 2;
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
