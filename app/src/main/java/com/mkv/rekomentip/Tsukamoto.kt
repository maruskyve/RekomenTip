package com.mkv.rekomentip

import kotlin.math.abs

class Tsukamoto(val inputPaymentNominal: Float, val inputServiceRating: Float) {
    //
    // Fuzzy Inference System (FIS) Model
    // Variable for Fuzzification
    //
    // Membership Function for Payment Nominal
    private val mFuncPaymentNominalLinguistic = mutableListOf<String>("LITTLE", "MORE")
    private val mFuncLittlePaymentNominal = mutableListOf<Int>(
        30000,
        2000000
    )  // at LITTLE linguistic set (curve_type=L_DOWN, boundary=(a,b))
    private val mFuncMorePaymentNominal = mutableListOf<Int>(
        30000,
        2000000
    )  // at MORE linguistic set (curve_type=L_UP, boundary=(a,b))
    private val mFuncPaymentNominalOutput = mutableListOf<Float>()

    // Membership Function for Service Rating
    private val mFuncServiceRatingLinguistics = mutableListOf<String>("BAD", "GOOD")
    private val mFuncBadServiceRating =
        mutableListOf<Int>(1, 10)  // at BAD linguistic set (curve_type=L_DOWN, boundary=(a,b))
    private val mFuncGoodServiceRating =
        mutableListOf<Int>(1, 10)  // at GOOD linguistic set (curve_type=L_UP, boundary=(a,b))
    private val mFuncServiceRatingOutput = mutableListOf<Float>()

    // Membership Function for Tip (crisp output)
    private val mFuncTipLinguistics = mutableListOf<String>("LOW", "HIGH")
    private val mFuncLowTip = mutableListOf<Float>(
        10000F,
        50000F
    )  // at LOW linguistic set (curve_type=L_DOWN, boundary=(a,b))
    private val mFuncHighTip = mutableListOf<Float>(
        10000F,
        50000F
    )  // at HIGH linguistic set (curve_type=L_UP, boundary=(a,b))
    private val mFuncTipOutput = mutableListOf<Float>()

    // Fuzzy output - output for top two membership above (PaymentNominal, Service Rating)
//    private var fOutPaymentNominal = mutableListOf<Float>()  // Value example (0.43) or ((0.64) & (0.43))
//    private var fOutServiceRating = mutableListOf<Float>()  // Value example (0.43) or ((0.64) & (0.43))

    //
    // Variable for inference
    //
    //
    private val ruleSet = listOf<String>(
        "IF PaymentNominal = LITTLE AND ServiceRating = BAD THEN Tip = LOW",
        "IF PaymentNominal = LITTLE AND ServiceRating = GOOD THEN Tip = HIGH",
        "IF PaymentNominal = MORE AND ServiceRating = GOOD THEN Tip = HIGH"
    )
    private val aPredicate = mutableListOf<Float>()
    private val zPredicate = mutableListOf<Float>()

    //
    // Variable for defuzzification
    //
    // Z-Predict
    private var zPredict: Float = 0F

    private fun fuzzification() {
        //
        // Compute fuzzy output for PaymentNominal output
        //
        // For LITTLE linguistic set
        mFuncPaymentNominalOutput.add(
            if (inputPaymentNominal < mFuncLittlePaymentNominal[0]) 1F
            else
                if (mFuncLittlePaymentNominal[0] < inputPaymentNominal && inputPaymentNominal < mFuncLittlePaymentNominal[1])
                    (mFuncLittlePaymentNominal[1] - inputPaymentNominal) / (mFuncLittlePaymentNominal[1] - mFuncLittlePaymentNominal[0])
                else 0F
        )

        // For MORE linguistic set
        mFuncPaymentNominalOutput.add(
            if (inputPaymentNominal < mFuncMorePaymentNominal[0]) 0F
            else
                if (mFuncMorePaymentNominal[0] < inputPaymentNominal && inputPaymentNominal < mFuncMorePaymentNominal[1])
                    (inputPaymentNominal - mFuncMorePaymentNominal[0]) / (mFuncMorePaymentNominal[1] - mFuncMorePaymentNominal[0])
                else 1F
        )

        //
        // Compute fuzzy output for ServiceRating output
        //
        // For BAD linguistic set
        mFuncServiceRatingOutput.add(
            if (inputServiceRating < mFuncBadServiceRating[0]) 1F
            else
                if (mFuncBadServiceRating[0] < inputServiceRating && inputServiceRating < mFuncBadServiceRating[1])
                    (mFuncBadServiceRating[1] - inputServiceRating) / (mFuncBadServiceRating[1] - mFuncBadServiceRating[0])
                else 0F
        )

        // For GOOD linguistic set
        mFuncServiceRatingOutput.add(
            if (inputServiceRating < mFuncGoodServiceRating[0]) 0F
            else
                if (mFuncGoodServiceRating[0] < inputServiceRating && inputServiceRating < mFuncGoodServiceRating[1])
                    (inputServiceRating - mFuncGoodServiceRating[0]) / (mFuncGoodServiceRating[1] - mFuncGoodServiceRating[0])
                else 1F
        )

    }

    private fun inference() {
        //
        // Inference for Tsukamoto method
        //

        aPredicate.add(
            minOf(
                mFuncPaymentNominalOutput[0],
                mFuncServiceRatingOutput[0]
            )
        )  // Consequence -> Tip = LOW
        aPredicate.add(
            minOf(
                mFuncPaymentNominalOutput[0],
                mFuncServiceRatingOutput[1]
            )
        )  // Consequence -> Tip = HIGH
        aPredicate.add(
            minOf(
                mFuncPaymentNominalOutput[1],
                mFuncServiceRatingOutput[1]
            )
        )  // Consequence -> Tip = HIGH

        zPredicate.add(abs((aPredicate[0] * abs(mFuncLowTip[0] - mFuncLowTip[1])) - mFuncLowTip[1]))  // Tip = LOW, curve_type = L_DOWN
        zPredicate.add(abs((aPredicate[1] * abs(mFuncHighTip[0] - mFuncHighTip[1])) - mFuncHighTip[1]))  // Tip = HIGH, curve_type = L_UP
        zPredicate.add(abs((aPredicate[2] * abs(mFuncHighTip[0] - mFuncHighTip[1])) + mFuncHighTip[0]))  // Tip = HIGH, curve_type = L_UP

    }

    private fun defuzzification() {
        //
        // Defuzification for Tsukamoto method
        //

        var azPredicateProduct = 0F
        val aPredicateSummation: Float = aPredicate.sum()

        for (i in 0 until aPredicate.size) {
            azPredicateProduct += aPredicate[i] * zPredicate[i]
        }

        zPredict = azPredicateProduct / aPredicateSummation

    }

    fun modelBuild() {
        fuzzification()
        inference()
        defuzzification()
    }

    fun modelResult(): Float {
        return zPredict
    }
}