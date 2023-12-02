package com.example.googlesheetdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class UpdateRecord extends AppCompatActivity {

    TextView quan;
    TextView name;
    TextView price;
    Button add;
    ProgressBar pb;
    ArrayList<String> books;
    String baseURL = "https://script.google.com/macros/s/AKfycbyzil6_ziR41mXL6iSAY-" +
            "Pfw6kuutwkHFLo4Cbsql2zXTuoSPLc58kPi45F4_jthjr6Hg/exec?";
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_record);
        setTitle("Add Books");
        books = new ArrayList<String>();
        quan = findViewById(R.id.quan);
        price = findViewById(R.id.rate);
        name = findViewById(R.id.book_name);
        add = findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pb = findViewById(R.id.Loading);
                    getB();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public void getB() throws IOException {
        mQueue = Volley.newRequestQueue(UpdateRecord.this);
        pb.setVisibility(View.VISIBLE);
        String url = baseURL + "action=get";

        JsonObjectRequest request1 = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            pb.setVisibility(View.GONE);
                            JSONArray jsonArray = response.getJSONArray("books");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                if (!books.contains(jsonArray.getString(i)))
                                    books.add(jsonArray.getString(i));
                            }
                            addRecord();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request1);

    }

    public void addRecord(){
        if (quan.getText().toString().equals("") || price.getText().toString().equals("")
                || name.getText().toString().equals("")) {
            Toast.makeText(UpdateRecord.this, "All 3 fields are required",
                    Toast.LENGTH_LONG).show();
        } else if (books.contains(name.getText().toString())) {
            Toast.makeText(UpdateRecord.this, "Book already exist",
                    Toast.LENGTH_LONG).show();
            add.setEnabled(true);
        } else {
            add.setEnabled(false);
            String bName = "&bookName=" + name.getText().toString();
            String quantity = "&quantity=" + quan.getText();
            String rate = "&price=" + price.getText().toString();
            String action = "action=create";
            String url = baseURL + action + bName + rate + quantity;


            pb.setVisibility(View.VISIBLE);
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(UpdateRecord.this, response, Toast.LENGTH_LONG).show();
                    books.add(name.getText().toString());
                    add.setEnabled(true);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(UpdateRecord.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            })
            {@Override
            public RetryPolicy getRetryPolicy(){
                return new DefaultRetryPolicy(10000,0,0);
            }};
            mQueue = Volley.newRequestQueue(UpdateRecord.this);
            mQueue.add(request);
        }

    }



}