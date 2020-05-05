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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_exchange_rates);

        // Initialize a few UI components and our CurrencyUtils object
        final TextView exchangeRate = findViewById(R.id.dateSelected);
        final Button selectDate = findViewById(R.id.date);
        final CurrencyUtils mCurrencyUtilsObj = new CurrencyUtils(this);

        // Get the currency codes JsonObject
        JsonObject mCurrencyData = mCurrencyUtilsObj.getCurrencyCodes();

        // Initialize a few more UI components
        final Spinner toCurrency = findViewById(R.id.targetCurrency);
        final Spinner fromCurrency = findViewById(R.id.baseCurrency);

        // Populate the Spinners and initialize the RequestQueue.
        populateCurrencies(toCurrency, fromCurrency, mCurrencyData, this);
        final RequestQueue queue = Volley.newRequestQueue(this);

        // Create a DateSelectorDialog to pick the date for the exchange rate.
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Initialize a Calendar object for the DatePickerDialog
                final Calendar calendarInstance = Calendar.getInstance();
                final int day = calendarInstance.get(Calendar.DAY_OF_MONTH);
                final int month = calendarInstance.get(Calendar.MONTH);
                final int year = calendarInstance.get(Calendar.YEAR);

                // Finally create the DatePickerDialog
                final DatePickerDialog datePickerDialog = new DatePickerDialog(CurrencyExchangeRates.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int mYear, int mMonth, int dayOfMonth) {

                        // Get the currency codes of the currencies selected in the spinners.
                        final String countryCodeFrom = fromCurrency.getSelectedItem().toString().substring(0, 3).trim();
                        final String countryCodeTo = toCurrency.getSelectedItem().toString().substring(0, 3).trim();

                        // Save API calls by handling calls with the same currency codes.
                        if (countryCodeFrom.equals(countryCodeTo)) {
                            exchangeRate.setText(("Exchange Rate: " + 1 + " " + countryCodeFrom));
                        } else {
                            // Get the date and URL using our currencyUtils object.
                            String date = mCurrencyUtilsObj.convertDate(year, month + 1, day);
                            StringRequest stringRequest = new StringRequest(Request.Method.GET,
                                    mCurrencyUtilsObj.getHistoricCurrency(countryCodeFrom, countryCodeTo, date),
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
                                            exchangeRate.setText("Exchange Rate: " + convertedAmount);
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    String TAG = "WebAPIGetErrorExchange";
                                    try {
                                        int errorCode = error.networkResponse.statusCode;
                                        if (errorCode == 101) {
                                            // This specific error entails that we've reached the maximum number of API hits for the month.
                                            Log.e(TAG, "API Monthly limit reached.");
                                        }
                                    } catch (Exception e) {
                                        // Handle any error in getting a response. Can be viewed in the error section of logcat.
                                        Log.e(TAG, Objects.requireNonNull(error.getMessage()));
                                    }
                                }
                            });
                            // Add this request to the queue. Enables caching.
                            queue.add(stringRequest);
                        }
                    }
                }, day, month, year);
                // Set Minimum date to 1st Jan 1999 (API Maximum).
                datePickerDialog.getDatePicker().setMinDate(Long.parseLong("915192000000"));
                datePickerDialog.show();
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
