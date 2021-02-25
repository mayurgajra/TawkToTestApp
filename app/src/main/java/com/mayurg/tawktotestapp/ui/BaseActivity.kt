package com.mayurg.tawktotestapp.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar

/**
 * Base class for activities to implement common functionality
 */
open class BaseActivity : AppCompatActivity() {

    private val connectionLiveData = MutableLiveData<Boolean>()


    private val connectivityManager: ConnectivityManager by lazy { getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread {
                connectionLiveData.value = true
            }
        }

        override fun onLost(network: Network) {
            runOnUiThread {
                connectionLiveData.value = false
            }
        }
    }
    companion object {
        const val KEY_IS_LIVE_DATA_FROM_CONFIG_CHANGE = "KEY_IS_LIVE_DATA_FROM_CONFIG_CHANGE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
        }
        connectionLiveData.observe(this, {
            val isFromConfigChange =
                savedInstanceState?.getBoolean(KEY_IS_LIVE_DATA_FROM_CONFIG_CHANGE) ?: false
            if (isFromConfigChange) {
                savedInstanceState?.putBoolean(KEY_IS_LIVE_DATA_FROM_CONFIG_CHANGE, false)
                return@observe
            }
            onInternetChange(it)
        })
    }

    open fun onInternetChange(isAvailable: Boolean) {
        Log.d("MG-Base", "Internet change")
    }

    open fun showSnackBar(view: View, msg: String, action: String = "") {
        val snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
        if (action.isNotEmpty()) {
            snackbar.duration = Snackbar.LENGTH_INDEFINITE
            snackbar.setAction(action) {}
        }
        snackbar.show()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_LIVE_DATA_FROM_CONFIG_CHANGE, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }


}