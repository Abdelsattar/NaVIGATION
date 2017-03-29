package com.androidtech.around.Utils;

/**
 * Created by Ahmed Donkl on 11/14/2016.
 */


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.androidtech.around.R;
import com.androidtech.around.Widgets.CircleTransformation;
import com.squareup.picasso.Picasso;

public class PicassoUtil {
    public static void loadImage(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, R.color.divider));
        Picasso.with(context)
                .load(url)
                .placeholder(cd)
                .centerCrop()
                .into(imageView);
    }

    public static void loadFullImage(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, R.color.cardview_dark_background));
        Picasso.with(context)
                .load(url)
                .into(imageView);
    }

    public static void loadProfileIcon(String url, ImageView imageView) {
        Context context = imageView.getContext();
        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.profile_image)
                .transform(new CircleTransformation())
                .into(imageView);
    }

    public static void loadProfileIconFromUri(Uri uri, ImageView imageView) {
        Context context = imageView.getContext();
        Picasso.with(context)
                .load(uri)
                .placeholder(R.drawable.profile_image)
                .transform(new CircleTransformation())
                .into(imageView);
    }
}
