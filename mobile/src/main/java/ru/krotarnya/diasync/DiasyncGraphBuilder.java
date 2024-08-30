package ru.krotarnya.diasync;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import androidx.core.graphics.ColorUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import ru.krotarnya.diasync.common.model.BloodData;
import ru.krotarnya.diasync.common.model.BloodGlucose;
import ru.krotarnya.diasync.common.model.BloodGlucoseStats;
import ru.krotarnya.diasync.common.model.BloodPoint;
import ru.krotarnya.diasync.common.model.TrendArrow;
import ru.krotarnya.diasync.common.util.BloodCollectors;

public final class DiasyncGraphBuilder {
    private int width;
    private int height;
    private BloodData data;

    public DiasyncGraphBuilder setWidth(int width) {
        this.width = width;
        return this;
    }

    public DiasyncGraphBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public DiasyncGraphBuilder setData(BloodData data) {
        this.data = data;
        return this;
    }

    public Bitmap build() {
        return new Renderer(width, height, data).render();
    }

    private static class Renderer {
        private static final String TAG = Renderer.class.getSimpleName();
        private static final BloodGlucose BLOOD_MARGIN = BloodGlucose.consMgdl(18);
        private static final Duration TIME_MARGIN = Duration.ofSeconds(30);
        private static final Duration AGO_WARNING_THRESHOLD = Duration.ofSeconds(90);
        private final int width;
        private final int height;
        private final BloodData data;
        private final Instant leftBound;
        private final Instant rightBound;
        private final BloodGlucose topBound;
        private final BloodGlucose bottomBound;
        private final Canvas canvas;
        private final Bitmap bitmap;
        private final Instant renderTime;

        private Renderer(int width, int height, BloodData data) {
            this.width = width;
            this.height = height;
            this.data = data;

            this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            this.canvas = new Canvas(bitmap);

            this.renderTime = Instant.now();
            this.leftBound = renderTime.minus(data.params().timeWindow().minus(TIME_MARGIN));
            this.rightBound = renderTime.plus(TIME_MARGIN);

            BloodGlucoseStats bloodStats = Stream.concat(
                            data.points().stream().map(BloodPoint::glucose),
                            Stream.of(data.params().bloodGlucoseLow(), data.params().bloodGlucoseHigh()))
                    .collect(BloodCollectors.bloodGlucoseCollector())
                    .orElseThrow();

            this.bottomBound = bloodStats.minBloodGlucose().minus(BLOOD_MARGIN);
            this.topBound = bloodStats.maxBloodGlucose().plus(BLOOD_MARGIN);
        }

        private float x(Instant timestamp) {
            // x = width × (x - min_ts) / (max_ts - min_ts)
            return (float) width * (timestamp.toEpochMilli() - leftBound.toEpochMilli())
                    / (rightBound.toEpochMilli() - leftBound.toEpochMilli());
        }

        private float y(BloodGlucose bg) {
            // y = height × (max_bg - x) / (max_bg - min_bg)
            return height * topBound.minus(bg).floatRatio(topBound.minus(bottomBound));
        }

        public Bitmap render() {
            renderBackground();
            renderZones();
            renderPoints();
            renderCurrentBloodText();
            renderNoDataText();
            return bitmap;
        }

        private void renderBackground() {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(data.params().colors().background());
            canvas.drawRect(0, 0, width, height, paint);
        }

        private void renderZones() {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL);
            BloodData.Params params = data.params();

            paint.setColor(params.colors().zoneHigh());
            canvas.drawRect(0, 0, width, y(params.bloodGlucoseHigh()), paint);
            paint.setColor(params.colors().zoneNormal());
            canvas.drawRect(
                    0, y(params.bloodGlucoseLow()), width, y(params.bloodGlucoseHigh()), paint);
            paint.setColor(params.colors().zoneLow());
            canvas.drawRect(0, y(params.bloodGlucoseLow()), width, height, paint);
        }

        private void renderPoints() {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL);
            float r = (float) width * Duration.ofMinutes(1).toMillis()
                    / Duration.between(leftBound, rightBound).toMillis()
                    / 2.5f;

            data.points().forEach(
                    point -> {
                        Log.d(TAG, "Rendering" + point);
                        paint.setColor(data.getColor(point.glucose()));
                        canvas.drawCircle(x(point.time()), y(point.glucose()), r, paint);
                    });
        }


        private void renderCurrentBloodText() {
            Optional<BloodPoint> lastPoint = data.points().stream()
                    .max(Comparator.comparing(BloodPoint::time));

            String text = lastPoint.map(BloodPoint::glucose)
                    .map(bg -> data.params().bloodGlucoseUnit().toString(bg) +
                            data.trendArrow().getSymbol())
                    .orElse("?");

            Function<Float, Paint> textPaintConstructor = height -> {
                Paint paint = new Paint();
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setStyle(Paint.Style.STROKE);
                paint.setTextSize(height);
                paint.setStrokeWidth(height / 15f);
                paint.setFakeBoldText(true);
                return paint;
            };

            float textHWRatio = height / textPaintConstructor.apply((float) height)
                    .measureText("20.0" + TrendArrow.FLAT.getSymbol());

            float desiredHeight = height / 5f;
            float resultingWidth = desiredHeight / textHWRatio;
            float textHeight = resultingWidth > width * 0.8f
                    ? textHWRatio * width * 0.8f
                    : desiredHeight;

            float textX = width / 10f;
            float textY = height / 3f;

            Paint paint = textPaintConstructor.apply(textHeight);
            paint.setTextSize(textHeight);
            paint.setColor(ColorUtils.setAlphaComponent(data.params().colors().background(), 0xFF));
            canvas.drawText(text, textX, textY - (paint.descent() + paint.ascent()) / 2, paint);

            paint.setColor(data.getTextColor(lastPoint.map(BloodPoint::glucose).orElse(null)));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, textX, textY - (paint.descent() + paint.ascent()) / 2, paint);
        }

        private void renderNoDataText() {
            Paint paint = new Paint();
            paint.setFakeBoldText(true);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(height / 15f);
            paint.setColor(data.params().colors().textError());

            Function<Duration, String> agoTextBuilder = duration -> {
                if (duration.compareTo(AGO_WARNING_THRESHOLD) < 0) return "";
                long minutes = duration.getSeconds() / 60;
                String fullText = minutes + " minute" + ((minutes == 1) ? "" : "s") + " ago";
                if (paint.measureText(fullText) < width) return fullText;

                String mediumText = minutes + "m ago";
                if (paint.measureText(mediumText) < width) return mediumText;

                return minutes + "m";
            };

            String text = data.points().stream()
                    .max(Comparator.comparing(BloodPoint::time))
                    .map(BloodPoint::time)
                    .map(t -> Duration.between(t, renderTime))
                    .map(agoTextBuilder)
                    .orElse("NO DATA");

            float y = height * 0.94f - (paint.descent() + paint.ascent()) / 2;
            canvas.drawText(text, (float) width / 2, y, paint);
        }
    }
}
