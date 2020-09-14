package com.ipssi;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class OcrApp extends Application {
    private RequestQueue mRequestQueue = null;
    public static OcrApp instance;


    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void cancelPendingRequests(Object tag) {
        mRequestQueue.cancelAll(tag);
    }
}
