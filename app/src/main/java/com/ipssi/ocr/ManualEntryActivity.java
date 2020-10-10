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
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ipssi.ocr.adapter.CustomSpinnerAdapter;
import com.ipssi.ocr.databinding.ActivityManualEntryBinding;
import com.ipssi.ocr.model.FormData;
import com.ipssi.ocr.ocrparser.Document;
import com.ipssi.ocr.ocrparser.FormField;
import com.ipssi.ocr.ocrparser.OCRUtility;
import com.ipssi.ocr.ocrparser.OcrHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ManualEntryActivity extends AppCompatActivity {


    private ActivityManualEntryBinding binding;
    private CustomSpinnerAdapter materialAdapter;
    private ArrayList<android.util.Pair<Integer, String>> materialList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manual_entry);
        setSupportActionBar(binding.toolbar);
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
                android.R.layout.simple_spinner_dropdown_item, materialList);
        binding.spinnerMaterial.setAdapter(materialAdapter);


        ArrayList<Pair<Integer, String>> source = new ArrayList<>();
        source.add(new Pair<>(1, "Source1"));
        source.add(new Pair<>(1, "Source2"));
        source.add(new Pair<>(1, "Source3"));

        CustomSpinnerAdapter sourceAdapter = new CustomSpinnerAdapter(ManualEntryActivity.this,
                android.R.layout.simple_spinner_dropdown_item, source);
        binding.spinnerSource.setAdapter(sourceAdapter);

        ArrayList<Pair<Integer, String>> custList = new ArrayList<>();
        custList.add(new Pair<>(1, "Cust1"));
        custList.add(new Pair<>(1, "Cust2"));
        custList.add(new Pair<>(1, "Cust3"));

        CustomSpinnerAdapter custAdapter = new CustomSpinnerAdapter(ManualEntryActivity.this,
                android.R.layout.simple_spinner_dropdown_item, custList);
        binding.spinnerShipToParty.setAdapter(custAdapter);

      /*  ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.spinner1);
        sItems.setAdapter(adapter);*/
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
        //uncomment these lines If you want to lock editing after OCR scan
//        binding.layoutCustomer.getEditText().setFocusableInTouchMode(false);
//        binding.layoutCustomer.getEditText().setFocusable(false);

        binding.layoutInvoice.getEditText().setText(data.getInvoiceNo());
//        binding.layoutInvoice.getEditText().setFocusableInTouchMode(false);
//        binding.layoutInvoice.getEditText().setFocusable(false);

        binding.layoutVehicle.getEditText().setText(data.getVehicleNo());
//        binding.layoutVehicle.getEditText().setFocusableInTouchMode(false);
//        binding.layoutVehicle.getEditText().setFocusable(false);

        binding.layoutWeight.getEditText().setText(data.getQty());
//        binding.layoutWeight.getEditText().setFocusableInTouchMode(false);
//        binding.layoutWeight.getEditText().setFocusable(false);

        binding.layoutLr.getEditText().setText(data.getSrNo());
//        binding.layoutLr.getEditText().setFocusable(false);
//        binding.layoutLr.getEditText().setFocusableInTouchMode(false);
    }

    public void fetchMaterialData() {
        materialList.add(new Pair<>(1, "Coal"));
        materialList.add(new Pair<>(1, "FlyAsh"));
        materialList.add(new Pair<>(1, "Sand"));

        /*RequestQueue requestQueue = OcrApp.instance.getRequestQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Utills.MAT_URL,
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
        requestQueue.add(request);*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setupViews();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manual, menu);
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
            case R.id.menu_print:
                Toast.makeText(this, "Print CLicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_logout:
                getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit().clear().apply();
                Intent login = new Intent(this, LoginActivity.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(login);
                break;
            default:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public void saveData(View view) {


        JSONObject dataObj = new JSONObject();

        try {
            Document currDocument = OcrHelper.getCurrDocument();

            JSONArray docArry = new JSONArray();
            JSONObject docObj = new JSONObject();
            JSONArray valueArr = new JSONArray();
            if (currDocument != null) {
                docObj.put("id", "CCL".equals(currDocument.getName()) ? 1 : 2);
                docObj.put("name", currDocument.getName());

                for (FormField field : currDocument.getFormFields()) {
                    JSONObject data = new JSONObject();
                    data.put("formFieldName", field.getFormconfig().getFormFieldName());
                    data.put("bestPossibleValue", field.getValueBestPossible());
                    data.put("secondBestPossibleValue", field.getValueSecondBestPossible());
                    String userValue = getUserEditedValue(field.getFormconfig().getFormFieldName(), field.getValueBestPossible());
                    data.put("userEditedValue", userValue);
                    valueArr.put(data);
                }

                if (true) {
                    JSONObject data = new JSONObject();
                    data.put("formFieldName", "mat_id");
                    data.put("bestPossibleValue", binding.spinnerMaterial.getSelectedItem().toString());
                    data.put("secondBestPossibleValue", binding.spinnerMaterial.getSelectedItem().toString());
                    data.put("userEditedValue", null);
                    valueArr.put(data);

                    JSONObject data1 = new JSONObject();
                    data1.put("formFieldName", "source_id");
                    data1.put("bestPossibleValue", binding.spinnerSource.getSelectedItem().toString());
                    data1.put("secondBestPossibleValue", binding.spinnerSource.getSelectedItem().toString());
                    data1.put("userEditedValue", null);
                    valueArr.put(data1);

                    JSONObject data11 = new JSONObject();
                    data11.put("formFieldName", "cust_id");
                    data11.put("bestPossibleValue", binding.spinnerShipToParty.getSelectedItem().toString());
                    data11.put("secondBestPossibleValue", binding.spinnerShipToParty.getSelectedItem().toString());
                    data11.put("userEditedValue", null);
                    valueArr.put(data11);

                }

                docObj.put("formFields", valueArr);
                docArry.put(docObj);

                dataObj.put("configs", docArry);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
//Build Request

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Utills.DATA_SAVE_URL, dataObj, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(@NonNull JSONObject resp) {
                try {
                    JSONArray response =null;
                    try{
                     response = resp.getJSONArray("result");
                    } catch (JSONException e) {

                        if(resp.getInt("status")<0){
                            //logout
                            Toast.makeText(getApplicationContext(), "Authorization Token Expired. Login Again.",Toast.LENGTH_LONG).show();
                            getSharedPreferences(ManualEntryActivity.this.getString(R.string.app_name),MODE_PRIVATE).edit().putBoolean(C.IsLoggedIn,false).apply();
                            startActivity(new Intent(ManualEntryActivity.this, LoginActivity.class));
                        }
                    }
                    if(response!=null)
                    Log.e("Data Saved", String.valueOf(resp));
                    dataSaved(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                String token = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).getString(C.token, null);
                params.put("Authorization", "Bearer "+token);
                return params;
            }
        };
//10000 is the time in milliseconds adn is equal to 10 sec
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);


    }

    private void dataSaved(JSONArray response) {
        OcrHelper.resetObjects();
        Toast.makeText(getApplicationContext(), "After Load Data Saved.",Toast.LENGTH_LONG).show();

        startActivity(new Intent(this, MainActivity.class));
    }

    private String getUserEditedValue(String formFieldName, String value) {
        String uservalue = "";
        if ("invoice_no".equalsIgnoreCase(formFieldName)) {
            uservalue = binding.layoutInvoice.getEditText().getText().toString();
            return value.equals(uservalue) ? null : uservalue;
        } else if ("vehicle_no".equalsIgnoreCase(formFieldName)) {
            uservalue = binding.layoutVehicle.getEditText().getText().toString();
            return value.equals(uservalue) ? null : uservalue;

        } else if ("date_time".equalsIgnoreCase(formFieldName)) {
            return null;
        } else if ("customer".equalsIgnoreCase(formFieldName)) {
            uservalue = binding.layoutCustomer.getEditText().getText().toString();
            return value.equals(uservalue) ? null : uservalue;

        } else if ("so_no".equalsIgnoreCase(formFieldName)) {
            return null;
        } else if ("qty".equalsIgnoreCase(formFieldName)) {
            uservalue = binding.layoutWeight.getEditText().getText().toString();
            return value.equals(uservalue) ? null : uservalue;

        } else if ("date_out".equalsIgnoreCase(formFieldName)) {
            return null;
        } else if ("time_out".equalsIgnoreCase(formFieldName)) {
            return null;
        } else if ("do_no".equalsIgnoreCase(formFieldName)) {
            uservalue = binding.layoutDoRr.getEditText().getText().toString();
            return value.equals(uservalue) ? null : uservalue;
        } else if ("lr_no".equalsIgnoreCase(formFieldName)) {
            uservalue = binding.layoutLr.getEditText().getText().toString();
            return value.equals(uservalue) ? null : uservalue;
        } else if ("eway_bill".equalsIgnoreCase(formFieldName)) {
            uservalue = binding.layoutWaybill.getEditText().getText().toString();
            return value.equals(uservalue) ? null : uservalue;
        } else
            return null;
    }
 /*   String cust=binding.layoutCustomer.getEditText().getText().toString();
    String invo=binding.layoutInvoice.getEditText().getText().toString();
    String dono=binding.layoutDoRr.getEditText().getText().toString();
    String lr=binding.layoutLr.getEditText().getText().toString();
    String veh=binding.layoutVehicle.getEditText().getText().toString();
    String ewayBill=binding.layoutWaybill.getEditText().getText().toString();
    String qty=binding.layoutWeight.getEditText().getText().toString();*/
}