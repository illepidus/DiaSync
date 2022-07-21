package ru.krotarnya.diasync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DiasyncDB extends SQLiteOpenHelper {
    private static final String TAG = "DiasyncDB";
    private static DiasyncDB instance;

    private static final String DATABASE_NAME = "diasync";
    private static final int DATABASE_VERSION = 11;

    public DiasyncDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DiasyncDB getInstance(Context context) {
        if (instance == null) {
            instance = new DiasyncDB(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating " + DATABASE_NAME + " tables. Schema = " + DATABASE_VERSION);
        db.execSQL(
                "CREATE TABLE xdrip_calibrations (" +
                "timestamp INTEGER PRIMARY KEY, " +
                "slope REAL, " +
                "intercept REAL);"
        );
        db.execSQL(
                "CREATE TABLE xdrip_values (" +
                "timestamp INTEGER PRIMARY KEY, " +
                "value REAL, " +
                "arrow TEXT, " +
                "xdrip_calibration INTEGER);"
        );
        db.execSQL(
                "CREATE TABLE libre2_values (" +
                "timestamp INTEGER PRIMARY KEY, " +
                "serial TEXT, " +
                "value REAL, " +
                "xdrip_calibration INTEGER, " +
                "xdrip_value INTEGER);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_version,  int new_version) {
        if (old_version != new_version) {
            Log.d(TAG, "Dropping " + DATABASE_NAME + " tables. Schema = " + DATABASE_VERSION);
            db.execSQL("DROP TABLE IF EXISTS xdrip_calibrations");
            db.execSQL("DROP TABLE IF EXISTS xdrip_values");
            db.execSQL("DROP TABLE IF EXISTS libre2_values");
            onCreate(db);
        }
    }

    private void addXDripCalibration(XDripCalibration calibration) {
        SQLiteDatabase db = getWritableDatabase();

        Log.d(TAG, "Inserting XDripCalibration");
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("timestamp", calibration.timestamp);
            values.put("slope", calibration.slope);
            values.put("intercept", calibration.intercept);
            long res = db.insertOrThrow("xdrip_calibrations", null, values);
            if (res >= 0) db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Wasn't able to add XDripCalibration to database. Calibration exits?");
            Log.v(TAG, e.toString());
        } finally {
            db.endTransaction();
        }
    }

    private void addXDripValue(XDripValue value) {
        SQLiteDatabase db = getWritableDatabase();
        addXDripCalibration(value.calibration);

        Log.d(TAG, "Inserting XDripValue");
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("timestamp", value.timestamp);
            values.put("value", value.value);
            values.put("arrow", value.arrow);
            values.put("xdrip_calibration", value.calibration.timestamp);
            long res = db.insertOrThrow("xdrip_values", null, values);
            if (res >= 0) db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Wasn't able to add XDripValue to database. Value exits?");
            Log.v(TAG, e.toString());
        } finally {
            db.endTransaction();
        }
    }

    public void addLibre2Value(Libre2Value value) {
        SQLiteDatabase db = getWritableDatabase();
        addXDripValue(value.xdrip_value);

        Log.d(TAG, "Inserting Libre2Value");
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("timestamp", value.timestamp);
            values.put("value", value.value);
            values.put("serial", value.serial);
            values.put("xdrip_value", value.xdrip_value.timestamp);
            values.put("xdrip_calibration", value.xdrip_calibration.timestamp);
            long res = db.insertOrThrow("libre2_values", null, values);
            if (res >= 0) db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Wasn't able to add Libre2Value to database. Value exits?");
            Log.v(TAG, e.toString());
        } finally {
            db.endTransaction();
        }
    }

    public Libre2ValueList getLibre2Values(long from, long till) {
        return getLibre2Values(from, till, 100000);
    }

    public Libre2ValueList getLastLibre2Values(long limit) {
        return getLibre2Values(0, Long.MAX_VALUE, limit);
    }

    public Libre2Value getLastLibre2Value() {
        Libre2ValueList vals = getLastLibre2Values(1);
        if (vals.size() == 1)
            return vals.get(0);
        return new Libre2Value();
    }

    public Libre2ValueList getLibre2Values(long from, long till, long limit) {
        List<Libre2Value> values = new ArrayList<>();

        String LIBRE2_SELECT_QUERY =
                "SELECT lv.timestamp, lv.serial, lv.value, " +
                "xc.timestamp AS calibration_timestamp, xc.slope AS calibration_slope, xc.intercept AS calibration_intercept, " +
                "xv.timestamp AS xdrip_timestamp, xv.value AS xdrip_value, xv.arrow AS xdrip_arrow " +
                "FROM libre2_values lv " +
                "LEFT JOIN xdrip_values xv ON lv.xdrip_value = xv.timestamp " +
                "LEFT JOIN xdrip_calibrations xc ON lv.xdrip_calibration = xc.timestamp " +
                "WHERE (lv.timestamp > " + from + ") AND (lv.timestamp < " + till + ") " +
                "ORDER BY lv.timestamp DESC LIMIT " + limit;

        Log.d(TAG, "Running query: " + LIBRE2_SELECT_QUERY);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(LIBRE2_SELECT_QUERY, null);
        Log.v(TAG, DatabaseUtils.dumpCursorToString(cursor));

        try {
            if (cursor.moveToFirst()) {
                do {
                    Libre2Value value = new Libre2Value();
                    value.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
                    value.serial = cursor.getString(cursor.getColumnIndexOrThrow("serial"));
                    value.value = cursor.getDouble(cursor.getColumnIndexOrThrow("value"));
                    value.xdrip_calibration.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("calibration_timestamp"));
                    value.xdrip_calibration.slope = cursor.getDouble(cursor.getColumnIndexOrThrow("calibration_slope"));
                    value.xdrip_calibration.intercept = cursor.getDouble(cursor.getColumnIndexOrThrow("calibration_intercept"));
                    value.xdrip_value.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("xdrip_timestamp"));
                    value.xdrip_value.value = cursor.getDouble(cursor.getColumnIndexOrThrow("xdrip_value"));
                    value.xdrip_value.arrow = cursor.getString(cursor.getColumnIndexOrThrow("xdrip_arrow"));
                    values.add(value);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get libre2values from database");
            Log.d(TAG, e.toString());
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }

        return new Libre2ValueList(values);
    }
}
