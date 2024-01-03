package ru.krotarnya.diasync.service;

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

import kotlin.coroutines.Continuation;

public class WatchFaceRenderer extends Renderer.CanvasRenderer2<Renderer.SharedAssets> {
    private static final int UPDATE_INTERVAL = 1000;
    private static final String TAG = "WatchFaceRenderer";
    private final Paint paint;
    private String text = "No data";
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
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
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
        canvas.drawColor(Color.BLACK);
        canvas.drawText(text, rect.centerX(), rect.centerY(), paint);
    }

    @Override
    public void renderHighlightLayer(
            @NonNull Canvas canvas,
            @NonNull Rect rect,
            @NonNull ZonedDateTime zonedDateTime,
            @NonNull Renderer.SharedAssets diasyncAssets)
    {

    }

    public void setText(String text) {
        this.text = text;
    }
}
