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
import android.widget.Toast;

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
    /** CurrencyUtils object to be used within the class*/
    private CurrencyUtils mCurrencyObj = new CurrencyUtils(this);
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the JsonObject for the currency codes.
        JsonObject currencyCode = mCurrencyObj.getCurrencyCodes();

        // Initialize context and RequestQueue for Web GET requests
        final Context mContext = this;
        final RequestQueue queue = Volley.newRequestQueue(mContext);

        final Spinner fromCurrency = findViewById(R.id.inputSpinner);
        final Spinner toCurrency = findViewById(R.id.outputSpinner);
        final EditText amountToConvert = findViewById(R.id.inputAmount);

        // Initialize the submit button and set it as disabled.
        // This is done to avoid blank requests from being sent.
        final Button submitButton = findViewById(R.id.confirm);
        submitButton.setEnabled(false);

        // Initialize the Result TextView and set text color to black.
        final TextView out = findViewById(R.id.output);
        out.setTextColor(Color.BLACK);

        // Add a listener for any changes in the amount text box.
        // This is done to disable the text box if its blank.
        amountToConvert.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    submitButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (s.length() == 0 || Integer.parseInt(s.toString()) == 0) {
                        submitButton.setEnabled(false);
                    } else {
                        submitButton.setEnabled(true);
                    }
                } catch (NumberFormatException e) {
                    Log.e("NFE", e.getMessage());
                    Toast.makeText(mContext,
                            "The amount you've entered is too large to process. Please choose a smaller value. ",
                            Toast.LENGTH_SHORT).show();
                    submitButton.setEnabled(false);
                }
            }
        });

        // Call function to populate the Currency Spinners
        populateCurrencies(fromCurrency, toCurrency, currencyCode, mContext);

        // Handle Submit Button clicks.
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle clicks only if there is an amount in the amount text box.
                if (amountToConvert.getText().length() != 0) {
                    // Get the selected currencies from the spinners and get their Currency Codes.
                    final String countryCodeFrom = ((String) fromCurrency.getSelectedItem()).substring(0, 3);
                    final String countryCodeTo = ((String) toCurrency.getSelectedItem()).substring(0, 3);

                    // Do not waste API calls if the currency code is the same.
                    if (countryCodeFrom.contains(countryCodeTo)) {
                        out.setText("Converted Amount: " + amountToConvert.getText().toString().trim() + " " + countryCodeTo);
                    } else {
                        // Create a Web GET request to get the currency exchange rate from the server.
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, mCurrencyObj
                                .getConversionURL(countryCodeFrom, countryCodeTo),
                                new Response.Listener<String>() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void onResponse(String response) {
                                        // response is the Json Response from the server with the exchange rates

                                        // Get the converted amount using the CurrencyUtils Object
                                        String convertedAmount = mCurrencyObj.convertCurrency(response, countryCodeFrom, countryCodeTo, amountToConvert.getText().toString().trim());

                                        // Suffix the target Currency Code to the amount for better readability.
                                        convertedAmount = convertedAmount + " " + countryCodeTo;

                                        // Set the value of the result to the converted amount.
                                        out.setText("Converted Amount: " + convertedAmount);
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
                }
            }
        });
    }

    /**
     * Method to populate the spinners with the Json CurrencyCode object.
     * @param firstSpinner : Spinner One
     * @param secondSpinner : Spinner Two
     * @param currencyCode : The Json Currency Code object
     * @param mContext : Context of the calling activity.
     */
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
