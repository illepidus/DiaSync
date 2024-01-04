package ru.krotarnya.diasync;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.wear.watchface.Renderer;
import androidx.wear.watchface.WatchFaceType;
import androidx.wear.watchface.WatchState;
import androidx.wear.watchface.style.CurrentUserStyleRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

import kotlin.coroutines.Continuation;
import ru.krotarnya.diasync.common.model.BloodChart;
import ru.krotarnya.diasync.common.model.BloodPoint;

public class WatchFaceRenderer extends Renderer.CanvasRenderer2<Renderer.SharedAssets> {
    private static final int UPDATE_INTERVAL = 1000;
    private static final String TAG = "WatchFaceRenderer";
    private BloodChart chart;
    public WatchFaceRenderer(
            SurfaceHolder surfaceHolder,
            WatchState watchState,
            CurrentUserStyleRepository currentUserStyleRepository)
    {
        super(
                surfaceHolder,
                currentUserStyleRepository,
                watchState,
                WatchFaceType.DIGITAL,
                UPDATE_INTERVAL,
                false);
    }

    @Nullable
    @Override
    protected Renderer.SharedAssets createSharedAssets(@NonNull Continuation<? super Renderer.SharedAssets> continuation)
    {
        return () -> {};
    }

    @Override
    public void render(
            @NonNull Canvas canvas,
            @NonNull Rect rect,
            @NonNull ZonedDateTime zonedDateTime,
            @NonNull Renderer.SharedAssets sharedAssets)
    {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(128);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawColor(Color.BLACK);
        canvas.drawText(
                Optional.ofNullable(chart)
                        .flatMap(chart ->
                                chart.points().stream()
                                        .findFirst()
                                        .map(BloodPoint::glucose)
                                        .map(p -> chart.params().unit().getString(p)))
                        .orElse("No data"),
                rect.centerX(),
                rect.centerY() - (paint.descent() + paint.ascent()) / 2,
                paint);
    }

    @Override
    public void renderHighlightLayer(
            @NonNull Canvas canvas,
            @NonNull Rect rect,
            @NonNull ZonedDateTime zonedDateTime,
            @NonNull Renderer.SharedAssets diasyncAssets)
    {

    }

    public void setChart(BloodChart chart) {
        this.chart = chart;
    }
}
