package ru.krotarnya.diasync.service;

import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.wear.watchface.ComplicationSlotsManager;
import androidx.wear.watchface.WatchFace;
import androidx.wear.watchface.WatchFaceService;
import androidx.wear.watchface.WatchFaceType;
import androidx.wear.watchface.WatchState;
import androidx.wear.watchface.style.CurrentUserStyleRepository;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Optional;

import kotlin.coroutines.Continuation;
import ru.krotarnya.diasync.WatchFaceRenderer;
import ru.krotarnya.diasync.common.model.BloodChart;

public class DiasyncWatchFaceService
        extends WatchFaceService
        implements MessageClient.OnMessageReceivedListener
{
    private static final String TAG = "DiasyncWatchFaceService";
    @Nullable
    private WatchFaceRenderer watchFaceRenderer;

    @Nullable
    @Override
    protected WatchFace createWatchFace(
            @NonNull SurfaceHolder surfaceHolder,
            @NonNull WatchState watchState,
            @NonNull ComplicationSlotsManager complicationSlotsManager,
            @NonNull CurrentUserStyleRepository currentUserStyleRepository,
            @NonNull Continuation<? super WatchFace> continuation) {
        watchFaceRenderer = new WatchFaceRenderer(
                surfaceHolder,
                watchState,
                currentUserStyleRepository);

        return new WatchFace(WatchFaceType.DIGITAL, watchFaceRenderer);
    }

    @NonNull
    @Override
    protected ComplicationSlotsManager createComplicationSlotsManager(
            @NonNull CurrentUserStyleRepository currentUserStyleRepository)
    {
        return super.createComplicationSlotsManager(currentUserStyleRepository);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, new String(messageEvent.getData()));
        Optional.ofNullable(watchFaceRenderer)
                .filter(ignored -> messageEvent.getPath().equals("/blood_chart"))
                .ifPresent(r -> r.setChart(BloodChart.deserialize(messageEvent.getData())));
    }
}
