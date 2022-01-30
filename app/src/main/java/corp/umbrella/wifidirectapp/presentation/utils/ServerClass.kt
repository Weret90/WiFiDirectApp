package corp.umbrella.wifidirectapp.presentation.utils

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import corp.umbrella.wifidirectapp.presentation.activities.MainActivity
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class ServerClass(private val activity: MainActivity) : Thread() {

    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var socket: Socket? = null

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