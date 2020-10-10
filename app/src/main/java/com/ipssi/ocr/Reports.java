package com.ipssi.ocr;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ipssi.OcrApp;
import com.ipssi.ocr.adapter.ReportListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Reports extends AppCompatActivity {

    private ReportListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView list = findViewById(R.id.report_list);
        adapter = new ReportListAdapter();
        list.setAdapter(adapter);

//        getReportsList();
    }

    private void getReportsList() {
        final RequestQueue requestQueue = OcrApp.instance.getRequestQueue();


//Build Request
        String url = Utills.GPS_DATA_URL;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(@NonNull JSONObject resp) {
                try {
                    //  String response = resp.getString("result");

                    Log.e("GPS Data ", resp.toString());
                    if (resp.getInt("status") < 0) {
                        //logout
                        Toast.makeText(getApplicationContext(), "Authorization Token Epired. Login Again.", Toast.LENGTH_LONG).show();

                    } else if (resp.getInt("status") == 1) {
                        Toast.makeText(getApplicationContext(), "Success.", Toast.LENGTH_LONG).show();

                        // pass your data to Adapter here
//                        adapter.setData(your data list)
                    } else if (resp.getInt("status") == 0) {
                        Toast.makeText(getApplicationContext(), Utills.toCamelCase(resp.getString("msg") + ", Check Vehicle Number."), Toast.LENGTH_LONG).show();
                    }

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
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
//10000 is the time in milliseconds adn is equal to 10 sec
        request.setRetryPolicy(new DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);


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