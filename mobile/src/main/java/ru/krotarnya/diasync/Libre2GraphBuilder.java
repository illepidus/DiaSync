package ru.krotarnya.diasync;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;

import ru.krotarnya.diasync.model.Libre2Value;
import ru.krotarnya.diasync.model.Libre2ValueList;

public class Libre2GraphBuilder {
    protected static final String TAG = "Libre2GraphBuilder";
    private final Context context;
    protected int width = 100;
    protected int height = 100;
    protected long x_min = 0;
    protected long x_max = 0;
    protected double y_min = 0;
    protected double y_max = 0;
    protected boolean range_lines = false;
    protected boolean range_zones = false;
    protected Libre2ValueList data;

    public Libre2GraphBuilder(Context context) {
        this.context = context;
    }

    public int convertDpToPixel(float dp) {
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

    public Libre2GraphBuilder setRangeLines(boolean v) {
        range_lines = v;
        return this;
    }

    public Libre2GraphBuilder setRangeZones(boolean v) {
        range_zones = v;
        return this;
    }

    protected float px(long x) {
        return (float) width * (x - x_min) / (x_max - x_min);
    }

    protected float py(double y) {
        return (float) (height - height * (y - y_min) / (y_max - y_min));
    }

    public Bitmap build() {
        Log.d(TAG, "Building graph");

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (range_zones) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Glucose.highGraphZoneColor());
            canvas.drawRect(0, 0, width, py(Glucose.high()), paint);
            paint.setColor(Glucose.normalGraphZoneColor());
            canvas.drawRect(0, py(Glucose.low()), width, py(Glucose.high()), paint);
            paint.setColor(Glucose.lowGraphZoneColor());
            canvas.drawRect(0, py(Glucose.low()), width, height, paint);
        }

        if (range_lines) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Float.min((float) height / 100, (float) width / 100));
            paint.setColor(Glucose.lowGraphColor());
            canvas.drawLine(0, py(Glucose.low()), width, py(Glucose.low()), paint);
            paint.setColor(Glucose.highGraphColor());
            canvas.drawLine(0, py(Glucose.high()), width, py(Glucose.high()), paint);
        }

        if ((data == null) || (data.size() < 1)) {
            return bitmap;
        }

        paint.setStyle(Paint.Style.FILL);
        float r = (float) ((double) (width * 25000L) / (x_max - x_min));
        if (r > (float) height / 50) r = (float) height / 50;
        for (int i = 0; i < data.size(); i++) {
            Libre2Value v = data.get(i);
            paint.setColor(Glucose.bloodGraphColor(v.getValue()));
            canvas.drawCircle(px(v.timestamp), py(v.getValue()), r, paint);
        }

        return bitmap;
    }
}
