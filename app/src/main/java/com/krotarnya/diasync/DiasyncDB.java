package com.krotarnya.diasync;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DiasyncDB extends SQLiteOpenHelper {
    private static final String TAG = "DiasyncDB";
    private static DiasyncDB instance;

    private static final String DATABASE_NAME = "diasync";
    private static final int DATABASE_VERSION = 10;

    private static final String TABLE_LIBRE2_VALUES = "libre2_values";
    private static final String COLUMN_LIBRE2_VALUES_TIMESTAMP = "timestamp";
    private static final String COLUMN_LIBRE2_VALUES_SERIAL = "serial";
    private static final String COLUMN_LIBRE2_VALUES_VALUE = "value";
    private static final String COLUMN_LIBRE2_VALUES_XDRIP_VALUE = "xdrip_value";
    private static final String COLUMN_LIBRE2_VALUES_CALIBRATION = "calibration";

    private static final String TABLE_XDRIP_VALUES = "xdrip_values";
    private static final String COLUMN_XDRIP_VALUES_TIMESTAMP = "timestamp";
    private static final String COLUMN_XDRIP_VALUES_VALUE = "value";
    private static final String COLUMN_XDRIP_VALUES_ARROW = "arrow";
    private static final String COLUMN_XDRIP_VALUES_CALIBRATION = "calibration";

    private static final String TABLE_XDRIP_CALIBRATIONS = "xdrip_calibrations";
    private static final String COLUMN_XDRIP_CALIBRATIONS_TIMESTAMP = "timestamp";
    private static final String COLUMN_XDRIP_CALIBRATIONS_SLOPE = "slope";
    private static final String COLUMN_XDRIP_CALIBRATIONS_INTERCEPT = "intercept";

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
            "CREATE TABLE " + TABLE_XDRIP_CALIBRATIONS +
            "(" +
                COLUMN_XDRIP_CALIBRATIONS_TIMESTAMP + " INTEGER PRIMARY KEY," +
                COLUMN_XDRIP_CALIBRATIONS_SLOPE + " REAL," +
                COLUMN_XDRIP_CALIBRATIONS_INTERCEPT + " REAL" +
            ");"
        );
        db.execSQL(
            "CREATE TABLE " + TABLE_XDRIP_VALUES +
            "(" +
                COLUMN_XDRIP_VALUES_TIMESTAMP + " INTEGER PRIMARY KEY," +
                COLUMN_XDRIP_VALUES_VALUE + " REAL," +
                COLUMN_XDRIP_VALUES_ARROW + " TEXT," +
                COLUMN_XDRIP_VALUES_CALIBRATION + " INTEGER" +
            ");"
        );
        db.execSQL(
            "CREATE TABLE " + TABLE_LIBRE2_VALUES +
            "(" +
                COLUMN_LIBRE2_VALUES_TIMESTAMP + " INTEGER PRIMARY KEY," +
                COLUMN_LIBRE2_VALUES_SERIAL + " TEXT," +
                COLUMN_LIBRE2_VALUES_VALUE + " REAL," +
                COLUMN_LIBRE2_VALUES_XDRIP_VALUE + " INTEGER," +
                COLUMN_LIBRE2_VALUES_CALIBRATION + " INTEGER" +
            ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_version,  int new_version) {
        if (old_version != new_version) {
            Log.d(TAG, "Dropping " + DATABASE_NAME + " tables. Schema = " + DATABASE_VERSION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_XDRIP_CALIBRATIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_XDRIP_VALUES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIBRE2_VALUES);
            onCreate(db);
        }
    }

    private void addXDripCalibration(XDripCalibration calibration) {
        SQLiteDatabase db = getWritableDatabase();

        Log.d(TAG, "Inserting XDripCalibration");
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_XDRIP_CALIBRATIONS_TIMESTAMP, calibration.timestamp);
            values.put(COLUMN_XDRIP_CALIBRATIONS_SLOPE, calibration.slope);
            values.put(COLUMN_XDRIP_CALIBRATIONS_INTERCEPT, calibration.intercept);
            long res = db.insertOrThrow(TABLE_XDRIP_CALIBRATIONS, null, values);
            Log.d(TAG, "Inserted row ID[" + res + "] row");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Wasn't able to add XDripCalibration to database. Calibration exsits?");
            Log.d(TAG, e.toString());
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
            values.put(COLUMN_XDRIP_VALUES_TIMESTAMP, value.timestamp);
            values.put(COLUMN_XDRIP_VALUES_VALUE, value.value);
            values.put(COLUMN_XDRIP_VALUES_ARROW, value.arrow);
            values.put(COLUMN_LIBRE2_VALUES_CALIBRATION, value.calibration.timestamp);
            long res = db.insertOrThrow(TABLE_XDRIP_VALUES, null, values);
            Log.d(TAG, "Inserted row ID[" + res + "] row");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Wasn't able to add XDripValue to database. Value exsits?");
            Log.d(TAG, e.toString());
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
            values.put(COLUMN_LIBRE2_VALUES_TIMESTAMP, value.timestamp);
            values.put(COLUMN_LIBRE2_VALUES_VALUE, value.value);
            values.put(COLUMN_LIBRE2_VALUES_SERIAL, value.serial);
            values.put(COLUMN_LIBRE2_VALUES_XDRIP_VALUE, value.xdrip_value.timestamp);
            values.put(COLUMN_LIBRE2_VALUES_CALIBRATION, value.xdrip_calibration.timestamp);
            long res = db.insertOrThrow(TABLE_LIBRE2_VALUES, null, values);
            Log.d(TAG, "Inserted row ID[" + res + "] row");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Wasn't able to add Libre2Value to database. Value exsits? ");
            Log.d(TAG, e.toString());
        } finally {
            db.endTransaction();
        }
    }

    public List<Libre2Value> getLastLibre2Values(int limit) {
        List<Libre2Value> values = new ArrayList<>();
        @SuppressLint("DefaultLocale")
        String LIBRE2_SELECT_QUERY = String.format("SELECT * FROM %s LIMIT %d", TABLE_LIBRE2_VALUES, limit);
        Log.d(TAG, "Running query: " + LIBRE2_SELECT_QUERY);

        SQLiteDatabase db = getReadableDatabase();
        Log.d(TAG, db.isOpen() ? "db_open" : "db_close");
        Cursor cursor = db.rawQuery(LIBRE2_SELECT_QUERY, null);
        Log.d(TAG, "Query returned " + cursor.getCount() + " rows");
        try {
            if (cursor.moveToFirst()) {
                do {
                    Libre2Value value = new Libre2Value();
                    value.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LIBRE2_VALUES_TIMESTAMP));
                    value.serial = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIBRE2_VALUES_SERIAL));
                    value.value = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LIBRE2_VALUES_SERIAL));
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
        return values;
    }
}
