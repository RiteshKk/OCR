package com.ipssi.ocr;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ipssi.OcrApp;
import com.ipssi.ocr.adapter.CustomSpinnerAdapter;
import com.ipssi.ocr.databinding.ActivityManualEntryBinding;
import com.ipssi.ocr.model.FormData;
import com.ipssi.ocr.ocrparser.OCRUtility;
import com.ipssi.ocr.ocrparser.OcrHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ManualEntryActivity extends AppCompatActivity {

    private ActivityManualEntryBinding binding;
    private CustomSpinnerAdapter materialAdapter;
    private ArrayList<android.util.Pair<Integer, String>> materialList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manual_entry);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getBooleanExtra(C.IsScanned, false)) {
            String text = OcrHelper.getFormValues();
            OCRUtility.writeStringAsFile("result_12.txt", text);
            Log.d("data", text);
            FormData data = parseData(text);
            setViews(data);
        }

        setupViews();
        fetchMaterialData();
    }

    private void setupViews() {
        materialAdapter = new CustomSpinnerAdapter(ManualEntryActivity.this,
            android.R.layout.simple_spinner_dropdown_item,
            materialList);
        binding.spinnerMaterial.setAdapter(materialAdapter);
    }

    private FormData parseData(String text) {
        FormData model = new FormData();
        if (text.contains("vehicle_no")) {
            int vehicle_no = text.indexOf("vehicle_no");
            model.setVehicleNo(text.substring(vehicle_no + "vehicle_no=[value1=".length(), text.indexOf("]", vehicle_no)));
        }
        if (text.contains("invoice_no")) {
            int invoice_no = text.indexOf("invoice_no");
            model.setInvoiceNo(text.substring(invoice_no + "invoice_no=[value1=".length(), text.indexOf("]", invoice_no)));
        }
        if (text.contains("date_time")) {
            int date_time = text.indexOf("date_time");
            model.setDateTime(text.substring(date_time + "date_time=[value1=".length(), text.indexOf("]", date_time)));
        }
        if (text.contains("customer")) {
            int customer = text.indexOf("customer");
            model.setCustomer(text.substring(customer + "customer=[value1=".length(), text.indexOf("]", customer)));
        }
        if (text.contains("so_no")) {
            int so_no = text.indexOf("so_no");
            model.setSrNo(text.substring(so_no + "so_no=[value1=".length(), text.indexOf("]", so_no)));
        }
        if (text.contains("qty")) {
            int qty = text.indexOf("qty");
            model.setQty(text.substring(qty + "qty=[value1=".length(), text.indexOf("]", qty)));
        }
        return model;
    }

    private void setViews(FormData data) {
        Objects.requireNonNull(binding.layoutCustomer.getEditText()).setText(data.getCustomer());
        binding.layoutCustomer.getEditText().setFocusableInTouchMode(false);
        binding.layoutCustomer.getEditText().setFocusable(false);

        binding.layoutInvoice.getEditText().setText(data.getInvoiceNo());
        binding.layoutInvoice.getEditText().setFocusableInTouchMode(false);
        binding.layoutInvoice.getEditText().setFocusable(false);

        binding.layoutVehicle.getEditText().setText(data.getVehicleNo());
        binding.layoutVehicle.getEditText().setFocusableInTouchMode(false);
        binding.layoutVehicle.getEditText().setFocusable(false);

        binding.layoutWeight.getEditText().setText(data.getQty());
        binding.layoutWeight.getEditText().setFocusableInTouchMode(false);
        binding.layoutWeight.getEditText().setFocusable(false);

        binding.layoutLr.getEditText().setText(data.getSrNo());
        binding.layoutLr.getEditText().setFocusable(false);
        binding.layoutLr.getEditText().setFocusableInTouchMode(false);



    }

    public void fetchMaterialData() {
        RequestQueue requestQueue = OcrApp.instance.getRequestQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
            "http://203.197.197.16:9950/LocTracker/TempleDashboardData.jsp?action_p=global_json",
            null,
            new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    JSONArray reasonList = response.optJSONArray("ReasonList");
                    for (int i = 0; i < reasonList.length(); i++) {
                        JSONObject object = reasonList.optJSONObject(i);
                        materialList.add(new Pair<>(object.optInt("id", 0), object.optString("name")));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupViews();
                        }
                    });

                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("error check", error.getMessage() + "");
                }
            });
        requestQueue.add(request);
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