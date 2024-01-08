package ru.krotarnya.diasync.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kotlin.coroutines.Continuation;
import ru.krotarnya.diasync.WatchFaceRenderer;
import ru.krotarnya.diasync.common.model.BatteryStatus;
import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodPoint;
import ru.krotarnya.diasync.common.model.WatchFaceBloodData;

public class MainWatchFaceService
        extends WatchFaceService
        implements MessageClient.OnMessageReceivedListener
{
    private static final String TAG = "MainWatchFaceService";
    private static final VibrationEffect LOW_VIBRATION_EFFECT =
            VibrationEffect.createWaveform(
                    new long[]{800, 400, 800},
                    new  int[]{255, 0, 255},
                    -1);
    private static final VibrationEffect HIGH_VIBRATION_EFFECT =
            VibrationEffect.createOneShot(1000, 255);

    private final BroadcastReceiver tickReceiver;
    private final BroadcastReceiver watchBatteryStatusReceiver;
    @Nullable
    private WatchFaceRenderer watchFaceRenderer;

    public MainWatchFaceService() {
        tickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Optional.ofNullable(watchFaceRenderer).ifPresent(WatchFaceRenderer::invalidate);
            }
        };

        watchBatteryStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Optional.ofNullable(watchFaceRenderer).ifPresent(r -> {
                    boolean isCharging = Set.of(
                                    BatteryManager.BATTERY_STATUS_CHARGING,
                                    BatteryManager.BATTERY_STATUS_FULL)
                            .contains(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1));
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                    r.setWatchBatteryStatus(new BatteryStatus(isCharging, (double) level / scale));
                });
            }
        };
    }

    @Nullable
    @Override
    protected WatchFace createWatchFace(
            @NonNull SurfaceHolder surfaceHolder,
            @NonNull WatchState watchState,
            @NonNull ComplicationSlotsManager complicationSlotsManager,
            @NonNull CurrentUserStyleRepository currentUserStyleRepository,
            @NonNull Continuation<? super WatchFace> continuation) {
        BatteryStatus watchBatteryStatus = Optional.ofNullable((BatteryManager) this.getSystemService(BATTERY_SERVICE))
                .map(bm -> new BatteryStatus(
                        bm.isCharging(),
                        bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) / 100.0))
                .orElse(null);

        watchFaceRenderer = new WatchFaceRenderer(
                surfaceHolder,
                watchState,
                currentUserStyleRepository,
                watchBatteryStatus);

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
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        registerReceiver(watchBatteryStatusReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(tickReceiver);
        unregisterReceiver(watchBatteryStatusReceiver);
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, new String(messageEvent.getData()));
        Optional.ofNullable(watchFaceRenderer)
                .filter(ignored -> messageEvent.getPath().equals("/blood_chart"))
                .ifPresent(r -> {
                    try {
                        WatchFaceBloodData data = WatchFaceBloodData.deserialize(messageEvent.getData());
                        watchFaceRenderer.setBloodData(data);

                        List<BloodGlucose> last = data.points().stream()
                                .sorted(Comparator.comparing(BloodPoint::time).reversed())
                                .map(BloodPoint::glucose)
                                .limit(2)
                                .collect(Collectors.toList());

                        boolean lowAlert = (last.size() > 0)
                                && last.get(0).lt(data.params().low())
                                && ((last.size() == 1) || last.get(0).lt(last.get(1)));

                        boolean highAlert = (last.size() > 0)
                                && last.get(0).gt(data.params().high())
                                && ((last.size() == 1) || last.get(0).gt(last.get(1)));

                        if (lowAlert || highAlert) {
                            VibratorManager vibratorManager = (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
                            Vibrator vibrator = vibratorManager.getDefaultVibrator();
                            vibrator.vibrate(lowAlert ? LOW_VIBRATION_EFFECT : HIGH_VIBRATION_EFFECT);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Was not able to deserialize chart data", e);
                    }
                });
    }
}
