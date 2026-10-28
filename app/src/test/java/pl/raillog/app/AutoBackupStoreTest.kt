package pl.raillog.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.raillog.app.core.util.AutoBackupStore

class AutoBackupStoreTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private var now = 1_000L
    private fun store(dir: java.io.File = tmp.newFolder()) =
        AutoBackupStore(dir, maxFiles = 3, clock = { now++ })

    @Test
    fun saveAndReadContent() {
        val s = store()
        val f = s.save("{\"a\":1}")
        assertEquals("{\"a\":1}", f.readText())
        assertEquals(f, s.latest())
    }

    @Test
    fun rotationKeepsThreeNewest() {
        val dir = tmp.newFolder()
        val s = store(dir)
        repeat(5) { s.save("v$it") }
        val names = dir.listFiles()!!.map { it.name }.sorted()
        assertEquals(listOf("auto_1002.json", "auto_1003.json", "auto_1004.json"), names)
    }

    @Test
    fun latestReturnsNewest() {
        val s = store()
        s.save("old")
        val newest = s.save("new")
        assertEquals(newest, s.latest())
        assertEquals("new", s.latest()!!.readText())
        assertEquals(1001L, s.timestampOf(newest))
    }

    @Test
    fun deleteRemovesFile() {
        val s = store()
        val f = s.save("x")
        s.delete(f)
        assertFalse(f.exists())
        assertNull(s.latest())
    }
}
