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
import ru.krotarnya.diasync.common.model.BatteryStatus;
import ru.krotarnya.diasync.common.model.BloodData;
import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodPoint;
import ru.krotarnya.diasync.common.util.DateTimeUtil;

public class WatchFaceRenderer extends Renderer.CanvasRenderer2<Renderer.SharedAssets> {
    private static final Duration UPDATE_INTERVAL = Duration.ofSeconds(60);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EE dd.MM");
    private static final Duration AGO_WARNING_THRESHOLD = Duration.ofSeconds(90);
    private static final int BACKGROUND_COLOR = Color.BLACK;
    private static final int FOREGROUND_PRIMARY_COLOR = Color.WHITE;
    private static final int FOREGROUND_SECONDARY_COLOR = Color.GRAY;
    private static final int BATTERY_NORMAL_COLOR = Color.WHITE;
    private static final int BATTERY_CHARGING_COLOR = Color.GREEN;
    private static final int BATTERY_CRITICAL_COLOR = Color.RED;
    private static final int BATTERY_CRITICAL_PERCENT = 15;
    private static final int ERROR_COLOR = Color.RED;

    @Nullable
    private BloodData bloodData;
    @Nullable
    private BatteryStatus watchBatteryStatus;

    public WatchFaceRenderer(
            @NonNull SurfaceHolder surfaceHolder,
            @NonNull WatchState watchState,
            @NonNull CurrentUserStyleRepository currentUserStyleRepository,
            @Nullable BatteryStatus watchBatteryStatus) {
        super(
                surfaceHolder,
                currentUserStyleRepository,
                watchState,
                WatchFaceType.DIGITAL,
                UPDATE_INTERVAL.toMillis(),
                false);
        this.watchBatteryStatus = watchBatteryStatus;
    }

    @Nullable
    @Override
    protected Renderer.SharedAssets createSharedAssets(@NonNull Continuation<? super Renderer.SharedAssets> continuation) {
        return () -> {
        };
    }

    @Override
    public void render(
            @NonNull Canvas canvas,
            @NonNull Rect rect,
            @NonNull ZonedDateTime zonedDateTime,
            @NonNull Renderer.SharedAssets sharedAssets) {
        canvas.drawColor(BACKGROUND_COLOR);
        renderTime(canvas, rect, zonedDateTime);
        renderWatchBattery(canvas, rect);
        renderNoDataMessage(canvas, rect, zonedDateTime);
        renderChart(canvas, rect, zonedDateTime);
    }

    private void renderWatchBattery(Canvas canvas, Rect rect) {
        Optional.ofNullable(watchBatteryStatus).ifPresent(batteryStatus -> {
            Paint paint = new Paint();
            paint.setFakeBoldText(true);
            paint.setColor(batteryStatus.isCharging()
                    ? BATTERY_CHARGING_COLOR
                    : batteryStatus.chargePercentRounded() <= BATTERY_CRITICAL_PERCENT
                    ? BATTERY_CRITICAL_COLOR
                    : BATTERY_NORMAL_COLOR);

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(rect.height() / 15f);

            canvas.drawText(
                    batteryStatus.chargePercentRounded() + "%",
                    (int) (rect.width() * 0.1),
                    rect.height() * 0.28f - (paint.descent() + paint.ascent()) / 2,
                    paint);
        });
    }

    private void renderChart(Canvas canvas, Rect rect, ZonedDateTime zonedDateTime) {
        Optional.ofNullable(bloodData).ifPresent(data -> {
            Rect graphRect = new Rect(
                    (int) (rect.width() * 0.1),
                    (int) (rect.height() * 0.35),
                    (int) (rect.width() * 0.9),
                    (int) (rect.height() * 0.8));

            Function<Instant, Integer> toX = instant -> {
                long t = instant.toEpochMilli();
                long maxT = zonedDateTime.toInstant().toEpochMilli();
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

            renderTimeLines(canvas, graphRect, data, zonedDateTime, toX);
            renderThresholdLines(canvas, graphRect, data, toY);
            renderChartData(canvas, graphRect, data, toPoint);
            renderBloodGlucose(canvas, graphRect, data);
        });
    }

    private void renderTimeLines(
            Canvas canvas,
            Rect graphRect,
            BloodData data,
            ZonedDateTime zonedDateTime,
            Function<Instant, Integer> toX) {
        Duration timeWindow = data.params().timeWindow();
        ZonedDateTime from = zonedDateTime.minus(timeWindow);

        Paint linePaint = new Paint();
        linePaint.setColor(FOREGROUND_SECONDARY_COLOR);


        Paint textPaint = new Paint();
        float textSize = graphRect.height() / 10f;
        textPaint.setColor(FOREGROUND_SECONDARY_COLOR);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        int minutesPerLine;
        if (timeWindow.compareTo(Duration.ofMinutes(60)) > 0) {
            minutesPerLine = 60;
        } else if (timeWindow.compareTo(Duration.ofMinutes(30)) > 0) {
            minutesPerLine = 30;
        } else {
            minutesPerLine = 15;
        }

        for (ZonedDateTime t = DateTimeUtil.toStartOfNMinutes(from, minutesPerLine);
             t.isBefore(zonedDateTime); t = t.plus(Duration.ofMinutes(minutesPerLine))) {
            int x = toX.apply(t.toInstant());

            if (x >= graphRect.left && x < graphRect.right) {
                int y1 = graphRect.bottom;
                int y2 = graphRect.top;
                int y3 = (int) (graphRect.bottom + textSize * 1.2);
                canvas.drawLine(x, y1, x, y2, linePaint);
                canvas.drawText(t.format(TIME_FORMATTER), x, y3, textPaint);
            }
        }
    }

    private void renderChartData(
            Canvas canvas,
            Rect graphRect,
            BloodData data,
            Function<BloodPoint, Point> toPoint) {
        Paint paint = new Paint();
        float r = graphRect.width() * 20f / data.params().timeWindow().getSeconds();
        data.points().forEach(p -> {
            Point point = toPoint.apply(p);
            if (point.x >= graphRect.left && point.x <= graphRect.right) {
                paint.setColor(getDataColor(p.glucose()));
                canvas.drawCircle(point.x, point.y, r, paint);
            }
        });
    }

    private void renderBloodGlucose(Canvas canvas, Rect graphRect, BloodData data) {
        Paint paint = new Paint();
        Optional<BloodPoint> lastPoint = data.points().stream()
                .max(Comparator.comparing(BloodPoint::time));

        String text = lastPoint.map(BloodPoint::glucose)
                .map(bg -> data.params().unit().getString(bg) + data.trendArrow().getSymbol())
                .orElse("???");

        float textHeight = graphRect.height() / 2.5f;
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
            Rect graphRect,
            BloodData data,
            Function<BloodGlucose, Integer> toY) {
        int x1 = graphRect.left;
        int x2 = graphRect.right;

        Paint paint = new Paint();
        paint.setStrokeWidth(graphRect.height() / 100f);

        paint.setColor(data.params().colors().low());
        canvas.drawLine(x1, toY.apply(data.params().low()), x2, toY.apply(data.params().low()), paint);

        paint.setColor(data.params().colors().high());
        canvas.drawLine(x1, toY.apply(data.params().high()), x2, toY.apply(data.params().high()), paint);
    }

    private void renderTime(Canvas canvas, Rect rect, ZonedDateTime zonedDateTime) {
        Paint paint = new Paint();
        paint.setFakeBoldText(true);
        paint.setColor(FOREGROUND_PRIMARY_COLOR);
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

    private void renderNoDataMessage(Canvas canvas, Rect rect, ZonedDateTime zonedDateTime) {
        Paint paint = new Paint();
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(rect.height() / 10f);
        paint.setColor(ERROR_COLOR);
        Optional<Instant> lastPoint = Optional.ofNullable(bloodData)
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
                .flatMap(bg -> Optional.ofNullable(bloodData).map(data -> data.getColor(bg)))
                .orElse(ERROR_COLOR);
    }

    private int getTextColor(@Nullable BloodGlucose bloodGlucose) {
        return Optional.ofNullable(bloodGlucose)
                .flatMap(bg -> Optional.ofNullable(bloodData).map(data -> data.getTextColor(bg)))
                .orElse(ERROR_COLOR);
    }

    @Override
    public void renderHighlightLayer(
            @NonNull Canvas canvas,
            @NonNull Rect rect,
            @NonNull ZonedDateTime zonedDateTime,
            @NonNull Renderer.SharedAssets diasyncAssets) {

    }

    public void setBloodData(@Nullable BloodData bloodData) {
        this.bloodData = bloodData;
        invalidate();
    }

    public void setWatchBatteryStatus(@Nullable BatteryStatus batteryStatus) {
        this.watchBatteryStatus = batteryStatus;
        invalidate();
    }
}
