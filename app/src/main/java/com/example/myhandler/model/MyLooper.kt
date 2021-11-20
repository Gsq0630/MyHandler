package com.example.myhandler.model

import android.os.Binder
import android.util.Log
import java.lang.RuntimeException

class MyLooper constructor(){

    companion object {
        val threadLocal = MyThreadLocal<MyLooper>()
        private const val TAG = "MyLooper"

        fun myLooper(): MyLooper? {
            Log.d(TAG, "myLooper: ")
            return threadLocal.get()
        }

        fun prepare(quitAllowed: Boolean = true) {
            Log.d(TAG, "prepare: ")
            if (threadLocal.get() != null) {
                throw RuntimeException("Only one Looper may be created per thread")
            }
            threadLocal.set(MyLooper())
        }
    }

    var msgQueue: MyMessageQueue = MyMessageQueue()

    var mThread: MyThread = MyThread.currentThread()

    var isInLoop = false



    fun loop() {
        Log.d(TAG, "loop: ")
        val me = myLooper()
        me ?: throw RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.")
        if (isInLoop) {
            Log.d(TAG, "Loop again would have the queued messages be executed"
                    + " before this one completed."
            )
        }
        me.isInLoop = true
        // Make sure the identity of this thread is that of the local process,
        // and keep track of what that identity token actually is.
        Binder.clearCallingIdentity()
        val ident = Binder.clearCallingIdentity()

        // Allow overriding a threshold with a system prop. e.g.
        // adb shell 'setprop log.looper.1000.main.slow 1 && stop && start'
        val thresholdOverride: Int = 0

        while (true) {
            if (!loopOnce(me, ident, thresholdOverride)) {
                Log.d(TAG, "loop: return")
                return
            }
        }
    }

    fun loopOnce(me: MyLooper, ident: Long, thresholdOverride: Int): Boolean {
        Log.d(TAG, "loopOnce: ")
        val msg = me.msgQueue.next() ?: return false
        msg.target?.dispatchMessage(msg)
        msg.recycle()
        return true
    }



    fun quit() {
        msgQueue.quit(false)
    }


}






















