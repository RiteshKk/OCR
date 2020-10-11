package com.ipssi.ocr;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ipssi.OcrApp;
import com.ipssi.ocr.databinding.ActivityAfterUnloadBinding;
import com.ipssi.ocr.databinding.ActivityPreLoadBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AfterUnloadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAfterUnloadBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_after_unload);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String gpsDetails=
                try {
                    updateUnload(AfterUnloadActivity.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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

    public void updateUnload(final AfterUnloadActivity activity) throws JSONException {
        final RequestQueue requestQueue = OcrApp.instance.getRequestQueue();
        TextInputLayout userInputLayout = activity.findViewById(R.id.layout_vehicle);
        Editable user = userInputLayout.getEditText().getText();

        JSONObject dataObj = new JSONObject();
        dataObj.put("veh", user.toString());
//Build Request
        String url=Utills.AFTER_UNLOAD_URL+dataObj.getString("veh");
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,url ,
                dataObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject resp) {
                        int status = -1;

                        try {
                            status = resp.getInt("status");
                            if(resp.getInt("status")<0){
                                //logout
                                Toast.makeText(activity, "Authorization Token Expired. Login Again.",Toast.LENGTH_LONG).show();
                                getSharedPreferences(activity.getString(R.string.app_name),MODE_PRIVATE).edit().putBoolean(C.IsLoggedIn,false).apply();
                                startActivity(new Intent(activity, LoginActivity.class));
                            }else if (1 == status) {
                                Toast.makeText(getApplicationContext(), "After UnLoad Data Saved.",Toast.LENGTH_LONG).show();
                                startActivity(new Intent(activity, MainActivity.class));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error check", error.getMessage() + "");
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

        requestQueue.add(request);
    }

}