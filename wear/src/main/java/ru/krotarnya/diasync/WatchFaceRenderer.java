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
    private static final String TAG = "WatchFaceRenderer";
    private static final int UPDATE_INTERVAL = 1000;
    private BloodChart chart = BloodChart.deserialize("""
            {"params":{"from":1704369873.051000000,"high":{"mgdl":180.0},"low":{"mgdl":70.0},"to":1704371673.051000000,"unit":"MMOL"},"points":[{"glucose":{"mgdl":107.0},"time":1704371672.119000000},{"glucose":{"mgdl":106.0},"time":1704371610.888000000},{"glucose":{"mgdl":106.0},"time":1704371551.607000000},{"glucose":{"mgdl":106.0},"time":1704371489.987000000},{"glucose":{"mgdl":106.0},"time":1704371430.315000000},{"glucose":{"mgdl":107.0},"time":1704371370.645000000},{"glucose":{"mgdl":110.0},"time":1704371309.693000000},{"glucose":{"mgdl":109.0},"time":1704371250.022000000},{"glucose":{"mgdl":109.0},"time":1704371189.181000000},{"glucose":{"mgdl":109.0},"time":1704371128.341000000},{"glucose":{"mgdl":111.0},"time":1704371069.057000000},{"glucose":{"mgdl":114.0},"time":1704371008.606000000},{"glucose":{"mgdl":117.0},"time":1704370948.548000000},{"glucose":{"mgdl":120.0},"time":1704370886.928000000},{"glucose":{"mgdl":126.0},"time":1704370828.036000000},{"glucose":{"mgdl":126.0},"time":1704370766.807000000},{"glucose":{"mgdl":131.0},"time":1704370706.745000000},{"glucose":{"mgdl":130.0},"time":1704370647.073000000},{"glucose":{"mgdl":131.0},"time":1704370585.454000000},{"glucose":{"mgdl":134.0},"time":1704370525.393000000},{"glucose":{"mgdl":136.0},"time":1704370465.332000000},{"glucose":{"mgdl":143.0},"time":1704370405.271000000},{"glucose":{"mgdl":145.0},"time":1704370343.650000000},{"glucose":{"mgdl":147.0},"time":1704370284.760000000},{"glucose":{"mgdl":147.0},"time":1704370223.529000000},{"glucose":{"mgdl":150.0},"time":1704370162.298000000},{"glucose":{"mgdl":149.0},"time":1704370102.628000000},{"glucose":{"mgdl":153.0},"time":1704370043.346000000},{"glucose":{"mgdl":155.0},"time":1704369982.117000000},{"glucose":{"mgdl":157.0},"time":1704369922.446000000}]}
            """.getBytes());
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
