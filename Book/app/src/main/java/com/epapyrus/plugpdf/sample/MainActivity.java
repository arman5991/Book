package com.epapyrus.plugpdf.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.epapyrus.plugpdf.sample.documentView.ReaderWithControllerActivity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent i = new Intent(this, ReaderWithControllerActivity.class);
        startActivity(i);
        finish();
    }
}
