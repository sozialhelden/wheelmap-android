package org.wheelmap.android.tango.mode;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.wheelmap.android.tango.mode.operations.CreateObjectsOperation;
import org.wheelmap.android.tango.mode.operations.OperationsModeRenderer;
import org.wheelmap.android.tango.renderer.TextureCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class MeasureSlopeAngleModeRenderer extends OperationsModeRenderer {

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
                                .cross(0, 0, 1)
                                .absoluteValue();

                        // place text 10cm above the line

                        n.normalize();
                        n.multiply(0.1);

                        Vector3 textPosition = linePoints.get(0).clone()
                                .add(linePoints.get(1)).multiply(0.5)
                                .add(n);

                        String text = String.format(Locale.getDefault(), "%.1f\u00B0", getAngle());
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

    @Override
    public void clear() {
        super.clear();
        pointObjects.clear();
    }

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
