package ru.krotarnya.diasync;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.wear.watchface.Renderer;
import androidx.wear.watchface.WatchFaceType;
import androidx.wear.watchface.WatchState;
import androidx.wear.watchface.style.CurrentUserStyleRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kotlin.coroutines.Continuation;
import ru.krotarnya.diasync.common.model.WatchFaceDto;
import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodPoint;

public class WatchFaceRenderer extends Renderer.CanvasRenderer2<Renderer.SharedAssets> {
    private static final Duration UPDATE_INTERVAL = Duration.ofSeconds(1);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");
    private static final Duration AGO_WARNING_THRESHOLD = Duration.ofSeconds(90);
    private static final int BACKGROUND_COLOR = Color.BLACK;
    private static final int FOREGROUND_COLOR = Color.WHITE;
    private static final int ERROR_COLOR = Color.RED;
    @Nullable
    private WatchFaceDto watchFaceData;

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
                UPDATE_INTERVAL.toMillis(),
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
        canvas.drawColor(BACKGROUND_COLOR);
        renderTime(canvas, rect, zonedDateTime);
        renderAgoWarning(canvas, rect, zonedDateTime);
        renderChart(canvas, rect);
    }

    private void renderChart(Canvas canvas, Rect rect) {
        Optional.ofNullable(watchFaceData).ifPresent(data -> {
            Rect graphRect = new Rect(
                    (int) (rect.width() * 0.1),
                    (int) (rect.height() * 0.35),
                    (int) (rect.width() * 0.9),
                    (int) (rect.height() * 0.8));

            Function<Instant, Integer> toX = instant -> {
                long t = instant.toEpochMilli();
                long maxT = Instant.now().toEpochMilli();
                long minT = maxT - data.params().timeWindow().toMillis();
                int minX = graphRect.left;
                int maxX = graphRect.right;
                return Math.toIntExact(minX + (maxX - minX) * (t - minT) / (maxT - minT));
            };

            DoubleSummaryStatistics glucoseStatistics = Stream.concat(
                            data.points().stream().map(BloodPoint::glucose),
                            Stream.of(data.params().low(), data.params().high()))
                    .collect(Collectors.summarizingDouble(g -> data.params().unit().getValue(g)));


            Function<BloodGlucose, Integer> toY = bg -> {
                double v = data.params().unit().getValue(bg);
                double minV = glucoseStatistics.getMin();
                double maxV = glucoseStatistics.getMax();
                int minY = graphRect.bottom;
                int maxY = graphRect.top;
                return (int) (minY + (maxY - minY) * (v - minV) / (maxV - minV));
            };

            Function<BloodPoint, Point> toPoint = p -> new Point(
                    toX.apply(p.time()),
                    toY.apply(p.glucose()));

            renderThresholdLines(canvas, rect, data, graphRect.left, graphRect.right, toY);
            renderChartData(canvas, rect, data, toPoint);
            renderBloodGlucose(canvas, data, rect, graphRect);
        });
    }

    private void renderChartData(
            Canvas canvas,
            Rect rect,
            WatchFaceDto data,
            Function<BloodPoint, Point> toPoint)
    {
        Paint paint = new Paint();
        float r = rect.width() * 20f / data.params().timeWindow().getSeconds();
        data.points().forEach(p -> {
            Point point = toPoint.apply(p);
            paint.setColor(getDataColor(p.glucose()));
            canvas.drawCircle(point.x, point.y, r, paint);
        });
    }

    private void renderBloodGlucose(Canvas canvas, WatchFaceDto data, Rect rect, Rect graphRect) {
        Paint paint = new Paint();
        Optional<BloodPoint> lastPoint = data.points().stream()
                .max(Comparator.comparing(BloodPoint::time));

        String text = lastPoint.map(BloodPoint::glucose)
                .map(bg -> data.params().unit().getString(bg)  + data.trendArrow().getSymbol())
                .orElse("???");

        float textHeight = rect.height() / 5f;
        float strokeWidth = textHeight / 15f;
        float textX = graphRect.left;
        float textY = graphRect.centerY();

        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(textHeight);

        paint.setColor(BACKGROUND_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawText(text, textX, textY - (paint.descent() + paint.ascent()) / 2, paint);

        paint.setColor(getTextColor(lastPoint.map(BloodPoint::glucose).orElse(null)));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, textX, textY - (paint.descent() + paint.ascent()) / 2, paint);
    }

    private void renderThresholdLines(
            Canvas canvas,
            Rect rect,
            WatchFaceDto data,
            Integer x1,
            Integer x2,
            Function<BloodGlucose, Integer> toY)
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(rect.height() / 200f);

        paint.setColor(data.params().colors().low());
        canvas.drawLine(x1, toY.apply(data.params().low()), x2, toY.apply(data.params().low()), paint);

        paint.setColor(data.params().colors().high());
        canvas.drawLine(x1, toY.apply(data.params().high()), x2, toY.apply(data.params().high()), paint);
    }

    private void renderTime(Canvas canvas, Rect rect, ZonedDateTime zonedDateTime) {
        Paint paint = new Paint();
        paint.setFakeBoldText(true);
        paint.setColor(FOREGROUND_COLOR);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(rect.height() / 5f);
        canvas.drawText(
                zonedDateTime.format(TIME_FORMATTER),
                rect.centerX(),
                rect.height() * 0.15f - (paint.descent() + paint.ascent()) / 2,
                paint);

        paint.setTextSize(rect.height() / 15f);
        canvas.drawText(
                zonedDateTime.format(DATE_FORMATTER),
                rect.centerX(),
                rect.height() * 0.28f - (paint.descent() + paint.ascent()) / 2,
                paint);
    }

    private void renderAgoWarning(Canvas canvas, Rect rect, ZonedDateTime zonedDateTime) {
        Paint paint = new Paint();
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(rect.height() / 10f);
        paint.setColor(ERROR_COLOR);
        Optional<Instant> lastPoint = Optional.ofNullable(watchFaceData)
                .stream()
                .flatMap(c -> c.points().stream())
                .max(Comparator.comparing(BloodPoint::time))
                .map(BloodPoint::time);

        String text = lastPoint
                .map(p -> Duration.between(p, zonedDateTime))
                .map(d -> d.compareTo(AGO_WARNING_THRESHOLD) > 0
                        ? d.getSeconds() / 60 + "m"
                        : "")
                .orElse("NO DATA");

        canvas.drawText(
                text,
                rect.centerX(),
                rect.height() * 0.9f - (paint.descent() + paint.ascent()) / 2,
                paint);
    }

    private int getDataColor(@Nullable BloodGlucose bloodGlucose) {
        return Optional.ofNullable(bloodGlucose)
                .flatMap(bg -> Optional.ofNullable(watchFaceData).map(data -> data.getColor(bg)))
                .orElse(ERROR_COLOR);
    }

    private int getTextColor(@Nullable BloodGlucose bloodGlucose) {
        return Optional.ofNullable(bloodGlucose)
                .flatMap(bg -> Optional.ofNullable(watchFaceData).map(data -> data.getTextColor(bg)))
                .orElse(ERROR_COLOR);
    }

    @Override
    public void renderHighlightLayer(
            @NonNull Canvas canvas,
            @NonNull Rect rect,
            @NonNull ZonedDateTime zonedDateTime,
            @NonNull Renderer.SharedAssets diasyncAssets)
    {

    }

    public void setWatchFaceData(@Nullable WatchFaceDto watchFaceData) {
        this.watchFaceData = watchFaceData;
    }
}
