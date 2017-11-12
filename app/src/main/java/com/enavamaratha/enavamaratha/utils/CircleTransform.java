

package com.enavamaratha.enavamaratha.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

public class CircleTransform implements Transformation {


    private int mWidth;
    private int mHeight;

    @Override public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        mWidth = (source.getWidth() - size) / 2;
        mHeight = (source.getHeight() - size) / 2;

        Bitmap bitmap = Bitmap.createBitmap(source, mWidth, mHeight, size, size);
        if (bitmap != source) {
            source.recycle();
        }

        return bitmap;
    }

    @Override public String key() {
        return "circle";
    }
}

   /* @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        Bitmap circleBitmap;
        Canvas canvas;
        try {
            circleBitmap = Bitmap.createBitmap(size, size, source.getConfig());
            canvas = new Canvas(circleBitmap);
        } catch (Exception ignored) {
            return source;
        }

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap squaredBitmap;
        try {
            squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        } catch (Exception ignored) {
            circleBitmap.recycle();
            return source;
        }

        if (squaredBitmap != source) {
            source.recycle();
        }
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return circleBitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}*/