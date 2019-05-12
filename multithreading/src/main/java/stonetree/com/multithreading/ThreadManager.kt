package stonetree.com.multithreading

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class ThreadManager {

    private var lock: Lock? = null

    private var condition: Condition? = null

    /**
     * Enqueue functions execution based on ThreadManager object
     */
    fun enqueue(function: () -> Unit) {
        Thread(Runnable {
            synchronized(this@ThreadManager) {
                function()
            }
        }).start()
    }

    /**
    /**
     * Execution relies on no lock condition created by start function.
     * Enqueue functions execution based on ThreadManager object
    */
     */
    fun startBlocking(function: () -> Unit) {
        Thread(Runnable {
            initLock()
            awaitLock()
            function()
            flush()
        }).start()
    }

    /**
     * Start new thread execution and create lock condition to prevent startBlocking's execution right away
     */
    fun startAndNotify(function: () -> Unit) {
        Thread(Runnable {
            function()
            releaseLock()
        }).start()
    }

    /**
     * Clear locks that would prevent startBlocking infinity await
     */
    private fun flush() {
        lock = null
        condition = null
    }

    /**
     * Create lock condition
     */
    private fun initLock() {
        if(lock == null) {
            lock = ReentrantLock()
        }

        if(condition == null) {
            condition = lock?.newCondition()
        }
    }

    /**
     * Set current thread to await a running lock condition
     */
    private fun awaitLock() {
        lock?.withLock {
            condition?.await()
        }
    }

    /**
     * Release thread waiting condition
     */
    private fun releaseLock() {
        lock?.withLock {
            condition?.signal()
        }
    }
}