package org.wheelmap.android.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.wheelmap.android.manager.MyLocationManager;

import de.greenrobot.event.EventBus;

public class MyLocationProvider implements IMyLocationProvider, SensorEventListener {

    private static final float MIN_DIRECTION_DELTA = 10;

    private float lastDirection;

    private float mDirection;

    private Location mProviderLocation;

    private IMyLocationConsumer mMyLocationConsumer;

    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        mMyLocationConsumer = myLocationConsumer;
        updateLocation(getLocation());
        return true;
    }

    public void register() {
        EventBus bus = EventBus.getDefault();
        bus.registerSticky(this);
        bus.post(MyLocationManager.RegisterEvent.INSTANCE);
    }

    public void unregister() {
        EventBus bus = EventBus.getDefault();
        bus.unregister(this);
        bus.post(new MyLocationManager.UnregisterEvent());
    }

    @Override
    public void stopLocationProvider() {
        mMyLocationConsumer = null;
    }

    @Override
    public Location getLastKnownLocation() {
        return mProviderLocation;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float direction = event.values[0];
        if (direction > 180) {
            direction -= 360;
        }

        if (Math.abs(direction - lastDirection) < MIN_DIRECTION_DELTA) {
            return;
        }

        lastDirection = mDirection;
        mDirection = direction;
        updateLocation(getLocation());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void updateLocation(Location location) {
        if (location == null) {
            return;
        }

        mProviderLocation = location;
        mProviderLocation.setBearing(mDirection + 90);
        if (mMyLocationConsumer != null) {
            mMyLocationConsumer.onLocationChanged(mProviderLocation, this);
        }
    }

    public void onEventMainThread(MyLocationManager.LocationEvent locationEvent) {
        Location location = locationEvent.location;
        updateLocation(location);
    }

    public Location getLocation() {
        return mProviderLocation;
    }
}
