package com.krotarnya.diasync;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_detonate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_detonate = (Button) findViewById( R.id.btn_detonate );
        btn_detonate.setOnClickListener( this );
    }

    public void onClick(View view) {
        if(view == btn_detonate) {
            Log.d("DETONATED: ", "Detonation");
        }
    }
}