package corp.umbrella.wifidirectapp

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import corp.umbrella.wifidirectapp.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private val wifiManager: WifiManager by lazy {
        applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, StartActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkWiFiStatus()

        binding.checkWifiStatus.setOnClickListener {
            checkWiFiStatus()
        }
    }

    private fun checkWiFiStatus() {
        if (wifiManager.isWifiEnabled) {
            startActivity(MainActivity.newIntent(this))
            finish()
        } else {
            Toast.makeText(this, "Включите Wi-Fi и нажмите на кнопку", Toast.LENGTH_LONG).show()
        }
    }
}