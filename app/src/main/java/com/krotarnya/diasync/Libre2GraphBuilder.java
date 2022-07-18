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
    protected long x_min;
    protected long x_max;
    protected double y_min;
    protected double y_max;
    protected Libre2ValueList data;

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

    public Libre2GraphBuilder setData(Libre2ValueList list) {
        this.data = list;
        return this;
    }

    public Libre2GraphBuilder setXMin(long v) {
        x_min = v;
        return this;
    }

    public Libre2GraphBuilder setXMax(long v) {
        x_max = v;
        return this;
    }

    public Libre2GraphBuilder setYMin(double v) {
        y_min = v;
        return this;
    }

    public Libre2GraphBuilder setYMax(double v) {
        y_max = v;
        return this;
    }

    public Bitmap build() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(bitmap);
        Paint paint    = new Paint();

        if (data.size() < 3) {
            paint.setColor(context.getColor(R.color.blood_error_text));
            paint.setTextSize(((float) height) / 7);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No data to plot", ((float) width) / 2, ((float)height) / 7 , paint);
            return bitmap;
        }

        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < data.size(); i++) {
            Libre2Value v = data.get(i);
            paint.setColor(Glucose.bloodGraphColor(v.getCalibratedValue()));
            float cx = 0;
            float cy = 0;
            float r = width * 25000 / (x_max - x_min);
            canvas.drawCircle(width * (v.timestamp - x_min) / (x_max - x_min), (float) (height - height * (v.getCalibratedValue() - y_min) / (y_max - y_min)), r, paint);
        }

        return bitmap;
    }
}
