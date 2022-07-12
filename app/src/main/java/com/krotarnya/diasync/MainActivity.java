package com.krotarnya.diasync;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Button btn_detonate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_detonate = findViewById( R.id.btn_detonate );
        btn_detonate.setOnClickListener( this );
        /*
        DiasyncDB diasync_db = DiasyncDB.getInstance(this);
        List<Libre2Value> values = diasync_db.getLastLibre2Values(5);
        for (Libre2Value value : values) {
            Log.d(TAG, value.getTimestampString() + " Calibrated value = " + value.getCalibratedValue());
        }
        */
    }

    public void onClick(View view) {
        if(view == btn_detonate) {
            Log.d(TAG, "Detonation");
        }
    }
}