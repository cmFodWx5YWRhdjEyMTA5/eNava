

package com.enavamaratha.enavamaratha.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LongSparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import com.enavamaratha.enavamaratha.MainApplication;
import com.enavamaratha.enavamaratha.R;

public class UiUtils {

    static private final LongSparseArray<Bitmap> FAVICON_CACHE = new LongSparseArray<>();

    static public void setPreferenceTheme(Activity a) {
       /* if (!PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true)) {
            a.setTheme(R.style.Theme_Dark);
        }*/
        a.setTheme(R.style.Theme_Light);
    }

    static public int dpToPixel(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, MainApplication.getContext().getResources().getDisplayMetrics());
    }

    static public Bitmap getFaviconBitmap(long feedId, Cursor cursor, int iconCursorPos) {
        Bitmap bitmap = UiUtils.FAVICON_CACHE.get(feedId);
        if (bitmap == null) {
            byte[] iconBytes = cursor.getBlob(iconCursorPos);
            if (iconBytes != null && iconBytes.length > 0) {
                bitmap = UiUtils.getScaledBitmap(iconBytes, 18);
                UiUtils.FAVICON_CACHE.put(feedId, bitmap);
            }
        }
        return bitmap;
    }

    static public Bitmap getScaledBitmap(byte[] iconBytes, int sizeInDp) {
        if (iconBytes != null && iconBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
            if (bitmap != null && bitmap.getWidth() != 0 && bitmap.getHeight() != 0) {
                int bitmapSizeInDip = UiUtils.dpToPixel(sizeInDp);
                if (bitmap.getHeight() != bitmapSizeInDip) {
                    Bitmap tmp = bitmap;
                    bitmap = Bitmap.createScaledBitmap(tmp, bitmapSizeInDip, bitmapSizeInDip, false);
                    tmp.recycle();
                }

                return bitmap;
            }
        }

        return null;
    }

    static public void updateHideReadButton(FloatingActionButton drawerHideReadButton) {
        if (drawerHideReadButton != null) {
            if (PrefUtils.getBoolean(PrefUtils.SHOW_READ, true)) {
                drawerHideReadButton.setColorNormalResId(getAttrResource(drawerHideReadButton.getContext(), R.attr.colorAccent, R.color.light_primary_color));
            } else {
                drawerHideReadButton.setColorNormalResId(R.color.light_disabled);
            }
        }
    }

    static public void displayHideReadButtonAction(Context context) {
        if (PrefUtils.getBoolean(PrefUtils.SHOW_READ, true)) {
            Toast.makeText(context, R.string.context_menu_hide_read, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.context_menu_show_read, Toast.LENGTH_SHORT).show();
        }
    }

    static public void addEmptyFooterView(ListView listView, int dp) {
        View view = new View(listView.getContext());
        view.setMinimumHeight(dpToPixel(dp));
        view.setClickable(true);
        listView.addFooterView(view);
    }

    static public int getAttrResource(Context context, int attrId, int defValue) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attrId});
        int result = a.getResourceId(0, defValue);
        a.recycle();
        return result;
    }
}
