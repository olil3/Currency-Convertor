package com.leboys.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.leboys.currencyconverter.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private CurrencyUtils mCurrencyObj = new CurrencyUtils(this);
    private RequestQueue queue;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JsonObject currencyCode = mCurrencyObj.getCurrencyCodes();
        final Context mContext = this;

        final Spinner fromCurrency = findViewById(R.id.inputSpinner);
        final TextView out = findViewById(R.id.output);
        out.setTextColor(Color.BLACK);
        final Spinner toCurrency = findViewById(R.id.outputSpinner);
        final EditText amountToConvert = findViewById(R.id.inputAmount);
        final Button submitButton = findViewById(R.id.confirm);
        submitButton.setEnabled(false);
        amountToConvert.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    submitButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    submitButton.setEnabled(false);
                } else {
                    submitButton.setEnabled(true);
                }
            }
        });

        populateCurrencies(fromCurrency, toCurrency, currencyCode, this);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amountToConvert.getText().length() != 0) {
                    queue = Volley.newRequestQueue(mContext);
                    final String countryCodeFrom = ((String) fromCurrency.getSelectedItem()).substring(0, 3);
                    final String countryCodeTo = ((String) toCurrency.getSelectedItem()).substring(0, 3);

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, mCurrencyObj
                            .getConversionURL(countryCodeFrom, countryCodeTo),
                            new Response.Listener<String>() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onResponse(String response) {
                                    String convertedAmount = mCurrencyObj.convertCurrency(response, countryCodeFrom, countryCodeTo, amountToConvert.getText().toString().trim());
                                    convertedAmount = convertedAmount + " " + countryCodeTo;
                                    out.setText("Converted Amount: " + convertedAmount);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("error", Objects.requireNonNull(error.getMessage()));
                        }
                    });
                    queue.add(stringRequest);
                }
            }
        });
    }
    private void populateCurrencies(final Spinner firstSpinner, final Spinner secondSpinner, final JsonObject currencyCode, final Context mContext) {
        HashMap<String, String> currencyMap = new Gson().fromJson(currencyCode, HashMap.class);
        List<String> spinnerList = new ArrayList<>();
        for (HashMap.Entry<String, String> currencyData : currencyMap.entrySet()) {
            String value = currencyData.getKey() + " : " + currencyData.getValue();
            spinnerList.add(value.trim());
        }
        Collections.sort(spinnerList);
        ArrayAdapter spinnerData = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, spinnerList);
        spinnerData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secondSpinner.setAdapter(spinnerData);
        firstSpinner.setAdapter(spinnerData);
    }
}
