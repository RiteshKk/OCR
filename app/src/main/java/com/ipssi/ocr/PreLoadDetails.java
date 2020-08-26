package com.ipssi.ocr;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ipssi.ocr.databinding.ActivityPreLoadBinding;
import com.ipssi.ocr.databinding.ActivityPreLoadDetailsBinding;

public class PreLoadDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActivityPreLoadDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_pre_load_details);
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //put your data saving logic here
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
        if (item.getItemId() == R.id.menu_home) {
            Intent home = new Intent(this, MainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(home);
        }else{
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}