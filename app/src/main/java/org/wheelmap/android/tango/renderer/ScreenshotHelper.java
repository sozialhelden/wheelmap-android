package org.wheelmap.android.tango.renderer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLException;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

class ScreenshotHelper {
    private static final String TAG = ScreenshotHelper.class.getSimpleName();

    static Bitmap getBitmap(int x, int y, int width, int height, GL10 gl) throws OutOfMemoryError {
        int[] bitmapBuffer = new int[(width * height)];
        int[] bitmapSource = new int[(width * height)];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);
        try {
            gl.glReadPixels(x, y, width, height, 6408, 5121, intBuffer);
            for (int i = 0; i < height; i++) {
                int offset1 = i * width;
                int offset2 = ((height - i) - 1) * width;
                for (int j = 0; j < width; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    bitmapSource[offset2 + j] = ((-16711936 & texturePixel) | ((texturePixel << 16) & 16711680)) | ((texturePixel >> 16) & MotionEventCompat.ACTION_MASK);
                }
            }
            return Bitmap.createBitmap(bitmapSource, width, height, Config.ARGB_8888);
        } catch (GLException e) {
            Log.e(TAG, "Error generating Bitmap.", e);
            return null;
        }
    }

}
