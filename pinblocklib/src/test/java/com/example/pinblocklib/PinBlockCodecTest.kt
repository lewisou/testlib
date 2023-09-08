package com.example.pinblocklib

import Format3Codec
import org.junit.Test

import org.junit.Assert.*

class PinBlockCodecTest {
    @Test
    fun addition_isCorrect() {
        val codec = PinBlockCodec.getCodec(PinBlockCodec.Format.ISO_3)
        assertTrue(codec is Format3Codec)
    }

    @Test
    fun isHexDigit_isCorrent() {
        assertTrue(PinBlockCodec.isHexDigit('1'))
        assertTrue(PinBlockCodec.isHexDigit('9'))
        assertTrue(PinBlockCodec.isHexDigit('a'))
        assertTrue(PinBlockCodec.isHexDigit('F'))

        assertFalse(PinBlockCodec.isHexDigit('G'))
        assertFalse(PinBlockCodec.isHexDigit('g'))
    }
}
