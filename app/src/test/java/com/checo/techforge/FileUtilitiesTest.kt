package com.checo.techforge

import com.checo.techforge.domain.readUtf8Limited
import com.checo.techforge.domain.sha256
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class FileUtilitiesTest {
    @Test
    fun sha256MatchesKnownAbcVector() {
        val result = sha256(ByteArrayInputStream("abc".toByteArray()))
        assertEquals(3L, result.bytesRead)
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", result.sha256)
    }

    @Test
    fun limitedReaderMarksOversizedInput() {
        val result = readUtf8Limited(ByteArrayInputStream("abcdefgh".toByteArray()), 4)
        assertEquals("abcd", result.text)
        assertEquals(4, result.bytesRead)
        assertTrue(result.truncated)
    }

    @Test
    fun limitedReaderDoesNotMarkSmallInput() {
        val result = readUtf8Limited(ByteArrayInputStream("abc".toByteArray()), 4)
        assertEquals("abc", result.text)
        assertFalse(result.truncated)
    }
}
