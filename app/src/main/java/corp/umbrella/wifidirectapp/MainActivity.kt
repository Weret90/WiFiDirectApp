package corp.umbrella.wifidirectapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import corp.umbrella.wifidirectapp.adapter.DevicesAdapter
import corp.umbrella.wifidirectapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val wifiManager: WifiManager by lazy {
        applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    private val manager: WifiP2pManager? by lazy {
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }
    private val channel: WifiP2pManager.Channel? by lazy {
        manager?.initialize(this, mainLooper, null)
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
    private val adapter: DevicesAdapter by lazy {
        DevicesAdapter()
    }
    private var peers: MutableList<WifiP2pDevice> = mutableListOf()

    val peerListListener by lazy {
        WifiP2pManager.PeerListListener {
            if (!it.deviceList.equals(peers)) {
                peers.clear()
                peers.addAll(it.deviceList)
                adapter.setData(peers)
            }
            if (peers.size == 0) {
                showToast("Devices Not Found")
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_WIFI = 0
        private const val REQUEST_CODE_LOCATION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission()
        }

        checkWifiStatusAndInitButtonText()

        binding.devicesRv.adapter = adapter

        binding.wifiButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                startActivityForResult(panelIntent, REQUEST_CODE_WIFI)
            } else {
                wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
                checkWifiStatusAndInitButtonText()
            }
        }

        binding.discoverButton.setOnClickListener {
            manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    binding.connectionStatus.text = "Discovery Started"
                }

                override fun onFailure(i: Int) {
                    binding.connectionStatus.text = "Discovery Starting Failed"
                }
            })
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

    private fun checkLocationPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showToast("Разрешения были даны ранее")
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Разрешения даны только что")
            } else {
                showToast("Для корректной работы приложения требуется дать необходимые разрешения")
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}