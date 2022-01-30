package corp.umbrella.wifidirectapp.presentation.adapter

import android.net.wifi.p2p.WifiP2pDevice
import androidx.recyclerview.widget.RecyclerView
import corp.umbrella.wifidirectapp.databinding.ItemDeviceBinding

class DeviceViewHolder(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(device: WifiP2pDevice) {
        binding.deviceName.text = device.deviceName
    }
}