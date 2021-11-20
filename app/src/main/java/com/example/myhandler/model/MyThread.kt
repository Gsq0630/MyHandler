package com.example.myhandler.model

class MyThread: Thread() {

    companion object {

        val current = MyThread()

        fun currentThread(): MyThread {
            return current
        }
    }

    var threadLocals: MyThreadLocal.MyThreadLocalMap? = null



}