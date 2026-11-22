package me.nanova.subspace.data.api.dto

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class TorrentDetailDtosTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test
    fun `files without indexes use their response positions`() {
        val adapter = moshi.adapter(TorrentFileDto::class.java)
        val first = adapter.fromJson("""{"name":"first"}""")!!
        val second = adapter.fromJson("""{"name":"second"}""")!!
        val indexed = adapter.fromJson("""{"index":7,"name":"indexed"}""")!!

        assertEquals(0, first.toDomain(0).index)
        assertEquals(1, second.toDomain(1).index)
        assertEquals(7, indexed.toDomain(2).index)
    }

    @Test
    fun `numeric tracker statuses are mapped to readable labels`() {
        val adapter = moshi.adapter(TorrentTrackerDto::class.java)
        assertEquals("Working", adapter.fromJson("""{"status":2}""")!!.toDomain().status)
        assertEquals("Not working", adapter.fromJson("""{"status":4}""")!!.toDomain().status)
    }
}
