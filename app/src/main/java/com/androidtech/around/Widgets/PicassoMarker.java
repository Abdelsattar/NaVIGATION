package com.androidtech.around.Widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.androidtech.around.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import static com.androidtech.around.Utils.Utils.createDrawableFromView;

/**
 * Created by OmarAli on 11/12/2016.
 */

public class PicassoMarker implements Target {

    Marker mMarker;
    Context context;

    public PicassoMarker(Marker marker,Context context) {
        Log.d("filter: ", "init marker");

        mMarker = marker;
        this.context=context;

    }

    @Override
    public int hashCode() {
        return mMarker.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof PicassoMarker) {
            Marker marker = ((PicassoMarker) o).mMarker;
            return mMarker.equals(marker);
        } else {
            return false;
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        Log.d("filter: ", "bitmap loaded");
        View viewMarker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.custom_marker, null);
        ImageView markerImage = (ImageView) viewMarker.findViewById(R.id.custom_marker_image);
        markerImage.setImageBitmap(bitmap);
        Bitmap markerBitmap=createDrawableFromView(context,viewMarker);

        mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap));
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.d("filter: ", "bitmap fail");
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        Log.d("filter: ", "bitmap preload");
    }
}