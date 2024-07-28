package ru.krotarnya.diasync.model;

import android.content.Intent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Libre2Update {
    @JsonProperty("xdrip_timestamp")
    public Long xdripTimestamp;
    @JsonProperty("xdrip_arrow")
    public String xdripArrow;
    @JsonProperty("xdrip_value")
    public Double xdripValue;
    @JsonProperty("libre2_serial")
    public String libre2Serial;
    @JsonProperty("source")
    public String source;
    @JsonProperty("libre2_value")
    public Double libre2Value;
    @JsonProperty("xdrip_sync_key")
    public String xdripSyncKey;
    @JsonProperty("libre2_timestamp")
    public Long libre2Timestamp;
    @JsonProperty("xdrip_calibration_slope")
    public Double xdripCalibrationSlope;
    @JsonProperty("xdrip_calibration_intercept")
    public Double xdripCalibrationIntercept;
    @JsonProperty("xdrip_calibration_timestamp")
    public Long xdripCalibrationTimestamp;

    public Libre2Value toLibre2Value() {
        Intent intent = new Intent();
        intent.putExtra("source", source);
        intent.putExtra("libre2_serial", libre2Serial);
        intent.putExtra("libre2_value", libre2Value);
        intent.putExtra("libre2_timestamp", libre2Timestamp);
        intent.putExtra("xdrip_sync_key", xdripSyncKey);
        if (xdripCalibrationSlope != null)
            intent.putExtra("xdrip_calibration_slope", xdripCalibrationSlope);
        if (xdripCalibrationIntercept != null)
            intent.putExtra("xdrip_calibration_intercept", xdripCalibrationIntercept);
        if (xdripCalibrationTimestamp != null)
            intent.putExtra("xdrip_calibration_timestamp", xdripCalibrationTimestamp);
        intent.putExtra("xdrip_value", xdripValue);
        intent.putExtra("xdrip_timestamp", xdripTimestamp);
        intent.putExtra("xdrip_arrow", xdripArrow);

        return new Libre2Value(Objects.requireNonNull(intent.getExtras()));
    }
}
