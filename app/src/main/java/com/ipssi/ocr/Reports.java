package com.ipssi.ocr;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.ipssi.ocr.adapter.ReportListAdapter;


public class Reports extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView list = findViewById(R.id.report_list);
        list.setAdapter(new ReportListAdapter());
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