package ru.krotarnya.diasync;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import ru.krotarnya.diasync.common.model.BloodData;

public final class DiasyncGraphBuilder {
    private static final String TAG = DiasyncGraphBuilder.class.getSimpleName();
    private int width;
    private int height;
    private BloodData data;

    public DiasyncGraphBuilder() {
    }

    public DiasyncGraphBuilder setWidth(int width) {
        this.width = width;
        return this;
    }

    public DiasyncGraphBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public DiasyncGraphBuilder setData(BloodData data) {
        this.data = data;
        return this;
    }

    public Bitmap build() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Integer.MAX_VALUE / 2);

        canvas.drawCircle((float) width / 2, (float) height / 2, (float) width / 8, paint);
        return bitmap;
    }
}
