package com.example.myhandler.model

import android.os.SystemClock
import android.util.Log
import java.lang.IllegalStateException


class MyMessageQueue {
    companion object {
        private const val TAG = "MyMessageQueue"
    }

    var messages: MyMessage? = null

    private var mQuit = false

    private var mBlocked = false
//    private external fun nativeInit(): Long

    var mPtr = 1
//    private external fun nativePollOnce(ptr: Long, timeoutMillis: Int) /*non-static for callbacks*/
//    private external fun nativeWake(ptr: Long)

    fun enqueueMessage(msg: MyMessage, time: Long): Boolean {
        Log.d(TAG, "enqueueMessage: ")
        if (msg.target == null) {
            throw IllegalArgumentException("Message must have a target.")
        }
        synchronized(this) {
            if (msg.isInUse()) {
                throw IllegalStateException("$msg This message is already in use.")
            }
            if (mQuit) {
                val exo = IllegalStateException(msg.target.toString() + " sending message to a Handler on a dead thread")
                Log.d(TAG, "enqueueMessage: e = $exo")
                msg.recycle()
                return false
            }
            msg.makeInUse()
            msg.whenTime = time
            var p = messages
            // what is this
            var needWake: Boolean
            if (p == null || time == 0L || time < p.whenTime!!) {
                msg.next = p
                messages = msg
                needWake = mBlocked
            } else {
                needWake = mBlocked && p.target == null && msg.isAsynchronous()
                var prev: MyMessage?
                while (true) {
                    prev = p
                    p = p?.next
                    if (p == null || msg.whenTime!! < p.whenTime!!) {
                        break
                    }
                    if (needWake && p.isAsynchronous()) {
                        needWake = false
                    }
                    msg.next = p
                    prev?.next = msg
                }
            }

            if (needWake) {
                //
//                nativeWake(ptr = mPtr)
            }
        }
        return true
    }

    fun next(): MyMessage? {
        Log.d(TAG, "next: ")
        var nextPollTimeoutMillis = 0
        while (true) {
//            Log.d(TAG, "next: true mptr = $mPtr")
//            nativePollOnce(mPtr, nextPollTimeoutMillis)
            synchronized(this) {
                val now = SystemClock.uptimeMillis()
                var preMsg : MyMessage? = null
                var msg = messages
                // 异步消息 屏障 why 查找下一个异步消息
                if (msg != null && msg.target == null) {
                    do {
                        preMsg = msg
                        msg = msg?.next
                    } while (msg != null && msg.isAsynchronous())
                }
                if (msg != null) {
                    if (now < msg.whenTime!!) {
                        // Next message is not ready.  Set a timeout to wake up when it is ready.
                        // 下一条消息没准备好
                        nextPollTimeoutMillis = (msg.whenTime!! - now).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                    } else {
                        if (preMsg != null) {
                            preMsg.next = msg.next
                        } else {
                            messages = msg.next
                        }
                        msg.next = null
                        msg.makeInUse()
                        return msg
                    }
                } else {
                    // 没有消息了
                    nextPollTimeoutMillis = -1
                }
                mBlocked  = true

                // 所有消息处理完毕 开始处理退出消息
                if (mQuit) {
                    dispose()
                    return null
                }
            }
        }

    }

    fun quit(safe: Boolean) {
        synchronized(this) {
            if (mQuit) return
            mQuit = true
            if (safe) {
                removeAllFutureMessagesLocked()
            } else {
                removeAllMessagesLocked()
            }
        }
    }

    fun dispose() {

    }

    fun removeAllMessagesLocked() {
        //删除所有消息
    }

    fun removeAllFutureMessagesLocked() {
        val now = SystemClock.uptimeMillis()
        // 删除所有未来消息
        // 删除所有msg.whenTime > now的msg
    }

}