package ru.krotarnya.diasync.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.wear.watchface.complications.data.ComplicationData;
import androidx.wear.watchface.complications.data.ComplicationType;
import androidx.wear.watchface.complications.data.PlainComplicationText;
import androidx.wear.watchface.complications.data.ShortTextComplicationData;
import androidx.wear.watchface.complications.datasource.ComplicationRequest;
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService;

import java.util.Calendar;

import kotlin.coroutines.Continuation;

public class MainComplicationService extends SuspendingComplicationDataSourceService {
    @Nullable
    @Override
    public ComplicationData onComplicationRequest(@NonNull ComplicationRequest complicationRequest, @NonNull Continuation<? super ComplicationData> continuation) {
        ComplicationData data;
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                data = createComplicationData("Mon", "Monday");
                break;
            case Calendar.TUESDAY:
                data = createComplicationData("Tue", "Tuesday");
                break;
            case Calendar.WEDNESDAY:
                data = createComplicationData("Wed", "Wednesday");
                break;
            case Calendar.THURSDAY:
                data = createComplicationData("Thu", "Thursday");
                break;
            case Calendar.FRIDAY:
                data = createComplicationData("Fri", "Friday");
                break;
            case Calendar.SATURDAY:
                data = createComplicationData("Sat", "Saturday");
                break;
            case Calendar.SUNDAY:
                data = createComplicationData("Sun", "Sunday");
                break;
            default:
                throw new IllegalStateException("Too many days");
        }

        return data;
    }

    @Nullable
    @Override
    public ComplicationData getPreviewData(@NonNull ComplicationType type) {
        if (type != ComplicationType.SHORT_TEXT) {
            return null;
        }
        return createComplicationData("Mon", "Monday");
    }

    private ComplicationData createComplicationData(String text, String contentDescription) {
        return new ShortTextComplicationData.Builder(
                new PlainComplicationText.Builder(text).build(),
                new PlainComplicationText.Builder(contentDescription).build())
                .build();
    }
}
