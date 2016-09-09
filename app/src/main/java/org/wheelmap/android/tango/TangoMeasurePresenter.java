package org.wheelmap.android.tango;

import android.util.Log;

import com.google.atap.tangoservice.TangoException;

import org.wheelmap.android.tango.mode.MeasureWidthModeRenderer;
import org.wheelmap.android.tango.mode.Mode;
import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

class TangoMeasurePresenter {

    private static final String TAG = TangoMeasurePresenter.class.getSimpleName();
    private WheelmapModeRenderer renderer;
    private TangoMeasureActivity view;

    TangoMeasurePresenter(TangoMeasureActivity view) {
        this.view = view;
        this.renderer = new MeasureWidthModeRenderer();
        view.setWheelmapModeRenderer(renderer);
    }

    void onFabClicked() {
        try {
            // Fit a plane on the clicked point using the latest poiont cloud data
            // Synchronize against concurrent access to the RGB timestamp in the OpenGL thread
            // and a possible service disconnection due to an onPause event.
            float[] planeFitTransform;
            planeFitTransform = view.doFitPlane(0.5f, 0.5f);

            if (planeFitTransform != null) {
                Log.d(TAG, "onClickedAt");
                // Update the position of the rendered cube to the pose of the detected plane
                // This update is made thread safe by the renderer
                renderer.onClickedAt(planeFitTransform);
            }
        } catch (TangoException t) {
            /*Toast.makeText(getApplicationContext(),
                    R.string.failed_measurement,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.failed_measurement), t);*/
            t.printStackTrace();
        } catch (SecurityException t) {
            /*Toast.makeText(getApplicationContext(),
                    R.string.failed_permissions,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.failed_permissions), t);*/
            t.printStackTrace();
        }
    }

    public void undo() {
        renderer.undo();
    }

    public void clear() {
        renderer.clear();
    }

    public void onModeSelected(Mode mode) {
        renderer = mode.newRenderer();
        view.setWheelmapModeRenderer(renderer);
    }

}
