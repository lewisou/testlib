package com.example.pinblocklib

import Format3Codec
import org.junit.Test

import org.junit.Assert.*

class PinBlockCodecTest {
    @Test
    fun getCodec_isCorrect() {
        val codec = PinBlockCodec.getCodec(PinBlockCodec.Format.ISO_3)
        assertTrue(codec is Format3Codec)
    }
}
