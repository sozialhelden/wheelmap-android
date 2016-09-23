package org.wheelmap.android.osmdroid;

import android.content.Context;
import android.graphics.Bitmap;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 *
 * @author Marc Kurtz
 * @author Manuel Stahl
 *
 */
public class MyLocationNewOverlayFixed extends MyLocationNewOverlay {

    public MyLocationNewOverlayFixed(IMyLocationProvider myLocationProvider, MapView mapView) {
        super(myLocationProvider, mapView);
        Context context = mapView.getContext();
        init(context);
    }

    private void init(Context context) {

        float density = context.getResources().getDisplayMetrics().density;

        Bitmap positionMarker = ImageLoader.getInstance().loadImageSync("assets://location_poi.png", new ImageSize((int)(12 * density), (int)(12 * density)));
        if (positionMarker != null) {
            setDirectionArrow(positionMarker, positionMarker);
        }
    }
}