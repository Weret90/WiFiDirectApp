package corp.umbrella.wifidirectapp.presentation.adapter

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import corp.umbrella.wifidirectapp.databinding.ItemDeviceBinding

class DevicesAdapter: RecyclerView.Adapter<DeviceViewHolder>() {

    private var devices: List<WifiP2pDevice> = listOf()
    var onDeviceClickListener: ((WifiP2pDevice) -> Unit)? = null

    fun setData(devices: List<WifiP2pDevice>) {
        this.devices = devices
        notifyDataSetChanged()
    }

    fun getData(): List<WifiP2pDevice> {
        return devices.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.bind(device)
        holder.itemView.setOnClickListener {
            onDeviceClickListener?.invoke(device)
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}