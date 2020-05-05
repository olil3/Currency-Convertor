package com.leboys.currencyconverter.utils

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.InputStreamReader

/** API Key for Fixer.io*/
private const val API_KEY = "503b8bbc7f6a2c5f640238f0978120ce"

/** Domain of our API GET Requests*/
private const val DOMAIN_ID = "http://data.fixer.io/api"

/**
 * Helper class to help aid the process of converting currencies.
 * @param mContext : context of the class creating the object.
 */
class CurrencyUtils(private val mContext : Context) {
    /**
     * Function to get the currency_codes.json file as a Json Object (Google GSon)
     * @return a JsonObject represent the .json file
     */
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

    /**
     * Function returns the URL for Web Requests for current exchange rates.
     * @param from : Currency code of the base currency
     * @param to : Currency code of the target currency
     * @return the URL for Web Requests
     */
    fun getConversionURL(from: String, to: String): String? {
        return "$DOMAIN_ID/latest?access_key=$API_KEY&symbols=$from,$to"
    }

    /**
     * Function converts string responses from GET requests for currency data into the converted value
     * @param currencyResponse : the response from the GET request
     * @param fromCode : Currency code of the base currency
     * @param toCode : Currency code of the target currency
     * @param amountToConvert : Value of the amount to convert from base to target.
     * If using for historic currency, pass amountToCovert as "1".
     * @return converted currency based on the amount given
     */
    fun convertCurrency(currencyResponse: String, fromCode: String, toCode: String, amountToConvert: String): String {
        val responseAsJson: JsonObject = JsonParser.parseString(currencyResponse) as JsonObject
        val rates = responseAsJson.get("rates").asJsonObject
        val fromRate = rates.get(fromCode).asFloat
        val toRate = rates.get(toCode).asFloat
        return ((toRate / fromRate) * amountToConvert.toInt()).toString()
    }

    /**
     * Function returns the URL for Web Request for historic exchange rates.
     * @param from : Currency code of base currency
     * @param to : Currency code of target currency
     * @param date : Date of exchange rate. Format: YYYY-MM-DD
     * @return the URL for historic exchange rate Web Requests
     */
    fun getHistoricCurrency(from: String, to: String, date: String): String {
        return "$DOMAIN_ID/$date?access_key=$API_KEY&symbols=$from,$to"
    }

    /**
     * Function to return a date in the form of a string.
     * @param year : Year of the date.
     * @param month : Month of the date.
     * @param day : Day of the date.
     * @return date in the format YYYY-MM-DD
     */
    fun convertDate(year:Int, month:Int, day:Int): String {
        var toReturn = year.toString()
        toReturn += if (month < 10) {
            "-0$month"
        } else {
            "-$month"
        }
        toReturn += if (day < 10) {
            "-0$day"
        } else  {
            "-$day"
        }
        return toReturn
    }
}