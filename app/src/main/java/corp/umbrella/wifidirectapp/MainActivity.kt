package corp.umbrella.wifidirectapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import corp.umbrella.wifidirectapp.adapter.DevicesAdapter
import corp.umbrella.wifidirectapp.databinding.ActivityMainBinding
import corp.umbrella.wifidirectapp.receiver.WiFiDirectBroadcastReceiver
import corp.umbrella.wifidirectapp.utils.ClientClass
import corp.umbrella.wifidirectapp.utils.ServerClass
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private var receiver: WiFiDirectBroadcastReceiver? = null

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

    val peerListListener by lazy {
        WifiP2pManager.PeerListListener {
            if (it.deviceList != adapter.getData()) {
                adapter.setData(it.deviceList.toList())
            }
            if (adapter.getData().isEmpty()) {
                showToast("Устройства не обнаружены")
            }
        }
    }

    val connectionInfoListener: WifiP2pManager.ConnectionInfoListener by lazy {
        WifiP2pManager.ConnectionInfoListener {
            if (it.groupFormed && it.isGroupOwner) {
                binding.connectionStatus.text = "Host (связь установлена)"
                isHost = true
                serverClass = ServerClass(binding.readMessage)
                serverClass?.start()
            } else if (it.groupFormed) {
                binding.connectionStatus.text = "Client (связь установлена)"
                isHost = false
                clientClass = ClientClass(it.groupOwnerAddress, binding.readMessage)
                clientClass?.start()
            }
        }
    }

    var serverClass: ServerClass? = null
    var clientClass: ClientClass? = null

    var isHost: Boolean? = null

    companion object {
        private const val REQUEST_CODE_LOCATION = 1
        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission()
        }

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
        channel = manager?.initialize(this, mainLooper, null)
        receiver = WiFiDirectBroadcastReceiver(manager, channel, this)

        binding.devicesRv.adapter = adapter

        binding.discoverButton.setOnClickListener {
            manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    binding.connectionStatus.text = "Поиск устройств"
                }

                override fun onFailure(i: Int) {
                    binding.connectionStatus.text = "Поиск устройств: ошибка"
                }
            })
        }

        adapter.onDeviceClickListener = { device ->
            val config = WifiP2pConfig()
            config.deviceAddress = device.deviceAddress

            manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    showToast("Установлена связь с ${device.deviceName}")
                }

                override fun onFailure(p0: Int) {
                    showToast("Ошибка при установке связи")
                }
            })
        }

        binding.buttonSend.setOnClickListener {
            val executor = Executors.newSingleThreadExecutor()
            val message = binding.writeMessage.text.toString()
            executor.execute {
                if (message.isNotBlank() && isHost == true) {
                    serverClass?.write(message.toByteArray())
                } else if (message.isNotBlank() && isHost == false) {
                    clientClass?.write(message.toByteArray())
                }
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

    override fun onStop() {
        manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                showToast("Соединение было разорвано")
                adapter.setData(listOf())
                binding.connectionStatus.text = "Нет активной связи"
            }

            override fun onFailure(p0: Int) {
                //ничего не делаем
            }
        })
        super.onStop()
    }

    private fun checkLocationPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //ничего не делаем
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
                //ничего не делаем
            } else {
                showToast("Для корректной работы приложения требуется дать необходимые разрешения")
                finish()
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    fun navigateToStartActivity() {
        startActivity(StartActivity.newIntent(this))
        finish()
    }
}