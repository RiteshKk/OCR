package com.ipssi.ocr;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ManualEntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manual, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_home) {
            Intent home = new Intent(this, MainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(home);
        } else if (item.getItemId() == R.id.menu_print) {
            Toast.makeText(this, "Print CLicked", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}