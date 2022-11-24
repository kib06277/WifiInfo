package com.example.wifiinfo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    //基本宣告
    lateinit var Show: TextView
    private var time = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Show = findViewById(R.id.Show)
        if(Checkwifi(this)){
            try {
                timeupdate()
            } catch (e : Exception){
                Show.text = e.message
                Log.i("Error","e：$e")
            }
        } else {
            Show.text = "並未開啟 wifi"
        }
    }

    //檢查網路狀態
    @Suppress("DEPRECATION")
    fun Checkwifi(context: Context): Boolean {
        var result = false
        //獲得伺服器狀況
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }

    //定時更新
    fun timeupdate(){
        //呼叫 Handler 變更畫面
        val handler = Handler { msg: Message ->

            when (msg.what) {
                //設定 recyclerView
                1 -> {
                    val wifi_service = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifi_service.connectionInfo
                    if (wifiInfo != null) {
                        val rssi = abs(wifiInfo.rssi)
                        Show.text = "網路穩定度：$rssi"
                    }
                }
                2 -> {
                    Show.text = "沒有網路"
                }
            }
            true
        }

        //計時器
        time.scheduleAtFixedRate( object : TimerTask() {
            override fun run() {
                try {
                    //檢查網路狀態
                    if(Checkwifi(this@MainActivity)){
                        handler.sendEmptyMessage(1) //刷新 RecycleView
                    } else {
                        handler.sendEmptyMessage(2) //網路異常
                    }
                } catch (e : Exception) {
                    Log.i("Error" , "e = $e")
                }
            }
        }, 0, 1000)
    }

}