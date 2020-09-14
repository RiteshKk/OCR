package com.ipssi.ocr;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ipssi.ocr.databinding.ActivityPreLoadBinding;
import com.ipssi.ocr.databinding.ActivityPreLoadDetailsBinding;
import com.ipssi.ocr.model.FormData;
import com.ipssi.ocr.ocrparser.OCRUtility;
import com.ipssi.ocr.ocrparser.OcrHelper;

public class PreLoadDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPreLoadDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_pre_load_details);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.valueVehicle.setText(getIntent().getStringExtra("veh"));
        binding.valueTrackingTime.setText(getIntent().getStringExtra("gpstime"));
        binding.valueDo.setText(getIntent().getStringExtra("do"));
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //put your data saving logic here
                //No need to save data
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home:
                Intent home = new Intent(this, MainActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(home);
                break;
            case R.id.menu_logout:
                getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit().clear().apply();
                Intent login = new Intent(this, LoginActivity.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(login);
                break;
            default:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}