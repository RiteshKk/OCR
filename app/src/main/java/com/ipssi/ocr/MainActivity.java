package com.ipssi.ocr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ipssi.ocr.databinding.ActivityMainBinding;
import com.ipssi.ocr.ocrUI.OcrCaptureActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.btnPreLoad.setOnClickListener(this);
        binding.btnAfterLoad.setOnClickListener(this);
        binding.btnAfterUnload.setOnClickListener(this);
        binding.btnReport.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pre_load:
                startActivity(new Intent(this, PreLoad.class));
                break;
            case R.id.btn_after_load:
                startActivity(new Intent(this, AfterLoadActivity.class));
                break;
            case R.id.btn_after_unload:
                startActivity(new Intent(this, AfterUnloadActivity.class));
                break;
            case R.id.btn_report:
                startActivity(new Intent(this, Reports.class));
                break;
        }

    }
}