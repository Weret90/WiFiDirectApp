package corp.umbrella.wifidirectapp.presentation.activities

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
import corp.umbrella.wifidirectapp.R
import corp.umbrella.wifidirectapp.presentation.adapter.DevicesAdapter
import corp.umbrella.wifidirectapp.databinding.ActivityMainBinding
import corp.umbrella.wifidirectapp.domain.entity.Note
import corp.umbrella.wifidirectapp.presentation.adapter.NotesAdapter
import corp.umbrella.wifidirectapp.presentation.receiver.WiFiDirectBroadcastReceiver
import corp.umbrella.wifidirectapp.presentation.utils.ClientClass
import corp.umbrella.wifidirectapp.presentation.utils.ServerClass
import corp.umbrella.wifidirectapp.presentation.viewmodel.MainActivityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
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
    private val deviceAdapter: DevicesAdapter by lazy {
        DevicesAdapter()
    }

    private val notesAdapter: NotesAdapter by lazy {
        NotesAdapter()
    }

    private val viewModel: MainActivityViewModel by viewModel()

    val peerListListener by lazy {
        WifiP2pManager.PeerListListener {
            if (it.deviceList != deviceAdapter.getData()) {
                deviceAdapter.setData(it.deviceList.toList())
            }
            if (deviceAdapter.getData().isEmpty()) {
                showToast(getString(R.string.devices_not_found))
            }
        }
    }

    val connectionInfoListener: WifiP2pManager.ConnectionInfoListener by lazy {
        WifiP2pManager.ConnectionInfoListener {
            if (it.groupFormed && it.isGroupOwner) {
                binding.connectionStatus.text = getString(R.string.connection_status_host)
                isHost = true
                serverClass = ServerClass(this)
                serverClass?.start()
            } else if (it.groupFormed) {
                binding.connectionStatus.text = getString(R.string.connection_status_client)
                isHost = false
                clientClass = ClientClass(it.groupOwnerAddress, this)
                clientClass?.start()
            }
        }
    }

    private var serverClass: ServerClass? = null
    private var clientClass: ClientClass? = null
    private var isHost: Boolean? = null

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

        initListeners()
        initReceiver()

        binding.devicesRv.adapter = deviceAdapter
        binding.readMessageRv.adapter = notesAdapter

        viewModel.getNotes().observe(this) {
            notesAdapter.setData(it)
        }
    }

    private fun initReceiver() {
        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
        channel = manager?.initialize(this, mainLooper, null)
        receiver = WiFiDirectBroadcastReceiver(manager, channel, this)
    }

    private fun initListeners() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission()
        }

        binding.discoverButton.setOnClickListener {
            manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    binding.connectionStatus.text = getString(R.string.search_devices_in_process)
                }

                override fun onFailure(i: Int) {
                    binding.connectionStatus.text = getString(R.string.search_devices_failure)
                }
            })
        }

        binding.buttonSend.setOnClickListener {
            val executor = Executors.newSingleThreadExecutor()
            val message = binding.writeMessage.text.toString()
            executor.execute {
                if (message.isNotBlank() && isHost == true) {
                    saveOutputMessage(message)
                    serverClass?.write(message.toByteArray())
                } else if (message.isNotBlank() && isHost == false) {
                    saveOutputMessage(message)
                    clientClass?.write(message.toByteArray())
                }
            }
        }

        binding.buttonClearMessages.setOnClickListener {
            viewModel.deleteNotes()
        }

        deviceAdapter.onDeviceClickListener = { device ->
            val config = WifiP2pConfig()
            config.deviceAddress = device.deviceAddress

            manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    showToast(
                        String.format(
                            getString(R.string.connection_success), device.deviceName
                        )
                    )
                }

                override fun onFailure(p0: Int) {
                    showToast(
                        getString(R.string.connection_failure)
                    )
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

    override fun onStop() {
        manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                showToast(getString(R.string.connection_is_disable))
                deviceAdapter.setData(listOf())
                binding.connectionStatus.text = getString(R.string.connection_status_zero)
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
                showToast(getString(R.string.message_permission_denied))
                finish()
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    fun saveInputMessage(message: String) {
        val note = Note(fromWho = getString(R.string.label_input_message), text = message)
        viewModel.saveNote(note)
    }

    private fun saveOutputMessage(message: String) {
        val note = Note(fromWho = getString(R.string.label_output_message), text = message)
        viewModel.saveNote(note)
    }

    fun navigateToStartActivity() {
        startActivity(StartActivity.newIntent(this))
        finish()
    }
}