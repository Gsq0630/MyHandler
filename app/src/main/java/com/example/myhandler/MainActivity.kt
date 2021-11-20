package com.example.myhandler

import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.myhandler.model.MyLooper
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val handler1 = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.d(TAG, "handleMessage1: msg = $msg")
            if (msg.what == 1001) {
                msg.arg2 = 20001
                Log.d(TAG, "handleMessage1: send")
                val aa = Message.obtain(msg)
                msg.replyTo.send(aa)
            }
        }
    }

    private val handler2 = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.d(TAG, "handleMessage2: msg = $msg")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        Thread(TextMyHandler).start()
//        TextMyHandler.run()

        thread {
            MyLooper.prepare()
            MyLooper.myLooper()?.loop()
        }

        Thread.sleep(200)
        thread {
            TextMyHandler
        }

//        val msg = Message().apply {
//            what = 1001
//            arg1 = 10001
//            replyTo = Messenger(handler2)
//        }
//        handler1.sendMessage(msg)
    }
}

















