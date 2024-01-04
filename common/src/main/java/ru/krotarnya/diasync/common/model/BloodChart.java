package ru.krotarnya.diasync.common.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class BloodChart {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .registerModule(new JavaTimeModule());
    private final List<BloodPoint> points;
    private final TrendArrow trendArrow;
    private final Params params;

    public BloodChart(
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
            return OBJECT_MAPPER.writeValueAsBytes(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static BloodChart deserialize(byte[] json) {
        try {
            return OBJECT_MAPPER.readValue(json, BloodChart.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getColor(BloodGlucose bloodGlucose) {
        if (bloodGlucose.lt(params().low()))
            return params().colors().low();
        else if (bloodGlucose.gt(params().high()))
            return params().colors().high();

        return params().colors().normal();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BloodChart) obj;
        return Objects.equals(this.points, that.points) &&
                Objects.equals(this.params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, params);
    }

    @Override
    public String toString() {
        return "BloodChart[" +
                "points=" + points + ", " +
                "params=" + params + ']';
    }

    public static final class Params {
        private final BloodGlucoseUnit unit;
        private final BloodGlucose low;
        private final BloodGlucose high;
        private final Instant from;
        private final Instant to;
        private final Colors colors;

        public Params(
                @JsonProperty("unit") BloodGlucoseUnit unit,
                @JsonProperty("low") BloodGlucose low,
                @JsonProperty("high") BloodGlucose high,
                @JsonProperty("from") Instant from,
                @JsonProperty("to") Instant to,
                @JsonProperty("colors") Colors colors) {
            this.unit = unit;
            this.low = low;
            this.high = high;
            this.from = from;
            this.to = to;
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

        public Instant from() {
            return from;
        }

        public Instant to() {
            return to;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Params) obj;
            return Objects.equals(this.unit, that.unit) &&
                    Objects.equals(this.low, that.low) &&
                    Objects.equals(this.high, that.high) &&
                    Objects.equals(this.from, that.from) &&
                    Objects.equals(this.to, that.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unit, low, high, from, to);
        }

        @Override
        public String toString() {
            return "Params[" +
                    "unit=" + unit + ", " +
                    "low=" + low + ", " +
                    "high=" + high + ", " +
                    "from=" + from + ", " +
                    "to=" + to + ']';
        }

        public Colors colors() {
            return colors;
        }
    }

    public static final class Colors {
        private final int low;
        private final int normal;
        private final int high;

        public Colors(
                @JsonProperty("low") int low,
                @JsonProperty("normal") int normal,
                @JsonProperty("high") int high)
        {
            this.low = low;
            this.normal = normal;
            this.high = high;
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

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Colors) obj;
            return Objects.equals(this.low, that.low) &&
                    Objects.equals(this.normal, that.normal) &&
                    Objects.equals(this.high, that.high);
        }

        @Override
        public int hashCode() {
            return Objects.hash(low, normal, high);
        }

        @Override
        public String toString() {
            return "Colors[" +
                    "high=" + low + ", " +
                    "normal=" + normal + ", " +
                    "low=" + high + ']';
        }
    }
}