package org.wheelmap.android.tango;

import org.rajawali3d.scene.ASceneFrameCallback;

public class SimpleASceneFrameCallback extends ASceneFrameCallback {
    @Override
    public void onPreFrame(long sceneTime, double deltaTime) {

    }

    @Override
    public void onPreDraw(long sceneTime, double deltaTime) {

    }

    @Override
    public void onPostFrame(long sceneTime, double deltaTime) {

    }

    public static class PreFrameCallback extends SimpleASceneFrameCallback {
        @Override
        public boolean callPreFrame() {
            return true;
        }
    }
}
