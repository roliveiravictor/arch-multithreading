package stonetree.com.multithreading

import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.util.concurrent.locks.ReentrantLock

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ThreadManagerTest {

    @Test
    fun enqueue_shouldReturnOne() {
        var value : Int? = null
        val threadManager = ThreadManager()

        threadManager.enqueue {
            value = 1
            Thread.sleep(100)
        }

        threadManager.enqueue {
            value = 0
        }

        assertEquals("Locked value was changed to zero.", 1 , value)
    }

    @Test
    fun run_Blocking_shouldReturnOne() {
        var value: Int? = null
        val threadManager = ThreadManager()

        threadManager.startBlocking {
            value = 1
        }


        threadManager.startAndNotify {
                value = 0
        }
        Thread.sleep(100)

        assertTrue(
            "Start blocking was not executed right after being released by notify. Its value should be one.",
            1 == value
        )
    }

    @Test
    fun blocking_shouldReturnZero() {
        var value = 0
        val threadManager = ThreadManager()

        threadManager.startBlocking {
            value = 1
        }

        Thread.sleep(100)

        assertEquals(0, value)
    }

    @Test
    fun start_shouldReturnZero() {
        var value = 1
        val threadManager = ThreadManager()

        threadManager.startBlocking {
            value = 0
        }

        Thread.sleep(100)

        assertTrue("Locked value was changed to zero.", 1 == value)
    }

    @Test
    fun blocking_reflected_shouldReturnZero() {
        var value = 0
        val threadManager = ThreadManager()

        val mLock = ReentrantLock()

        val lock = threadManager.javaClass.getDeclaredField("lock")
        lock.isAccessible = true
        lock.set(threadManager, mLock)

        val condition = threadManager.javaClass.getDeclaredField("condition")
        condition.isAccessible = true
        condition.set(threadManager, mLock.newCondition())

        threadManager.startBlocking {
            value = 1
        }

        Thread.sleep(100)

        assertEquals(0, value)
    }

    @Test
    fun blocking_multiple_shouldReturnOne() {
        var valueOne : Int? = null
        var valueTwo : Int? = null
        var valueThree : Int? = null
        var valueFour : Int? = null

        val threadManager = ThreadManager()

        threadManager.startBlocking {
            valueOne = 1
        }

        threadManager.startBlocking {
            valueTwo = 1
        }

        threadManager.startBlocking {
            valueThree = 1
        }

        threadManager.startBlocking {
            valueFour = 1
        }

        threadManager.startAndNotify {
            valueOne = 0
            valueTwo = 0
            valueThree = 0
            valueFour = 0
        }

        Thread.sleep(100)

        // Notify all mechanism should be implemented to see value two, three and four changing to 1

        assertTrue("startBlocking wasn't executed right after notify all", valueOne == 1)
        assertTrue("startBlocking wasn't executed right after notify all", valueTwo == 0)
        assertTrue("startBlocking wasn't executed right after notify all", valueThree == 0)
        assertTrue("startBlocking wasn't executed right after notify all", valueFour == 0)
    }
}