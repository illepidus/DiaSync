package ru.krotarnya.diasync.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import ru.krotarnya.diasync.R;

public class WearActivity extends ComponentActivity implements MessageClient.OnMessageReceivedListener {
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.myTextView);
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String income = new String(messageEvent.getData());
        text.setText(income);
    }

    @Override
    public void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }
}
