package com.krotarnya.diasync;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;

public class Libre2GraphBuilder {
    protected static final String TAG = "Libre2GraphBuilder";

    private final Context context;
    protected int width;
    protected int height;

    public Libre2GraphBuilder(Context c) {
        context = c;
    }

    public int convertDpToPixel(float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / 160f));
    }

    public Libre2GraphBuilder setWidth(float width) {
        this.width = convertDpToPixel(width);
        return this;
    }

    public Libre2GraphBuilder setHeight(float height) {
        this.height = convertDpToPixel(height);
        return this;
    }

    public Bitmap build() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(bitmap);
        Paint paint    = new Paint();
                paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawCircle( width / 7,  height / 7 , 6, paint);
        return bitmap;
    }
}
