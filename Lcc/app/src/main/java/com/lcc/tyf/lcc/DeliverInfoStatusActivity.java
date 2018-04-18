package com.lcc.tyf.lcc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lcc.tyf.lcc.adapter.AdapterInfo;
import com.lcc.tyf.lcc.fragment.DatePickerFragment;
import com.lcc.tyf.lcc.models.Info;
import com.lcc.tyf.lcc.utils.HandlerSharedPreferences;
import com.lcc.tyf.lcc.utils.Urls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by max on 4/10/18.
 */

public class DeliverInfoStatusActivity extends ActionBarActivity implements View.OnClickListener, DatePickerFragment.EditDialogListener{

    private Urls urls;
    private ProgressDialog progressDialog;
    private ListView lv_finddocuments;
    private HandlerSharedPreferences hsp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliverstatusinfo);

        toolbar();
        widgets();
    }

    public void toolbar(){
        urls = new Urls();
        progressDialog = new ProgressDialog(this);
        lv_finddocuments = new ListView(this);
        hsp = new HandlerSharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        toolbar.setSubtitleTextColor(android.graphics.Color.WHITE);
    }

    public void widgets(){
        Button btn_date = (Button) findViewById(R.id.btn_date);

        btn_date.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_quit:
                finish();
                return true;

            case R.id.action_search:
                Intent intent = new Intent(DeliverInfoStatusActivity.this, DeliverInfoActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_statusdelivered, menu);
        return true;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_date:

                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");

                break;
            default:
                break;
        }
    }

    @Override
    public void updateResult(int year, int month, int day) {
        Toast.makeText(getApplicationContext(), String.valueOf(year), Toast.LENGTH_LONG).show();


        RequestQueue queue = Volley.newRequestQueue(this);
        String url = urls.getDeliveriesbycodeseller() + "?id=" + hsp.getSellerId() + "&date_search=" + String.valueOf(year) + "-" + String.valueOf(month) + "-" +String.valueOf(day);
        Log.v("DATA",url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObj = new JSONObject(response);
                    JSONArray jsonArray = jsonObj.getJSONArray("deliveries");
                    ArrayList<Info> values = new ArrayList<>();
                    for (int i=0;i < jsonArray.length(); i++){
                        values.add(new Info(
                                jsonArray.getJSONObject(i).getBoolean("success"),
                                jsonArray.getJSONObject(i).getString("status"),
                                jsonArray.getJSONObject(i).getString("motive"),
                                jsonArray.getJSONObject(i).getString("client"),
                                jsonArray.getJSONObject(i).getString("estimated_date"),
                                jsonArray.getJSONObject(i).getString("company"),
                                jsonArray.getJSONObject(i).getString("document_type"),
                                jsonArray.getJSONObject(i).getString("serie"),
                                jsonArray.getJSONObject(i).getString("document_number")
                        ));
                    }

                    AdapterInfo adapterInfo = new AdapterInfo(getApplicationContext(), values);
                    lv_finddocuments.setAdapter(adapterInfo);

                    /*
                    if(jsonObj.get("success").toString().equals("true")){
                        tv_package_status.setVisibility(View.VISIBLE);
                        tv_package_motive.setVisibility(View.VISIBLE);
                        tv_package_date.setVisibility(View.VISIBLE);
                        tv_package_client.setVisibility(View.VISIBLE);
                        tv_package_status.setText("Estado: " + jsonObj.getString("status"));
                        if(jsonObj.getString("motive").toString().equals("null")){
                            tv_package_motive.setText("Motivo: " );
                        }else{
                            tv_package_motive.setText("Motivo: " + jsonObj.getString("motive"));
                        }
                        tv_package_date.setText("Fecha: " + jsonObj.getString("estimated_date"));
                        tv_package_client.setText("Cliente: " + jsonObj.getString("client"));
                    }else{

                        tv_package_status.setText("Estado: " );
                        tv_package_motive.setText("Motivo: " );
                        tv_package_date.setText("Fecha: " );
                        tv_package_client.setText("Cliente: " );

                        Toast.makeText(DeliverInfoActivity.this, jsonObj.get("message").toString(),Toast.LENGTH_LONG).show();
                    }
                    */
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(DeliverInfoStatusActivity.this, "Error de conexion",Toast.LENGTH_LONG).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(DeliverInfoStatusActivity.this, "Error de conexion",Toast.LENGTH_LONG).show();
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(stringRequest);

    }
}
