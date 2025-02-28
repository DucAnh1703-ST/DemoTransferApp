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
//    private val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("TransferServicePrefs", Context.MODE_PRIVATE)

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
                Log.d("TransferAppService", "Requesting First 100 Students")

                // Lấy danh sách first100
                val first100 = aidlUI?.first100Students
                if (first100 != null) {
                    // Lưu danh sách first100 dưới dạng String JSON vào SharedPreferences
                    saveToSharedPreferences("requestFirst100", first100)
                } else {
                    Log.d("TransferService", "First 100 students is null")
                }

                Log.d("TransferService", "onStartCommand: ${first100?.lastOrNull()}")
            }

            "request10BySubject" -> {
                val subject = intent?.getStringExtra("subject")
                Log.d("TransferAppService", "Requesting 10 students by Subject")

                // Lấy danh sách first100
                val first10BySubject = aidlUI?.getTop10StudentsBySubject(subject)
                if (first10BySubject != null) {
                    // Lưu danh sách first100 dưới dạng String JSON vào SharedPreferences
                    saveToSharedPreferences("request10BySubject", first10BySubject)
                } else {
                    Log.d("TransferService", "Get 10 students by Subject is null")
                }

//                Log.d("TransferService", "onStartCommand: ${first10BySubject?.lastOrNull()}")
            }

            "request10BySumA" -> {
                val city = intent?.getStringExtra("city")
                Log.d("TransferAppService", "Requesting 10 studentsA by City")

                // Lấy danh sách first100
                val first10AByCity = aidlUI?.getTop10StudentsBySumA(city)
                if (first10AByCity != null) {
                    // Lưu danh sách first100 dưới dạng String JSON vào SharedPreferences
                    saveToSharedPreferences("request10BySumA", first10AByCity)
                } else {
                    Log.d("TransferService", "Get 10 studentsA by City is null")
                }

//                Log.d("TransferService", "onStartCommand: ${first10BySubject?.lastOrNull()}")
            }

            "request10BySumB" -> {
                val city = intent?.getStringExtra("city")
                Log.d("TransferAppService", "Requesting 10 studentsB by City")

                // Lấy danh sách first100
                val first10BByCity = aidlUI?.getTop10StudentsBySumB(city)
                if (first10BByCity != null) {
                    // Lưu danh sách first100 dưới dạng String JSON vào SharedPreferences
                    saveToSharedPreferences("request10BySumB", first10BByCity)
                } else {
                    Log.d("TransferService", "Get 10 studentsB by City is null")
                }

//                Log.d("TransferService", "onStartCommand: ${first10BySubject?.lastOrNull()}")
            }

            "requestStudent" -> {
                val city = intent?.getStringExtra("city")
                val name = intent?.getStringExtra("name")
                Log.d("TransferAppService", "Requesting students by Firstname and City")

                // Lấy danh sách first100
                val studentByPriority = aidlUI?.getStudentByPriority(name, city)
                if (studentByPriority != null) {
                    // Lưu danh sách first100 dưới dạng String JSON vào SharedPreferences
                    saveToSharedPreferences("requestStudent", studentByPriority)
                } else {
                    Log.d("TransferService", "Get students by Firstname and City is null")
                }

//                Log.d("TransferService", "onStartCommand: ${first10BySubject?.lastOrNull()}")
            }

            "resFirst100" -> {
                removeValues("requestFirst100")
            }

            "res10BySubject" -> {
                removeValues("request10BySubject")
            }

            "res10BySumA" -> {
                removeValues("request10BySumA")
            }

            "res10BySumB" -> {
                removeValues("request10BySumB")
            }

            "resStudent" -> {
                removeValues("requestStudent")
            }

            else -> {
                Log.d("TransferAppService", "Unknown command: $command")
            }
        }

        return START_STICKY
    }

    // Hàm lưu vào SharedPreferences dưới dạng String (JSON)
    private fun saveToSharedPreferences(key: String, value: Any) {
        val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("TransferServicePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Chuyển đối tượng thành JSON string
        val json = Gson().toJson(value)

        // Lưu JSON string vào SharedPreferences
        editor.putString(key, json)
        editor.apply()

        Log.d("TransferService", "Saved $key to SharedPreferences: $json")
    }

    private fun removeValues(vararg keys: String) {
        val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("TransferServicePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        for (key in keys) {
            editor.remove(key)
        }
        editor.apply()

        Log.d("TransferService", "Move ${keys.joinToString(", ")} success")
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
