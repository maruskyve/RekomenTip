package com.mkv.rekomentip

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.material.card.MaterialCardView
import java.util.*
import kotlin.math.roundToInt
import android.widget.EditText
import androidx.core.widget.NestedScrollView
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.NumberFormat


class MainActivity : AppCompatActivity() {
    //
    // Main
    //
    private lateinit var mainScrollView: NestedScrollView
    private lateinit var outputRecommendedTip: TextView
    private lateinit var inputPaymentNominal: EditText
    private lateinit var inputServiceRating: Spinner
    private lateinit var inputPaymentNominalErase: ImageButton
    private lateinit var computeNow: MaterialCardView
    private lateinit var commitCopyright: TextView
    private lateinit var commitInstantScroller: ImageButton

    private lateinit var model: Tsukamoto  // Class var assign
    private lateinit var paymentNominal: String
    private lateinit var serviceRatingNominal: String
    private var tipNominal: Int = 0

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        preps()

        inputPaymentNominalErase.setOnClickListener {
            inputPaymentNominal.removeTextChangedListener(inputPaymentNominalTextWatcher)
            inputPaymentNominal.setText("0")
            inputPaymentNominal.addTextChangedListener(inputPaymentNominalTextWatcher)
        }

        computeNow.setOnClickListener {
            inputCleaning()
            Log.i("position", mainScrollView.scrollY.toString())

            this.model = Tsukamoto(
                inputPaymentNominal = paymentNominal.replace(",", "").toFloat(),
                inputServiceRating = serviceRatingNominal.toFloat()
            )
            model.modelBuild()
            tipNominal = model.modelResult().roundToInt()
            tipNominal = ((tipNominal + 999) / 1000) * 1000  // Round up to nearest thousand

            outputRecommendedTip.text = resultToCurrency()
        }

        commitCopyright.setOnClickListener {
            val dUrl = resources.getString(R.string.d_url)
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(dUrl))
            startActivity(i)
        }

        commitInstantScroller.setOnClickListener {  // Operating instant scroll
            mainScrollView.post {
                mainScrollView.fullScroll(View.FOCUS_DOWN)  // For scroll position at bottom of scrollview
                mainScrollView.fullScroll(View.FOCUS_UP)  // For scroll position at top of scrollview
            }
        }

        mainScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            // Detect if scroll status (TOP/BOTTOM) by scroll position
            if (mainScrollView.getChildAt(0).bottom <= (mainScrollView.height + scrollY)) {  // If status = bottom then set icon image to arrow up
                Log.i("scroll status", "SCROLL AT BOTTOM")
                commitInstantScroller.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            } else if (scrollY == 0) {  // If status = top then set icon image to arrow down
                Log.i("scroll status", "SCROLL AT TOP")
                commitInstantScroller.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun preps() {
        //
        // Method run on onCreate of page
        // Method for preparing data or unconfigured component
        //

        mainScrollView = findViewById(R.id.main_scrollview)
        outputRecommendedTip = findViewById(R.id.recommended_tip)
        inputPaymentNominal = findViewById(R.id.payment_nominal)
        inputServiceRating = findViewById(R.id.service_rating)
        inputPaymentNominalErase = findViewById(R.id.payment_nominal_erase)
        computeNow = findViewById(R.id.commit_build_model)
        commitCopyright = findViewById(R.id.commit_copyright)
        commitInstantScroller = findViewById(R.id.commit_instant_scroller)

        // TextWatcher for add dot in nominal input text
        inputPaymentNominal.addTextChangedListener(inputPaymentNominalTextWatcher)

        // Set Service Rating Spinner values
        // Using spinner_item_style for each text style
        val ratingArray = resources.getStringArray(R.array.service_rating)
        val adapter = ArrayAdapter(applicationContext,
            R.layout.spinner_item_style, ratingArray)
        inputServiceRating.adapter = adapter
        inputServiceRating.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

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

        paymentNominal = inputPaymentNominal.text.toString()
        serviceRatingNominal = (inputServiceRating.selectedItemId + 1).toString()  // Id = 0-9
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

    private var inputPaymentNominalTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
        }

        override fun onTextChanged(str: CharSequence? /* actual string text */, start: Int /* str length */, before: Int, count: Int) {
            // Remove/disable TextWatcher method (onTextChanged()) to avoid recursive action on inputPaymentNominal
            inputPaymentNominal.removeTextChangedListener(this)

            try {
                var originalString: String = str.toString()
                if (originalString.contains(",")) {
                    originalString = originalString.replace(",".toRegex(), "")
                }
                val longval: Long = originalString.toLong()
                val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
                formatter.applyPattern("#,###,###,###")
                val formattedString: String = formatter.format(longval)

                //setting text after format to EditText
                inputPaymentNominal.setText(formattedString)
                inputPaymentNominal.setSelection(inputPaymentNominal.getText().length)
            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
            }

            // Own code temporary
//            val currencyUnitFloor = listOf(1000, 10000, 100000, 1000000)
//            val currencyUnitCeil = listOf(9999, 99999, 999999, 9999999)
//            val currencyUnitMultipleRestart = listOf(1000000, 1000000000)
//            var tmpStr = str.toString()
//
//            Log.i("info", "p0=${str}\tstart=${start}\tbefore=${before}\tcount=${count}")
//
//            // Dot character removal, e.g (13.000) -> 13000
//            tmpStr = tmpStr.replace(".", "")
//
//            // Dot adding on input text
//            if (tmpStr != "" && tmpStr.matches("-?\\d+(\\.\\d+)?".toRegex())) {  // Pass null input (error handler) and isNumber detecting
//                for (i in currencyUnitFloor.indices) {  // Iterating currency unit size
//                    if (tmpStr.toInt() in currencyUnitFloor[i]..currencyUnitCeil[i]) {  // Detect in range or not
//                        val paymentNominalStr = inputPaymentNominal.text.toString()
//                        val processedNominal= paymentNominalStr.substring(0, i+1) + "." +
//                                paymentNominalStr.substring(i+1, paymentNominalStr.length)
//                        inputPaymentNominal.setText(processedNominal)
//                        inputPaymentNominal.setSelection(inputPaymentNominal.text.length)
//
//                        break
//                    }
//                }
//            }
            // Own code temporary - end

            // Add/Enable textChangeListener to inputPaymentNominal
            inputPaymentNominal.addTextChangedListener(this)
        }
    }
}