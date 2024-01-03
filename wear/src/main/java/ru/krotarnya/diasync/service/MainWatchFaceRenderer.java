package ru.krotarnya.diasync.service;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.wear.watchface.ComplicationSlotsManager;
import androidx.wear.watchface.Renderer;
import androidx.wear.watchface.WatchFace;
import androidx.wear.watchface.WatchFaceType;
import androidx.wear.watchface.WatchState;
import androidx.wear.watchface.style.CurrentUserStyleRepository;

import java.time.ZonedDateTime;

import kotlin.coroutines.Continuation;
import ru.krotarnya.diasync.asset.DiasyncAssets;

public class MainWatchFaceRenderer extends Renderer.CanvasRenderer2<DiasyncAssets> {
    private static final int UPDATE_INTERVAL = 1000;
    private final Paint paint;
    public MainWatchFaceRenderer(
            SurfaceHolder surfaceHolder,
            WatchState watchState,
            ComplicationSlotsManager complicationSlotsManager,
            CurrentUserStyleRepository currentUserStyleRepository,
            Continuation<? super WatchFace> continuation)
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
    }

    @Nullable
    @Override
    protected DiasyncAssets createSharedAssets(@NonNull Continuation<? super DiasyncAssets> continuation)
    {
        return new DiasyncAssets();
    }

    @Override
    public void render(
            @NonNull Canvas canvas,
            @NonNull Rect rect,
            @NonNull ZonedDateTime zonedDateTime,
            @NonNull DiasyncAssets diasyncAssets)
    {
        canvas.drawColor(Color.BLACK);
        canvas.drawCircle(rect.centerX(), rect.centerY(), (float) (Math.min(rect.width() / 2f, rect.height() / 2f) * Math.random()), paint);
    }

    @Override
    public void renderHighlightLayer(
            @NonNull Canvas canvas,
            @NonNull Rect rect,
            @NonNull ZonedDateTime zonedDateTime,
            @NonNull DiasyncAssets diasyncAssets)
    {

    }
}
