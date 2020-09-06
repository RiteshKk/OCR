package com.ipssi.ocr.ocrUI;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.ipssi.ocr.R;
import com.ipssi.ocr.ocrparser.OCRUtility;
import com.ipssi.ocr.ocrparser.OcrHelper;


public class ShowOcrDataActivity extends AppCompatActivity {

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                String text = OcrHelper.getFormValues();
                OCRUtility.writeStringAsFile("result_12.txt",text);

                setContentView(R.layout.activity_show_ocr_data);
                Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                // Document doc= OcrHelper.getCurrDocument();
//                String text = OcrHelper.getFormValues();
                EditText edtText = findViewById(R.id.editTextTextMultiLine);
                edtText.setText(text);

                FloatingActionButton fab = findViewById(R.id.fab);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
    }
}