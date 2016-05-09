package org.wheelmap.android.osmdroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Marc Kurtz
 * @author Manuel Stahl
 *
 */
public class MyLocationNewOverlayFixed extends MyLocationNewOverlay {

    public MyLocationNewOverlayFixed(MapView mapView) {
        super(mapView);
        init(mapView.getContext());
    }

    public MyLocationNewOverlayFixed(IMyLocationProvider myLocationProvider, MapView mapView) {
        super(myLocationProvider, mapView);
        Context context = mapView.getContext();
        init(context);
    }

    private void init(Context context) {

        float density = context.getResources().getDisplayMetrics().density;

        Bitmap directionArrow = getBitmapDirectionArrow(context);
        if (directionArrow == null) {
            return;
        }

        // needs to create two different bitmaps to avoid scaling the same image twice
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(directionArrow, (int)(20 * density), (int)(20 * density), false);
        Bitmap scaledBitmap2 = Bitmap.createScaledBitmap(directionArrow, (int)(20 * density), (int)(20 * density), false);
        setDirectionArrow(scaledBitmap,scaledBitmap2);
    }

    private Bitmap getBitmapDirectionArrow(Context context){
        InputStream is = null;
        try {
            final String resName = "location_poi.png";
            is = context.getAssets().open(resName);
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resName);
            }
            DisplayMetrics mDisplayMetrics =  context.getResources().getDisplayMetrics();

            BitmapFactory.Options options = new BitmapFactory.Options();
            DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
            options.inScreenDensity = metrics.densityDpi;
            options.inTargetDensity =  metrics.densityDpi;
            options.inDensity = DisplayMetrics.DENSITY_DEFAULT*2;

            return BitmapFactory.decodeStream(is, null, options);
        } catch (final Exception e) {
            System.gc();
            // there's not much we can do here
            // - when we load a bitmap from resources we expect it to be found
            e.printStackTrace();
            return null;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException ignore) {
                }
            }
        }
    }

}