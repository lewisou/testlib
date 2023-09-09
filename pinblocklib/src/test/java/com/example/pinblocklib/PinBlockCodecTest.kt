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

    @Test
    fun defaultPanEncodingPin4_isCorrect() {
        // The default pan is 1111222233334444
        val codec = PinBlockCodec.getCodec(PinBlockCodec.Format.ISO_3)

        // The prepared pan is  0000122223333444
        // The prepared pin is  341234xxxxxxxxxx
        // xor result should be 341226xxxxxxxxxx
        val blockPin4 = codec.encode("1234")

        assertEquals("341226", blockPin4.substring(0, 6))
        assertEquals(16, blockPin4.length)
    }

    @Test
    fun defaultPanEncodingPin12_isCorrect() {
        // The default pan is 1111222233334444
        val codec = PinBlockCodec.getCodec(PinBlockCodec.Format.ISO_3)

        // The prepared pan is  0000122223333444
        // The prepared pin is  3C123456789012xx
        // The xor should be    3C1226745BA326xx
        val blockPin12 = codec.encode("123456789012")

        assertEquals("3C1226745BA326", blockPin12.substring(0, 14))
        assertEquals(16, blockPin12.length)
    }

    @Test
    fun defaultPanDecodingPin4_isCorrect() {
        // The default pan is 1111222233334444
        val codec = PinBlockCodec.getCodec(PinBlockCodec.Format.ISO_3)

        // The block is         3412263463ACB345
        // the prepared pan is  0000122223333444
        // The xor should be    34123416409F8701
        // The pin should be 1234
        val pin4 = codec.decode("3412263463ACB345")
        assertEquals("1234", pin4)
    }

    @Test
    fun defaultPanDecodingPin12_isCorrect() {
        // The default pan is 1111222233334444
        val codec = PinBlockCodec.getCodec(PinBlockCodec.Format.ISO_3)

        // The block is         3C1226745BA326AB
        // The prepared pan is  0000122223333444
        // The xor should be    3C123456789012EF
        // The pin should be 123456789012

        val pin12 = codec.decode("3C1226745BA326AB")
        assertEquals("123456789012", pin12)
    }
}
