package ru.krotarnya.diasync.service;

import android.graphics.Canvas;
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
import ru.krotarnya.diasync.asset.SharedAsset;

public class MainWatchFaceRenderer extends Renderer.CanvasRenderer2<SharedAsset> {
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
                60000,
                false);
    }

    @Nullable
    @Override
    protected SharedAsset createSharedAssets(@NonNull Continuation<? super SharedAsset> continuation)
    {
        return new SharedAsset();
    }

    @Override
    public void render(
            @NonNull Canvas canvas,
            @NonNull Rect rect,
            @NonNull ZonedDateTime zonedDateTime,
            @NonNull SharedAsset sharedAsset)
    {

    }

    @Override
    public void renderHighlightLayer(
            @NonNull Canvas canvas,
            @NonNull Rect rect,
            @NonNull ZonedDateTime zonedDateTime,
            @NonNull SharedAsset sharedAsset)
    {

    }
}
