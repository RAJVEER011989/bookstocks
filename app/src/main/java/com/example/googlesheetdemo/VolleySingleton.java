package com.example.googlesheetdemo;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
private static VolleySingleton mInstance;
private static RequestQueue mQueue;

private VolleySingleton(Context context){
    mQueue = Volley.newRequestQueue(context.getApplicationContext());
}

public static synchronized VolleySingleton getInstance(Context context){
    if(mInstance==null) {
        mInstance = new VolleySingleton(context);
    }
    return mInstance;
}

public RequestQueue getmQueue(){
    return mQueue;
}
}
