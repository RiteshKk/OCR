package com.ipssi.ocr;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.android.volley.toolbox.Volley;
import com.ipssi.OcrApp;
import com.ipssi.ocr.databinding.ActivityPreLoadBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PreLoad extends AppCompatActivity {
    private  JSONObject dataObj = new JSONObject();
    private  String gpstime = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPreLoadBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_pre_load);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String gpsDetails=
                try {
                    getGPSDetails(PreLoad.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void getGPSDetails(PreLoad preLoad) throws JSONException {
        final RequestQueue requestQueue = OcrApp.instance.getRequestQueue();


            try {
                TextView viewt = PreLoad.this.findViewById(R.id.label_msg);
                viewt.setText("");

                TextInputLayout vehInputLayout = preLoad.findViewById(R.id.layout_vehicle);
                Editable veh = vehInputLayout.getEditText().getText();
                TextInputLayout doInputLayout = preLoad.findViewById(R.id.layout_do_rr);
                Editable do_rr = doInputLayout.getEditText().getText();

               dataObj.put("veh",veh.toString());
                dataObj.put("do",do_rr.toString());

              } catch (JSONException e) {
                e.printStackTrace();
            }
//Build Request
String url=Utills.GPS_DATA_URL+dataObj.getString("veh");
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,  new JSONObject(), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(@NonNull JSONObject resp) {
                    try {
                     //  String response = resp.getString("result");

                        Log.e("GPS Data ", resp.toString());
                        if(resp.getInt("status")<0){
                            //logout
                            Toast.makeText(getApplicationContext(), "Authorization Token Epired. Login Again.",Toast.LENGTH_LONG).show();
                            getSharedPreferences(PreLoad.this.getString(R.string.app_name),MODE_PRIVATE).edit().putBoolean(C.IsLoggedIn,false).apply();
                            startActivity(new Intent(PreLoad.this, LoginActivity.class));
                        }else if (resp.getInt("status")==1){
                            Toast.makeText(getApplicationContext(), "Success.",Toast.LENGTH_LONG).show();
//                            gpstime=resp.getString("msg");
                            Intent intent = new Intent(PreLoad.this, PreLoadDetails.class);
                            try {
                                intent.putExtra("veh",dataObj.getString("veh"));
                                intent.putExtra("gpstime",resp.getString("msg"));
                                intent.putExtra("do",dataObj.getString("do"));
                                startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else if (resp.getInt("status")==0){

                            Toast.makeText(getApplicationContext(),  Utills.toCamelCase(resp.getString("msg")+", Check Vehicle Number."),Toast.LENGTH_LONG).show();
                            TextView viewt = PreLoad.this.findViewById(R.id.label_msg);
                            viewt.setText(Utills.toCamelCase(resp.getString("msg")+", Check Vehicle Number."));
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