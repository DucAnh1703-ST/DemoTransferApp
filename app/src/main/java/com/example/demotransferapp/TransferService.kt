package com.example.demotransferapp

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import com.example.demodatabase.IClientInterface
import com.example.demodatabase.Student
import com.google.gson.Gson

class TransferService : Service() {
    var command: String = ""
    private var aidlUI: IClientInterface? = null

    private val serviceUIConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service != null) {
                aidlUI = IClientInterface.Stub.asInterface(service)
                Log.d("TransferService", "onServiceConnected")
                // Có thể thực hiện các thao tác với aidlUI sau khi kết nối
            } else {
                Log.d("TransferService", "Service is null")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            aidlUI = null
        }
    }

    // Nếu lần đầu mà Service chưa được khởi tạo thì nó sẽ gọi hàm onCreate trước sau đó mới đến onStartCommand
    // nhưng vì là bất đồng bộ nên nó sẽ thực hiện hàm onStartCommand sau đó vì bindService thực hiện lâu hơn nên nó sẽ thực hiện sau.
    // Còn lần sau khi đã tạo Service rồi thì nó chỉ gọi onStartCommand thôi.

    override fun onCreate() {
        super.onCreate()
        bindUIService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        command = intent?.getStringExtra("command") ?: ""

        when (command) {
            "requestFirst100" -> {
                Log.d("TransferAppService", "Requesting First 100 students")

                // Lấy danh sách first100
                val first100 = aidlUI?.first100Students
                if (first100 != null) {
                    // Lưu danh sách first100 dưới dạng String JSON vào SharedPreferences
                    saveToSharedPreferences(applicationContext, "requestFirst100", first100)
                } else {
                    Log.d("TransferService", "First 100 students is null")
                }

                Log.d("TransferService", "onStartCommand: ${first100?.lastOrNull()}")
            }

            "request10BySubject" -> {
                val subject = intent?.getStringExtra("subject") ?: ""
                if (subject.isNotEmpty()) {
                    Log.d("TransferAppService", "Requesting 10 students by subject: $subject")

                    //
//                    request10BySubjectAsync(subject = subject)


                } else {
                    Log.d("TransferAppService", "No subject provided")
                }
            }

            "request10BySumA" -> {
                Log.d("TransferAppService", "Requesting 10 students by SumA")
            }

            "request10BySumB" -> {
                Log.d("TransferAppService", "Requesting 10 students by SumB")
            }

            "requestStudent" -> {
                Log.d("TransferAppService", "Requesting a specific student")
            }


            "resFirst100" -> {
                Log.d("TransferAppService", "Res First 100 students")
                // Do something here
            }

            "res10BySubject" -> {
                Log.d("TransferAppService", "Res 10 students by subject")
            }

            "res10BySumA" -> {
                Log.d("TransferAppService", "Res 10 students by SumA")
            }

            "res10BySumB" -> {
                Log.d("TransferAppService", "Res 10 students by SumB")
            }

            "resStudent" -> {
                Log.d("TransferAppService", "Res a specific student")
            }

            else -> {
                Log.d("TransferAppService", "Unknown command: $command")
            }
        }

        return START_STICKY
    }

    // Hàm lưu vào SharedPreferences dưới dạng String (JSON)
    private fun saveToSharedPreferences(context: Context, key: String, value: Any) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("TransferServicePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Chuyển đối tượng thành JSON string
        val json = Gson().toJson(value)

        // Lưu JSON string vào SharedPreferences
        editor.putString(key, json)
        editor.apply()

        Log.d("TransferService", "Saved $key to SharedPreferences: $json")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun bindUIService() {
        val intent = Intent("com.example.demodatabase.IClientInterface")  // Đảm bảo đúng action của app UI
        intent.setPackage("com.example.demouiapp")  // Thay bằng package của app UI
        bindService(intent, serviceUIConnection, BIND_AUTO_CREATE)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        unbindService(serviceUIConnection)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        if(aidlUI != null){
            unbindService(serviceUIConnection)
        }
        super.onDestroy()
    }
}
