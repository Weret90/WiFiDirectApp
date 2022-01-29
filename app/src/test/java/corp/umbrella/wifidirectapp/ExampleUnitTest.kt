package corp.umbrella.wifidirectapp

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val mutableList = arrayListOf<String>("One", "Two")
        val list = listOf<String>("One", "Two")
        assertTrue(list == mutableList)
    }
}