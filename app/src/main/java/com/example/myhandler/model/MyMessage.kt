package com.example.myhandler.model

import android.os.Handler
import kotlin.properties.Delegates

class MyMessage {

    var what by Delegates.notNull<Int>()

    var arg1: Int? = null

    var arg2: Int? = null

    var target: MyHandler? = null

    var messenger: MyMessenger? = null

    var next: MyMessage? = null

    var whenTime: Long? = null

    var callBack: Runnable? = null

    private var isUse = false

    fun isInUse(): Boolean {
        return isUse
    }

    fun makeInUse() {
        isUse = true
    }

    fun recycle() {

    }

    fun isAsynchronous(): Boolean {
        return false
    }
}

class MyMessenger {

    private var mHandler: Handler? = null

    fun setHandler(handler: Handler) {
        mHandler = handler
    }

    fun sendMsg(msg: MyMessage) {
        mHandler?.sendEmptyMessage(1)
    }

}