package corp.umbrella.wifidirectapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import corp.umbrella.wifidirectapp.adapter.DevicesAdapter
import corp.umbrella.wifidirectapp.databinding.ActivityMainBinding
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }
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

    val connectionInfoListener: WifiP2pManager.ConnectionInfoListener by lazy {
        WifiP2pManager.ConnectionInfoListener {
            val groupOwnerAddress = it.groupOwnerAddress
            if (it.groupFormed && it.isGroupOwner) {
                binding.connectionStatus.text = "Host"
                isHost = true
                serverClass = ServerClass()
                serverClass?.start()
            } else if (it.groupFormed) {
                binding.connectionStatus.text = "Client"
                isHost = false
                clientClass = ClientClass(groupOwnerAddress)
                clientClass?.start()
            }
        }
    }

    var socket: Socket? = null

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

        channel = manager?.initialize(this, mainLooper, null)
        receiver = WiFiDirectBroadcastReceiver(manager, channel, this)

        binding.devicesRv.adapter = adapter

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

        adapter.onDeviceClickListener = { device ->
            val config = WifiP2pConfig()
            config.deviceAddress = device.deviceAddress

            manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    showToast("Connected to ${device.deviceName}")
                }

                override fun onFailure(p0: Int) {
                    showToast("Connected failure")
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

    inner class ServerClass : Thread() {

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        fun write(bytes: ByteArray) {
            try {
                outputStream?.write(bytes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun run() {
            try {
                val serverSocket = ServerSocket(8888)
                socket = serverSocket.accept()
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            executor.execute {
                val buffer = ByteArray(1024)
                var bytes: Int?
                while (socket != null) {
                    try {
                        bytes = inputStream?.read(buffer)
                        if (bytes != null && bytes > 0) {
                            handler.post {
                                val tempMessage = String(buffer, 0, bytes)
                                binding.readMessage.text = tempMessage
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    inner class ClientClass(private val hostAddress: InetAddress) : Thread() {

        var hostAdd: String? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        init {
            hostAdd = hostAddress.hostAddress
            socket = Socket()
        }

        fun write(bytes: ByteArray) {
            try {
                outputStream?.write(bytes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun run() {
            try {
                socket?.connect(InetSocketAddress(hostAdd, 8888), 500)
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            executor.execute {
                val buffer = ByteArray(1024)
                var bytes: Int?
                while (socket != null) {
                    try {
                        bytes = inputStream?.read(buffer)
                        if (bytes != null && bytes > 0) {
                            handler.post {
                                val tempMessage = String(buffer, 0, bytes)
                                binding.readMessage.text = tempMessage
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun navigateToStartActivity() {
        startActivity(StartActivity.newIntent(this))
        finish()
    }
}