package com.example.googlesheetdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncResponse {
    private ExecutorService executorService;
    private RequestQueue requestQueue;
    private JsonObjectRequest jsonObjectRequest;
    private String url;
    private MainActivity activity;
    private ProgressDialog progressDialog;

    public SyncResponse(RequestQueue requestQueue, String url, MainActivity activity) {
        executorService = Executors.newSingleThreadExecutor();
        this.url = url;
        this.requestQueue = requestQueue;
        this.activity = activity;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait....");
        progressDialog.setProgress(0);
        progressDialog.setMax(100);

    }

    /*public JSONArray executeThreadForJsonObjest(String data, String title) {
        executorService.execute(new Runnable() {
            JSONArray jsArray;
            @Override
            public void run() {
                // run on ui thread
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setTitle(title);
                        progressDialog.show();
                    }
                });
                jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray jsonArray = response.getJSONArray(data);
                                    jsArray = jsonArray;

                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressBar.dismiss();
                        //alertPopup(error.getMessage());
                    }
                });
                requestQueue.add(jsonObjectRequest);

                // on post execute
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }*/

}
