package ru.krotarnya.diasync.common.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Duration;
import java.util.List;

import ru.krotarnya.diasync.common.DefaultObject;
import ru.krotarnya.diasync.common.util.CompressionUtils;

public final class WatchFaceBloodData extends DefaultObject {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());
    private final List<BloodPoint> points;
    private final TrendArrow trendArrow;
    private final Params params;

    public WatchFaceBloodData(
            @JsonProperty("points") List<BloodPoint> points,
            @JsonProperty("trend") TrendArrow trendArrow,
            @JsonProperty("params") Params params) {
        this.points = points;
        this.trendArrow = trendArrow;
        this.params = params;
    }

    public List<BloodPoint> points() {
        return points;
    }

    public Params params() {
        return params;
    }

    public TrendArrow trendArrow() {
        return trendArrow;
    }

    public byte[] serialize() {
        try {
            return CompressionUtils.compress(OBJECT_MAPPER.writeValueAsBytes(this));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Was not able to serialize watchface dto", e);
        }
    }

    public static WatchFaceBloodData deserialize(byte[] compressedJson) throws Exception {
        return OBJECT_MAPPER.readValue(CompressionUtils.decompress(compressedJson), WatchFaceBloodData.class);
    }

    public int getColor(BloodGlucose bloodGlucose) {
        if (bloodGlucose.lt(params().low()))
            return params().colors().low();
        else if (bloodGlucose.gt(params().high()))
            return params().colors().high();

        return params().colors().normal();
    }

    public int getTextColor(BloodGlucose bloodGlucose) {
        if (bloodGlucose.lt(params().low()))
            return params().colors().textLow();
        else if (bloodGlucose.gt(params().high()))
            return params().colors().textHigh();

        return params().colors().textNormal();
    }

    public static final class Params extends DefaultObject {
        private final BloodGlucoseUnit unit;
        private final BloodGlucose low;
        private final BloodGlucose high;
        private final Duration timeWindow;
        private final Colors colors;

        public Params(
                @JsonProperty("unit") BloodGlucoseUnit unit,
                @JsonProperty("low") BloodGlucose low,
                @JsonProperty("high") BloodGlucose high,
                @JsonProperty("timeWindow") Duration timeWindow,
                @JsonProperty("colors") Colors colors)
        {
            this.unit = unit;
            this.low = low;
            this.high = high;
            this.timeWindow = timeWindow;
            this.colors = colors;
        }

        public BloodGlucoseUnit unit() {
            return unit;
        }

        public BloodGlucose low() {
            return low;
        }

        public BloodGlucose high() {
            return high;
        }

        public Duration timeWindow() {
            return timeWindow;
        }

        public Colors colors() {
            return colors;
        }
    }

    public static final class Colors extends DefaultObject {
        private final int low;
        private final int normal;
        private final int high;
        private final int textLow;
        private final int textNormal;
        private final int textHigh;

        public Colors(
                @JsonProperty("low") int low,
                @JsonProperty("normal") int normal,
                @JsonProperty("high") int high,
                @JsonProperty("textLow") int textLow,
                @JsonProperty("textNormal") int textNormal,
                @JsonProperty("textHigh") int textHigh)
        {
            this.low = low;
            this.normal = normal;
            this.high = high;
            this.textLow = textLow;
            this.textNormal = textNormal;
            this.textHigh = textHigh;
        }

        public int high() {
            return high;
        }

        public int normal() {
            return normal;
        }

        public int low() {
            return low;
        }

        public int textLow() {
            return textLow;
        }

        public int textNormal() {
            return textNormal;
        }

        public int textHigh() {
            return textHigh;
        }
    }
}