package com.leboys.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CurrencyExchangeRates extends AppCompatActivity {

    TextView mtv;
    Button selectDate;
    Calendar c;
    DatePickerDialog dpd;
    CurrencyUtils mCurrencyUtilsObj = new CurrencyUtils(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_exchange_rates);
        JsonObject mCurrencyData = mCurrencyUtilsObj.getCurrencyCodes();

        final Spinner toCurrency = findViewById(R.id.targetCurrency);
        final Spinner fromCurrency = findViewById(R.id.baseCurrency);

        populateCurrencies(toCurrency, fromCurrency, mCurrencyData, this);
        final RequestQueue queue = Volley.newRequestQueue(this);

        mtv = (TextView) findViewById(R.id.dateSelected);
        selectDate = (Button) findViewById(R.id.date);
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);
                int year = c.get(Calendar.YEAR);

                dpd = new DatePickerDialog(CurrencyExchangeRates.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int mYear, int mMonth, int dayOfMonth) {
                        final String countryCodeFrom = fromCurrency.getSelectedItem().toString().substring(0, 3).trim();
                        final String countryCodeTo = toCurrency.getSelectedItem().toString().substring(0, 3).trim();
                        String date = mYear + "-" + (mMonth + 1) + "-" + dayOfMonth;
                        String webUrl = mCurrencyUtilsObj.getHistoricCurrency(countryCodeFrom, countryCodeTo, date);
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, webUrl,
                                new Response.Listener<String>() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void onResponse(String response) {
                                        // response is the Json Response from the server with the exchange rates

                                        // Get the converted amount using the CurrencyUtils Object
                                        String convertedAmount = mCurrencyUtilsObj.convertCurrency(response, countryCodeFrom, countryCodeTo, String.valueOf(1));

                                        // Suffix the target Currency Code to the amount for better readability.
                                        convertedAmount = convertedAmount + " " + countryCodeTo;

                                        // Set the value of the result to the converted amount.
                                        mtv.setText("Converted Amount: " + convertedAmount);
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Handle any error in getting a response. Can be viewed in the error section of logcat.
                                Log.e("error", Objects.requireNonNull(error.getMessage()));
                            }
                        });
                        // Add this request to the queue. Enables caching.
                        queue.add(stringRequest);
                    }
                }, day, month, year);
                dpd.show();
            }
        });
    }
    private void populateCurrencies(final Spinner firstSpinner, final Spinner secondSpinner, final JsonObject currencyCode, final Context mContext) {
        // Transpose the JsonObject to a HashMap
        HashMap<String, String> currencyMap = new Gson().fromJson(currencyCode, HashMap.class);

        // Spinners need a list. Hence, run a loop to convert the entries of the HashMap to a String list.
        List<String> spinnerList = new ArrayList<>();
        for (HashMap.Entry<String, String> currencyData : currencyMap.entrySet()) {
            String value = currencyData.getKey() + " : " + currencyData.getValue();
            spinnerList.add(value.trim());
        }

        // Sort the List
        Collections.sort(spinnerList);

        // Create the adapters of the list. These go into the spinner.
        ArrayAdapter spinnerData = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, spinnerList);
        spinnerData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // If for any reason we have to pass a null value to the spinner parameter, we can handle it here.
        if (firstSpinner != null) {
            firstSpinner.setAdapter(spinnerData);
        }
        if (secondSpinner != null) {
            secondSpinner.setAdapter(spinnerData);
        }
    }
}
