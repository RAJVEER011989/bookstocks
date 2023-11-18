package com.example.googlesheetdemo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    //ExcelUtils excelUtils = new ExcelUtils();
    private ArrayList<String> books;
    private ArrayList<String> bookdetail;
    private RequestQueue mQueue;
    private RequestQueue rQueue;
    Dialog dialog;
    TextView selectBooks;
    TextView r2;
    TextView r3;
    TextView r4;
    TextView r5;
    TextView r6;
    Button viewRecord;
    Button refresh;
    RadioButton purchase;
    RadioButton sale;
    Button update;
    Button addRecord;
    TextView quantity;
    ProgressDialog progressBar;
    ProgressBar pb;
    int row;
    static int  count = 0 ;
    Integer totalTypesOfBooks;
    TextView totalsc;
    String baseURL = "https://script.google.com/macros/s/AKfycbyYDy43vhz1-lf-" +
            "YLuuztOhK0WCdQm5o5G2aNcx85MQA5M-300X2P3xl6L0QGJYyH0mnQ/exec?";
    ExecutorService executorService = Executors.newSingleThreadExecutor();



    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult activityResult) {
                            int result = activityResult.getResultCode();
                            Intent data = activityResult.getData();
                            totalsc.setText("");
                            refresh.setEnabled(false);
                            r2.setText("");
                            r3.setText("");
                            r4.setText("");
                            r5.setText("");
                            r6.setText("");
                            try {
                                selectBooks.setText("");
                                getBooks("Please Wait");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            getTotalStockCount();
                        }
                    }
            );

    public MainActivity() throws IOException {
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Books Stock Count");
        purchase = findViewById(R.id.pur);
        progressBar = new ProgressDialog(MainActivity.this);
        progressBar.setCancelable(false);
        progressBar.setMessage("Please wait....");
        progressBar.setProgress(0);
        progressBar.setMax(100);
        pb = findViewById(R.id.Loading);
        sale = findViewById(R.id.sale);
        mQueue = VolleySingleton.getInstance(MainActivity.this).getmQueue();
        selectBooks = findViewById(R.id.select_books);
        r2=findViewById(R.id.r2);
        r3=findViewById(R.id.r3);
        r4=findViewById(R.id.r4);
        r5=findViewById(R.id.r5);
        r6=findViewById(R.id.r6);
        refresh = findViewById(R.id.refresh);
        books = new ArrayList<>();
        bookdetail = new ArrayList<>();
        try {
            getBooks("Please Wait");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        refresh.setEnabled(false);
        getTotalStockCount();
        clickOnRefresh();


        //addBooksToListView();
        addBooksToListView1();
        //getTotalTypeOfBooks();
        //getTotalTypeOfBooks();
        /*try {
            getBookdetail();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        update = findViewById(R.id.update);
        quantity = findViewById(R.id.quan);
        clickOnUpdate();
        totalsc = findViewById(R.id.sc);
        addRecord = findViewById(R.id.add_record);
        addRecord();
    }

    public void getBooks(String progressB) throws IOException {
        books.clear();
        progressBar.setTitle(progressB);
        progressBar.show();
        String url = baseURL+"action=get";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("books");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                    books.add(jsonArray.getString(i));

                            }
                            progressBar.dismiss();
                       } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.dismiss();
                alertPopup(error.getMessage());
            }
        });
        mQueue.add(request);
    }

    public void updateData() {
        ExecutorService service = Executors.newSingleThreadExecutor();


        update.setEnabled(false);
        String url;
        progressBar.setTitle("Updating details....");
        progressBar.show();
        row = getRow(selectBooks.getText().toString());
        if (purchase.isChecked()) {
            url = baseURL+"action=purchase&&rowNumber="
                    + row + "&quantity="
                    + quantity.getText().toString() + "";
        } else {
            url = baseURL+"action=sale&&rowNumber="
                    + row + "&quantity="
                    + quantity.getText().toString() + "";
        }
        service.execute(new Runnable() {
            @Override
            public void run() {
                StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.dismiss();
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                        quantity.setText("");
                        getTotalStockCount();
                        getDetails();

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        quantity.setText("");
                    }
                }){@Override
                public RetryPolicy getRetryPolicy(){
                    return new DefaultRetryPolicy(10000,0,0);
                }};

                mQueue.add(request);
            }
        });

service.shutdown();

    }

    public void getTotalStockCount() {
        String url = baseURL+"action=stockcount";
        //pb.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pb.setVisibility(View.GONE);
                totalsc.setText(response);
                update.setEnabled(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        mQueue.add(request);

    }

    public int getRow(String bookName) {
        return books.indexOf(bookName) + 2;
    }

    public void addBooksToListView() {
            try {
                getBooks("Refreshing Book List...");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        selectBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    selectBooks.setText("");
                    getBooks("Please Wait...");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                    dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.dialog_searchable_spinner);

                    dialog.getWindow().setLayout(650, 800);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                    // Initialize and assign variable
                    EditText editText = dialog.findViewById(R.id.edit_text);
                    ListView listView = dialog.findViewById(R.id.list_view);

                    // Initialize array adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            MainActivity.this, android.R.layout.simple_list_item_1, books);
                    // set adapter
                    listView.setAdapter(adapter);
                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            adapter.getFilter().filter(s);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            // when item selected from list
                            // set selected item on textView
                            selectBooks.setText(adapter.getItem(position));

                            // Dismiss dialog
                            dialog.dismiss();
                            getDetails();
                        }
                    });


                }
            });
    }

    public void clickOnUpdate() {

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (quantity.getText().toString().equals("") || (!purchase.isChecked() && !sale.isChecked()) || selectBooks.getText().toString().equals("")) {
                        if (quantity.getText().toString().equals(""))
                            Toast.makeText(MainActivity.this, "Please add a Quantity ",
                                    Toast.LENGTH_LONG).show();

                        if (selectBooks.getText().toString().equals(""))
                            Toast.makeText(MainActivity.this,
                                    "Please select a Book", Toast.LENGTH_LONG).show();
                    } else {
                        updateData();
                    }
                }

        });

    }

    public void addRecord(){
        addRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addBookIntent = new Intent(MainActivity.this,UpdateRecord.class);
                activityResultLauncher.launch(addBookIntent);
            }
        });
    }

    public void getBookdetail() throws IOException {
        /*viewRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDetails();

            }
        });*/
        getDetails();
    }

    public void getDetails(){
        rQueue = Volley.newRequestQueue(MainActivity.this);
        bookdetail.clear();
        progressBar.setTitle("Fetching details....");
        progressBar.show();
        String url = baseURL+"action=detail&"+
                "rowNumber="+getRow(selectBooks.getText().toString())+"";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            pb.setVisibility(View.GONE);
                            JSONArray jsonArray = response.getJSONArray("details");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                bookdetail.add(jsonArray.getString(i));
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        r2.setText(bookdetail.get(1));
                        r3.setText(bookdetail.get(2));
                        r4.setText(bookdetail.get(3));
                        r5.setText(bookdetail.get(4));
                        r6.setText(bookdetail.get(5));

                        progressBar.dismiss();
                        refresh.setEnabled(true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });


        rQueue.add(request);

    }

    public void alertPopup(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // Set the message show for the Alert time

        builder.setMessage("Please check your Internet Connection turn it on and press Ok");

        // Set Alert Title
        builder.setTitle("Alert !");

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Ok", (DialogInterface.OnClickListener) (dialog, which) -> {
            // When the user click yes button then app will close
            dialog.cancel();
        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        alertDialog.show();
    }

    public void addBooksToListView1() {
        selectBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBooks.setText("");
                r2.setText("");
                r3.setText("");
                r4.setText("");
                r5.setText("");
                r6.setText("");
getTotalTypeOfBooks();


            }
        });
        }




    public void getTotalTypeOfBooks(){
        progressBar.setTitle("Please Wait....");
        progressBar.show();
        String url = baseURL+"action=articles";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String res = response;
               totalTypesOfBooks = Integer.valueOf(res);
               if(totalTypesOfBooks!=books.size()) {
                   try {
                       getBook1();
                   } catch (IOException e) {
                       throw new RuntimeException(e);
                   }
               }else {
                   progressBar.dismiss();
                   test();
               }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        mQueue.add(request);
    }
    public void test() {
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_searchable_spinner);

        dialog.getWindow().setLayout(2000, 3000);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        // Initialize and assign variable
        EditText editText = dialog.findViewById(R.id.edit_text);
        ListView listView = dialog.findViewById(R.id.list_view);

        // Initialize array adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                MainActivity.this, android.R.layout.simple_list_item_1, books);
        // set adapter
        listView.setAdapter(adapter);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // when item selected from list
                // set selected item on textView
                selectBooks.setText(adapter.getItem(position));

                // Dismiss dialog
                dialog.dismiss();
                getDetails();
            }
        });


    }

    public void getBook1() throws IOException {
        books.clear();
        progressBar.setTitle("Refreshing List....");
        progressBar.show();
        String url = baseURL+"action=get";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("books");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                books.add(jsonArray.getString(i));

                            }
                            progressBar.dismiss();
                            test();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.dismiss();
                alertPopup(error.getMessage());
            }
        });
        mQueue.add(request);
    }

public void clickOnRefresh(){
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectBooks.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,
                            "Please select a Book", Toast.LENGTH_LONG).show();
            } else {
                    getDetails();
            }

            }
        });
}

}