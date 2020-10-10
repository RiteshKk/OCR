package com.ipssi.ocr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class CaptureImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_1_view:
                Toast.makeText(this, "Image 1 Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.image_2_view:
                Toast.makeText(this, "Image 2 Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.image_3_view:
                Toast.makeText(this, "Image 3 Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.image_4_view:
                Toast.makeText(this, "Image 4 Clicked", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}