package com.mayurg.tawktotestapp.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mayurg.tawktotestapp.R
import jp.wasabeef.glide.transformations.gpu.InvertFilterTransformation

/**
 * Extension function for imageview to load remote image URL resources easily
 */
fun ImageView.loadImage(url: String) {
    Glide.with(this)
        .load(url)
        .thumbnail(0.5f)
        .apply(RequestOptions.placeholderOf(R.drawable.tawk_logo))
        .apply(RequestOptions.errorOf(R.drawable.tawk_logo))
        .into(this)
}

/**
 * Extension function for imageview to load remote image URL resources with inverted filter easily
 */
fun ImageView.loadImageInverted(url: String) {
    Glide.with(this)
        .load(url)
        .thumbnail(0.5f)
        .apply(RequestOptions.bitmapTransform(InvertFilterTransformation()))
        .apply(RequestOptions.placeholderOf(R.drawable.tawk_logo))
        .apply(RequestOptions.errorOf(R.drawable.tawk_logo))
        .into(this)
}

/**
 * Just a safe setter function for textview to set N/A for empty or null data
 */
fun TextView.setSafeText(string: String?) {
    text = if (string.isNullOrEmpty()) {
        "N/A"
    } else {
        string
    }
}

/**
 * Extension function to hide the keyboard from any view
 */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}