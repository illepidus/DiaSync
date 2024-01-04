package ru.krotarnya.diasync;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
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
import ru.krotarnya.diasync.common.model.BloodChart;
import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodPoint;

public class WatchFaceRenderer extends Renderer.CanvasRenderer2<Renderer.SharedAssets> {
    private static final String TAG = "WatchFaceRenderer";
    private static final int UPDATE_INTERVAL = 1000;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Duration AGO_WARNING_THRESHOLD = Duration.ofSeconds(60);
    @Nullable
    private BloodChart chart = BloodChart.deserialize("""
            {"points":[{"time":1704377226.451000000,"glucose":{"mgdl":196.0}},{"time":1704377167.170000000,"glucose":{"mgdl":194.0}},{"time":1704377105.940000000,"glucose":{"mgdl":199.0}},{"time":1704377045.099000000,"glucose":{"mgdl":199.0}},{"time":1704376985.428000000,"glucose":{"mgdl":205.0}},{"time":1704376924.587000000,"glucose":{"mgdl":206.0}},{"time":1704376864.916000000,"glucose":{"mgdl":215.0}},{"time":1704376805.246000000,"glucose":{"mgdl":216.0}},{"time":1704376743.626000000,"glucose":{"mgdl":211.0}},{"time":1704376682.785000000,"glucose":{"mgdl":214.0}},{"time":1704376623.894000000,"glucose":{"mgdl":214.0}},{"time":1704376563.831000000,"glucose":{"mgdl":218.0}},{"time":1704376501.822000000,"glucose":{"mgdl":220.0}},{"time":1704376442.152000000,"glucose":{"mgdl":215.0}},{"time":1704376381.310000000,"glucose":{"mgdl":208.0}},{"time":1704376322.030000000,"glucose":{"mgdl":203.0}},{"time":1704376260.020000000,"glucose":{"mgdl":206.0}},{"time":1704376201.128000000,"glucose":{"mgdl":197.0}},{"time":1704376139.508000000,"glucose":{"mgdl":203.0}},{"time":1704376079.836000000,"glucose":{"mgdl":205.0}},{"time":1704376020.166000000,"glucose":{"mgdl":207.0}},{"time":1704375958.546000000,"glucose":{"mgdl":213.0}},{"time":1704375898.865000000,"glucose":{"mgdl":206.0}},{"time":1704375839.204000000,"glucose":{"mgdl":211.0}},{"time":1704375778.364000000,"glucose":{"mgdl":200.0}},{"time":1704375716.743000000,"glucose":{"mgdl":200.0}},{"time":1704375657.073000000,"glucose":{"mgdl":192.0}},{"time":1704375597.402000000,"glucose":{"mgdl":190.0}},{"time":1704375535.390000000,"glucose":{"mgdl":190.0}},{"time":1704375476.891000000,"glucose":{"mgdl":186.0}}],"params":{"unit":"MMOL","low":{"mgdl":70.0},"high":{"mgdl":180.0},"from":1704375427.412000000,"to":1704377227.412000000,"colors":{"low":-3995383,"normal":-16728065,"high":-17613}},"trendArrow":"SINGLE_DOWN"}
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
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        renderTime(canvas, rect, zonedDateTime);
        renderBloodGlucose(canvas, rect);
        renderAgoWarning(canvas, rect, zonedDateTime);
        renderChart(canvas, rect);
    }

    private void renderChart(Canvas canvas, Rect rect) {
        Optional.ofNullable(chart).ifPresent(chart -> {
            Rect graphRect = new Rect(
                    (int) (rect.width() * 0.1),
                    (int) (rect.height() * 0.35),
                    (int) (rect.width() * 0.9),
                    (int) (rect.height() * 0.8));

            Function<Instant, Integer> toX = instant -> {
                long t = instant.toEpochMilli();
                long minT = chart.params().from().toEpochMilli();
                long maxT = chart.params().to().toEpochMilli();
                int minX = graphRect.left;
                int maxX = graphRect.right;
                return Math.toIntExact(minX + (maxX - minX) * (t - minT) / (maxT - minT));
            };

            DoubleSummaryStatistics glucoseStatistics = Stream.concat(
                            chart.points().stream().map(BloodPoint::glucose),
                            Stream.of(chart.params().low(), chart.params().high()))
                    .collect(Collectors.summarizingDouble(g -> chart.params().unit().getValue(g)));


            Function<BloodGlucose, Integer> toY = bg -> {
                double v = chart.params().unit().getValue(bg);
                double minV = glucoseStatistics.getMin() * 0.9;
                double maxV = glucoseStatistics.getMax() * 1.1;
                int minY = graphRect.bottom;
                int maxY = graphRect.top;
                return (int) (minY + (maxY - minY) * (v - minV) / (maxV - minV));
            };

            Function<BloodPoint, Point> toPoint = p -> new Point(
                    toX.apply(p.time()),
                    toY.apply(p.glucose()));

            renderChartLines(
                    canvas,
                    rect,
                    chart,
                    toX.apply(chart.params().from()),
                    toX.apply(chart.params().to()),
                    toY);

            renderChartData(
                    canvas,
                    rect,
                    chart,
                    toPoint);
        });
    }

    private void renderChartData(
            Canvas canvas,
            Rect rect,
            BloodChart chart,
            Function<BloodPoint, Point> toPoint)
    {
        Paint paint = new Paint();
        float r = Math.min(rect.width(), rect.height()) / 200f;
        chart.points().forEach(p -> {
            Point point = toPoint.apply(p);
            paint.setColor(chart.getColor(p.glucose()));
            canvas.drawCircle(point.x, point.y, r, paint);
        });
    }

    private void renderChartLines(
            Canvas canvas,
            Rect rect,
            BloodChart chart,
            Integer x1,
            Integer x2,
            Function<BloodGlucose, Integer> toY)
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(rect.height() / 200f);

        paint.setColor(chart.params().colors().low());
        canvas.drawLine(x1, toY.apply(chart.params().low()), x2, toY.apply(chart.params().low()), paint);

        paint.setColor(chart.params().colors().high());
        canvas.drawLine(x1, toY.apply(chart.params().high()), x2, toY.apply(chart.params().high()), paint);
    }

    private void renderTime(Canvas canvas, Rect rect, ZonedDateTime zonedDateTime) {
        Paint paint = new Paint();
        paint.setFakeBoldText(true);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(rect.height() / 10f);
        canvas.drawText(
                zonedDateTime.format(TIME_FORMATTER),
                rect.centerX(),
                rect.height() * 0.1f - (paint.descent() + paint.ascent()) / 2,
                paint);
    }

    private void renderBloodGlucose(Canvas canvas, Rect rect) {
        Paint paint = new Paint();
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(rect.height() / 10f);
        Optional<BloodPoint> lastPoint = Optional.ofNullable(chart)
                .stream()
                .flatMap(c -> c.points().stream())
                .max(Comparator.comparing(BloodPoint::time));
        paint.setColor(getColor(lastPoint.map(BloodPoint::glucose).orElse(null)));
        String text = lastPoint.map(BloodPoint::glucose)
                .map(bg -> chart.params().unit().getString(bg))
                .orElse("???") + chart.trendArrow().getSymbol();
        canvas.drawText(
                text,
                rect.centerX(),
                rect.height() * 0.2f - (paint.descent() + paint.ascent()) / 2,
                paint);
    }

    private void renderAgoWarning(Canvas canvas, Rect rect, ZonedDateTime zonedDateTime) {
        Paint paint = new Paint();
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(rect.height() / 10f);
        paint.setColor(Color.RED);
        Optional<Instant> lastPoint = Optional.ofNullable(chart)
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

    private int getColor(@Nullable BloodGlucose bloodGlucose) {
        return Optional.ofNullable(bloodGlucose)
                .flatMap(bg -> Optional.ofNullable(chart).map(chart -> chart.getColor(bg)))
                .orElse(Color.RED);
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
