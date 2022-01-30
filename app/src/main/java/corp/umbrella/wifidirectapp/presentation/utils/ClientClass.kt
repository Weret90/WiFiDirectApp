package corp.umbrella.wifidirectapp.presentation.utils

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import corp.umbrella.wifidirectapp.presentation.activities.MainActivity
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors

class ClientClass(hostAddress: InetAddress, private val activity: MainActivity) : Thread() {

    private var hostAdd: String? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var socket: Socket? = null

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
                            activity.saveInputMessage(tempMessage)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}