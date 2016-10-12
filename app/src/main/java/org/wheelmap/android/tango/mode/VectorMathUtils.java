package org.wheelmap.android.tango.mode;

import org.rajawali3d.math.vector.Vector3;

class VectorMathUtils {

    // 2 * atan(norm(x*norm(y) - norm(x)*y) / norm(x * norm(y) + norm(x) * y))
    static double getAngle(Vector3 x, Vector3 y) {

        double normX = x.clone().normalize();
        double normY = x.clone().normalize();

        double top = x.clone().multiply(normY)
                .subtract(y.clone().multiply(normX))
                .normalize();

        double bottom = x.clone().multiply(normY)
                .add(y.clone().multiply(normX))
                .normalize();

        return Math.toDegrees(2 * Math.atan(top / bottom));
    }

}
