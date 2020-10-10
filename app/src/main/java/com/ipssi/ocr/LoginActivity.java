package com.ipssi.ocr;


import android.content.Intent;
import android.content.SharedPreferences;

import android.support.design.widget.TextInputLayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ipssi.OcrApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        if (preferences.getBoolean(C.IsLoggedIn, false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        setContentView(R.layout.activity_login);

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit().putBoolean(C.IsLoggedIn, true).apply();
//                startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                finish();
                try {
                    login(LoginActivity.this, v);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static void login(final LoginActivity loginActivity, final View v) throws JSONException {
        final RequestQueue requestQueue = OcrApp.instance.getRequestQueue();
        TextInputLayout userInputLayout = loginActivity.findViewById(R.id.layout_username);
        Editable user = userInputLayout.getEditText().getText();
        TextInputLayout pInputLayout = loginActivity.findViewById(R.id.layout_password);
        Editable password = pInputLayout.getEditText().getText();

        /*TextInputEditText user = (TextInputEditText) findViewById(R.id.layout_username);
        TextInputEditText password = (TextInputEditText) findViewById(R.id.layout_password);*/
        JSONObject dataObj = new JSONObject();
        dataObj.put("user", user.toString());
        dataObj.put("pass", password.toString());

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Utills.LOGIN_URL,
                dataObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Login", response.toString());
                        int status = -1;
                        String webtoken = null;
                        try {
                            status = response.getInt("status");

                            if (1 == status) {
                                Toast.makeText(loginActivity.getApplicationContext(), "Login successful.", Toast.LENGTH_LONG).show();
                                webtoken = response.getString("msg");
                                loginActivity.getSharedPreferences(loginActivity.getString(R.string.app_name), MODE_PRIVATE).edit().putString(C.token, webtoken).apply();
                                loginActivity.getSharedPreferences(loginActivity.getString(R.string.app_name), MODE_PRIVATE).edit().putBoolean(C.IsLoggedIn, true).apply();
                                loginActivity.finish();
                            } else {

                                Toast.makeText(loginActivity.getApplicationContext(), "Login Failed. Wrong Username or Password.", Toast.LENGTH_LONG).show();
//                                loginActivity.getSharedPreferences(loginActivity.getString(R.string.app_name), MODE_PRIVATE).edit().putBoolean(C.IsLoggedIn, false).apply();
                                loginActivity.getSharedPreferences("OCR", MODE_PRIVATE).edit().clear().apply();
                                //loginActivity.finish();
                                loginActivity.getSharedPreferences(loginActivity.getString(R.string.app_name), MODE_PRIVATE).edit().putString(C.token, "webtoken").apply();
                                loginActivity.getSharedPreferences(loginActivity.getString(R.string.app_name), MODE_PRIVATE).edit().putBoolean(C.IsLoggedIn, true).apply();
                                loginActivity.finish();


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
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");

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