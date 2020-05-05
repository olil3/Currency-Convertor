package com.leboys.currencyconverter.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.InputStreamReader

private const val API_KEY = "503b8bbc7f6a2c5f640238f0978120ce"

class CurrencyUtils(private val mContext : Context) {
    fun getCurrencyCodes(): JsonObject? {
        var currencyData: JsonObject? = JsonObject()
        try {
            currencyData = JsonParser.parseReader(InputStreamReader(mContext.assets
                    .open("currency_codes.json")))
                    .asJsonObject
        } catch (ioe: Exception) {
            Log.e("MAIN", ioe.message!!)
        }
        return currencyData
    }
    fun getConversionURL(from: String, to: String): String? {
        return "http://data.fixer.io/api/latest?access_key=$API_KEY&symbols=$from,$to"
    }
    fun convertCurrency(currencyResponse: String, fromCode: String, toCode: String, amountToConvert: String): String {
        val responseAsJson: JsonObject = JsonParser.parseString(currencyResponse) as JsonObject
        val rates = responseAsJson.get("rates").asJsonObject
        val fromRate = rates.get(fromCode).asFloat
        val toRate = rates.get(toCode).asFloat
        return ((toRate / fromRate) * amountToConvert.toInt()).toString()
    }
}