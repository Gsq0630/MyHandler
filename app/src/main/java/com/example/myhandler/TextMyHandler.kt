package com.example.myhandler

import android.util.Log
import com.example.myhandler.model.MyHandler
import com.example.myhandler.model.MyLooper
import com.example.myhandler.model.MyMessage
import kotlin.concurrent.thread

object TextMyHandler : Runnable{


    private const val TAG = "TextMyHandler"

    init {

    }

    val myHandler = object : MyHandler(MyLooper.myLooper()!!) {
        var i = 0
        override fun handleMsg(msg: MyMessage) {
            super.handleMsg(msg)
            Log.d(TAG, "handleMsg: msg = ${msg.what}  iiiii = ${i++}")
        }
    }



    fun main() {
        Log.d(TAG, "main: ")

    }

    override fun run() {
        thread {
            Log.d(TAG, "run: 1111")

            for (i in 0..100) {
                val msg = MyMessage().apply {
                    what = i
                }
                myHandler.sendMsg(msg)
            }
            Log.d(TAG, "run: 2222")
        }

    }



}