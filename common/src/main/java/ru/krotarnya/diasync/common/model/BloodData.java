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

public final class BloodData extends DefaultObject {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());
    private final List<BloodPoint> points;
    private final TrendArrow trendArrow;
    private final Params params;

    public BloodData(
            @JsonProperty("points") List<BloodPoint> points,
            @JsonProperty("trend") TrendArrow trendArrow,
            @JsonProperty("params") Params params)
    {
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

    public static BloodData deserialize(byte[] compressedJson) throws Exception {
        return OBJECT_MAPPER.readValue(CompressionUtils.decompress(compressedJson), BloodData.class);
    }

    public int getColor(BloodGlucose bloodGlucose) {
        if (bloodGlucose.lt(params().bloodGlucoseLow()))
            return params().colors().pointLow();
        else if (bloodGlucose.gt(params().bloodGlucoseHigh()))
            return params().colors().pointHigh();

        return params().colors().pointNormal();
    }

    public int getTextColor(BloodGlucose bloodGlucose) {
        if (bloodGlucose.lt(params().bloodGlucoseLow()))
            return params().colors().textLow();
        else if (bloodGlucose.gt(params().bloodGlucoseHigh()))
            return params().colors().textHigh();

        return params().colors().textNormal();
    }

    public static final class Params extends DefaultObject {
        private final BloodGlucoseUnit bloodGlucoseUnit;
        private final BloodGlucose bloodGlucoseLow;
        private final BloodGlucose bloodGlucoseHigh;
        private final Duration timeWindow;
        private final Colors colors;

        public Params(
                @JsonProperty("unit") BloodGlucoseUnit bloodGlucoseUnit,
                @JsonProperty("low") BloodGlucose bloodGlucoseLow,
                @JsonProperty("high") BloodGlucose bloodGlucoseHigh,
                @JsonProperty("timeWindow") Duration timeWindow,
                @JsonProperty("colors") Colors colors)
        {
            this.bloodGlucoseUnit = bloodGlucoseUnit;
            this.bloodGlucoseLow = bloodGlucoseLow;
            this.bloodGlucoseHigh = bloodGlucoseHigh;
            this.timeWindow = timeWindow;
            this.colors = colors;
        }

        public BloodGlucoseUnit bloodGlucoseUnit() {
            return bloodGlucoseUnit;
        }

        public BloodGlucose bloodGlucoseLow() {
            return bloodGlucoseLow;
        }

        public BloodGlucose bloodGlucoseHigh() {
            return bloodGlucoseHigh;
        }

        public Duration timeWindow() {
            return timeWindow;
        }

        public Colors colors() {
            return colors;
        }
    }

    public static final class Colors extends DefaultObject {
        private final int background;
        private final int pointLow;
        private final int pointNormal;
        private final int pointHigh;
        private final int textLow;
        private final int textNormal;
        private final int textHigh;
        private final int textError;
        private final int zoneLow;
        private final int zoneNormal;
        private final int zoneHigh;

        public Colors(
                @JsonProperty("background") int background,
                @JsonProperty("pointLow") int pointLow,
                @JsonProperty("pointNormal") int pointNormal,
                @JsonProperty("pointHigh") int pointHigh,
                @JsonProperty("textLow") int textLow,
                @JsonProperty("textNormal") int textNormal,
                @JsonProperty("textHigh") int textHigh,
                @JsonProperty("textError") int textError,
                @JsonProperty("zoneLow") int zoneLow,
                @JsonProperty("zoneNormal") int zoneNormal,
                @JsonProperty("zoneHigh") int zoneHigh)
        {
            this.background = background;
            this.pointLow = pointLow;
            this.pointNormal = pointNormal;
            this.pointHigh = pointHigh;
            this.textLow = textLow;
            this.textNormal = textNormal;
            this.textHigh = textHigh;
            this.textError = textError;
            this.zoneLow = zoneLow;
            this.zoneNormal = zoneNormal;
            this.zoneHigh = zoneHigh;
        }

        public int pointHigh() {
            return pointHigh;
        }

        public int pointNormal() {
            return pointNormal;
        }

        public int pointLow() {
            return pointLow;
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

        public int background() {
            return background;
        }

        public int textError() {
            return textError;
        }

        public int zoneLow() {
            return zoneLow;
        }

        public int zoneNormal() {
            return zoneNormal;
        }

        public int zoneHigh() {
            return zoneHigh;
        }
    }
}