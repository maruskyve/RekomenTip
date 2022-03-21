package com.mkv.rekomentip

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.material.card.MaterialCardView
import org.w3c.dom.Text
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    //
    // Main
    //
    private lateinit var outputRecommendedTip: TextView
    private lateinit var inputPaymentNominal: EditText
    private lateinit var inputServiceRating: Spinner
    private lateinit var computeNow: MaterialCardView
    private lateinit var commitCopyright: TextView

    private lateinit var model: Tsukamoto  // Class var assign
    private var paymentNominal: Float = 0F
    private var serviceRatingNominal: Float = 0F
    private var tipNominal: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        preps()

        computeNow.setOnClickListener {
            inputCleaning()

            this.model = Tsukamoto(
                inputPaymentNominal = paymentNominal.toString().toFloat(),
                inputServiceRating = serviceRatingNominal.toString().toFloat()
            )
            model.modelBuild()
            tipNominal = model.modelResult().roundToInt()
            tipNominal = ((tipNominal + 999) / 1000) * 1000  // Round up to nearest thousand

            resultToCurrency()
            outputRecommendedTip.text = tipNominal.toString()
        }

        commitCopyright.setOnClickListener {
            val dUrl = resources.getString(R.string.d_url)
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(dUrl))
            startActivity(i)
        }
    }

    @SuppressLint("ResourceType")
    private fun preps() {
        //
        // Method run on onCreate of page
        // Method for preparing data or unconfigured component
        //

        outputRecommendedTip = findViewById(R.id.recommended_tip)
        inputPaymentNominal = findViewById(R.id.payment_nominal)
        inputServiceRating = findViewById(R.id.service_rating)
        computeNow = findViewById(R.id.commit_build_model)
        commitCopyright = findViewById(R.id.commit_copyright)

        // Set Service Rating Spinner values
        // Using spinner_item_style for each text style
        val ratingArray = resources.getStringArray(R.array.service_rating)
        val adapter = ArrayAdapter(applicationContext,
            R.layout.spinner_item_style, ratingArray)
        inputServiceRating.adapter = adapter
        inputServiceRating.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun inputCleaning() {
        //
        // Method run after "Compute Now" button pressed
        // Cleaning input for unwanted noise/character including (dots(.), commas(,), stripes(-), whitespace( ))
        //

        paymentNominal = inputPaymentNominal.text.toString().toFloat()
        serviceRatingNominal = (inputServiceRating.selectedItemId + 1).toFloat()  // Id = 0-9
    }

    private fun resultToCurrency(): String {
        //
        // Method run after fuzzy model initialization
        // Its used to Convert result (tipNominal) into currency
        //

        val tipNominalStr: String = tipNominal.toString()
        var currency = "Rp."
        var currencyNominal = currency + ""

        Log.i("tipNominalStr", tipNominalStr)
        Log.i("tipNominalStrLen", tipNominalStr.length.toString())

        for (i in 0 until tipNominalStr.length) {  // Iterating tipNominal for each of it character
            currencyNominal += tipNominalStr[i]

            if (tipNominal in 10000..100000) {
                if (i == 1) {
                    currencyNominal += "."
                }
            }

        }
        currencyNominal += ",00,-"
        return currencyNominal
    }
}