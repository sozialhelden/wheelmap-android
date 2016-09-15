package org.wheelmap.android.tango;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.atap.tangoservice.TangoException;

import org.wheelmap.android.online.R;
import org.wheelmap.android.tango.mode.MeasureDistanceModeRenderer;
import org.wheelmap.android.tango.mode.Mode;
import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

class TangoMeasurePresenter {

    private static final String TAG = TangoMeasurePresenter.class.getSimpleName();
    private WheelmapModeRenderer renderer;
    private TangoMeasureActivity view;
    private Handler mainThreadHandler;

    TangoMeasurePresenter(TangoMeasureActivity view) {
        this.view = view;
        mainThreadHandler = new Handler(Looper.getMainLooper());
        setRenderer(new MeasureDistanceModeRenderer());
    }

    private void setRenderer(WheelmapModeRenderer renderer) {
        if (this.renderer != null) {
            this.renderer.setOnStatusChangeListener(null);
        }
        this.renderer = renderer;
        this.renderer.setOnStatusChangeListener(new WheelmapModeRenderer.OnStatusChangeListener() {
            @Override
            public void onStatusChanged() {
                refreshIsReady();
            }
        });
        view.setWheelmapModeRenderer(renderer);
    }

    void onFabClicked() {
        if (renderer == null) {
            return;
        }
        if (renderer.isReady()) {
            onReadyClicked();
        } else {
            onAddNewPointClicked();
        }
    }

    private void onAddNewPointClicked() {
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
            Toast.makeText(view.getApplicationContext(),
                    R.string.failed_measurement,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, view.getString(R.string.failed_measurement), t);
            t.printStackTrace();
        } catch (SecurityException t) {
            /*Toast.makeText(getApplicationContext(),
                    R.string.failed_permissions,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.failed_permissions), t);*/
            t.printStackTrace();
        }
    }

    private void onReadyClicked() {
        // TODO
    }

    private void refreshIsReady() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean isReady = renderer != null && renderer.isReady();
                view.setFabStatus(isReady ? TangoMeasureActivity.FabStatus.READY : TangoMeasureActivity.FabStatus.ADD_NEW);
            }
        });
    }

    public void undo() {
        renderer.undo();
        refreshIsReady();
    }

    public void clear() {
        renderer.clear();
        refreshIsReady();
    }

    public void onModeSelected(Mode mode) {
        renderer = mode.newRenderer();
        view.setWheelmapModeRenderer(renderer);
    }

}
