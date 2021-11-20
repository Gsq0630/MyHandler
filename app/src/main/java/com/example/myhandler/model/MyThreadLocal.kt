package com.example.myhandler.model

import java.lang.ref.WeakReference

class MyThreadLocal<T>: ThreadLocal<T>() {

    val threadLocalHashCode = 1234556

    override fun get(): T? {
        val t = MyThread.currentThread()
        val map = t.threadLocals
        if (map != null) {
            return map.getEntry(this)?.value as? T
        }
        return initial()
    }

    private fun initial(): T? {
        val t = MyThread.currentThread()
        val map = t.threadLocals
        if (map != null) {
            map.setEntry(this, null)
        } else {
            createMap(null)
        }
        return null
    }

    override fun set(value: T) {
        val t = MyThread.currentThread()
        val map = t.threadLocals
        if (map == null) {
            createMap(value)
        } else {
            map.setEntry(this, value)
        }
    }

    private fun createMap(value: T?) {
        val t = MyThread.currentThread()
        t.threadLocals = MyThreadLocalMap(this, value)
    }


    class MyThreadLocalMap(
        firstKey: MyThreadLocal<*>,
        firstValue: Any?
    ) {
        class Entry(
            k: ThreadLocal<*>,
            var value: Any?
        ): WeakReference<ThreadLocal<*>>(k)

        companion object {
            const val INITIAL_CAPACITY = 16
        }

        var table = arrayOfNulls<Entry>(INITIAL_CAPACITY)

        init {
            setEntry(firstKey, firstValue)
        }

        fun setEntry(threadLocal: MyThreadLocal<*>, obj: Any?) {
            val len = table.size
            val i = threadLocal.threadLocalHashCode.and(len -1)
            table[i] = Entry(threadLocal, obj)
        }

        fun getEntry(threadLocal: MyThreadLocal<*>): Entry? {
            val len = table.size
            val i = threadLocal.threadLocalHashCode.and(len -1)
            return table[i]
        }


    }


}