package com.example.myhandler.model

import android.util.Log


open class MyHandler(
    val looper: MyLooper
    ){

    companion object {
        private const val TAG = "MyHandler"
        fun handleCallMsg(msg: MyMessage) {
            msg.callBack?.run()
        }
    }

    var msgQueue: MyMessageQueue? = null

    init {
        msgQueue = looper.msgQueue
    }

    fun sendMsg(msg: MyMessage, time: Long = 0) {
        Log.d(TAG, "sendMsg: ")
        msg.target = this
        msgQueue!!.enqueueMessage(msg, time)
    }

    fun dispatchMessage(msg: MyMessage) {
        if (msg.callBack != null) {
            handleCallMsg(msg)
        } else {
            handleMsg(msg)
        }
    }

    open fun handleMsg(msg: MyMessage) {

    }

}