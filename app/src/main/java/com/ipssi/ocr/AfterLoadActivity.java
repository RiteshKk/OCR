package com.ipssi.ocr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;

import com.ipssi.ocr.ocrUI.OcrCaptureActivity;

public class AfterLoadActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_load);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.btn_manual).setOnClickListener(this);
        findViewById(R.id.btn_ocr).setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_manual:
                startActivity(new Intent(this, ManualEntryActivity.class));
                break;
            case R.id.btn_ocr:
                startActivity(new Intent(this, OcrCaptureActivity.class));
                break;

        }
    }
}