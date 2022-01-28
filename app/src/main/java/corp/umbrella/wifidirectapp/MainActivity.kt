package corp.umbrella.wifidirectapp

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import corp.umbrella.wifidirectapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val wifiManager: WifiManager by lazy {
        applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    private val manager: WifiP2pManager by lazy {
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private val channel: WifiP2pManager.Channel by lazy {
        manager.initialize(this, mainLooper, null)
    }
    private val receiver: WiFiDirectBroadcastReceiver by lazy {
        WiFiDirectBroadcastReceiver(manager, channel, this)
    }
    private val intentFilter: IntentFilter by lazy {
        IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
    }

    companion object {
        private const val REQUEST_CODE_WIFI = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkWifiStatusAndInitButtonText()

        binding.wifiButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                startActivityForResult(panelIntent, REQUEST_CODE_WIFI)
            } else {
                wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
                checkWifiStatusAndInitButtonText()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WIFI) {
            checkWifiStatusAndInitButtonText()
        }
    }

    private fun checkWifiStatusAndInitButtonText() {
        if (wifiManager.isWifiEnabled) {
            binding.wifiButton.text = "WiFi On"
        } else {
            binding.wifiButton.text = "WiFi Off"
        }
    }
}