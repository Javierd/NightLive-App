package com.javierd.nightlive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * This class implements an activity which uses the network.
 * It displays a snackbar when there is no internet conection
 * available.
 */

public abstract class NetworkActivity extends AppCompatActivity {
    private ConnectionChangeReceiver mNetworkReceiver;

    private class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent ) {
            //Log.i("NETWORK", "Received");

            onNetworkChange();
        }
    }

    protected abstract void onNetworkChange();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNetworkReceiver = new ConnectionChangeReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //NetworkReceiver
        // In this case we cannot use a LocalBroadcastManager because the broadcast is not send from our app.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getBaseContext().registerReceiver(mNetworkReceiver , filter);
    }

    @Override
    protected void onPause() {
        getBaseContext().unregisterReceiver(mNetworkReceiver);

        super.onPause();
    }
}
