package com.example.shopretail;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import java.io.InputStream;

public class ThemeUtils {

    public static void applyBackgroundToView(Context context, View view, String pageKey) {
        SharedPreferences prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        boolean applyToThisPage = prefs.getBoolean(pageKey, false);
        String uriString = prefs.getString("wallpaper_uri", null);

        if (applyToThisPage && uriString != null) {
            try {
                Uri uri = Uri.parse(uriString);
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                Drawable drawable = Drawable.createFromStream(inputStream, uri.toString());
                view.setBackground(drawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void applyBackground(Activity activity, String pageKey) {
        View root = activity.findViewById(android.R.id.content);
        if (root instanceof ViewGroup) {
            View firstChild = ((ViewGroup) root).getChildAt(0);
            if (firstChild != null) {
                applyBackgroundToView(activity, firstChild, pageKey);
            }
        }
    }
}
