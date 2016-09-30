package org.wheelmap.android.tango;

import org.wheelmap.android.net.UploadTangoMeasurementExecutor;

class MeasurementUploadManager {

    private static MeasurementUploadManager instance;

    public static MeasurementUploadManager getInstance() {
        if (instance == null) {
            instance = new MeasurementUploadManager();
        }
        return instance;
    }

    private UploadTangoMeasurementExecutor executor;

    void reset() {
        executor = new UploadTangoMeasurementExecutor();
    }

    UploadTangoMeasurementExecutor getExecutor() {
        return executor;
    }

}
