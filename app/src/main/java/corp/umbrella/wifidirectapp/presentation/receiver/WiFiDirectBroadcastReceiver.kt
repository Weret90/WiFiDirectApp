package corp.umbrella.wifidirectapp.presentation.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import androidx.core.app.ActivityCompat
import corp.umbrella.wifidirectapp.presentation.activities.MainActivity

class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager?,
    private val channel: WifiP2pManager.Channel?,
    private val activity: MainActivity,
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                    activity.navigateToStartActivity()
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                if (manager != null) {
                    if (ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ) {
                        manager.requestPeers(channel, activity.peerListListener)
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                if (manager != null) {
                    val networkInfo: NetworkInfo? = intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_NETWORK_INFO
                    )
                    networkInfo?.let {
                        if (networkInfo.isConnected) {
                            manager.requestConnectionInfo(channel, activity.connectionInfoListener)
                        } else {
                            activity.binding.connectionStatus.text = "Нет установленной связи"
                        }
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
            }
        }
    }
}