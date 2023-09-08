package com.example.pinblocklib

import Format3Codec
import org.junit.Test
import org.junit.Assert.*

class Format3CodecTest {

    @Test
    fun encodeDecode_isCorrect() {
        val pin = "1234567890"
        val codec = Format3Codec()

        assertEquals(16, codec.encode(pin).length)
        assertEquals(pin, codec.decode(codec.encode(pin)))
    }

    @Test
    fun decode_isCorrect() {
        val codec = Format3Codec()
        codec.setParameters(mapOf("pan" to "43219876543210987"))
        val pin = codec.decode("3412AC0EFC34066C")
        assertEquals("1234", pin)
    }

    @Test
    fun encode_isCorrect() {
        val codec = Format3Codec()
        codec.setParameters(mapOf("pan" to "43219876543210987"))
        val block = codec.encode("1234")

        assertEquals("3412AC", block.substring(0, 6))
        assertEquals(16, block.length)
    }

    @Test
    fun setParameter_isCorrect() {
        val testPan = "2222222233334444"
        val codec = Format3Codec()
        codec.setParameters(mapOf("pan" to testPan))
        assertArrayEquals(
            Format3Codec.preparePan(testPan),
            codec.pan
        )
    }

    @Test
    fun preparePan_isCorrect() {
        val rs = Format3Codec.preparePan("1111222233334444")

        assertArrayEquals(arrayOf(
            0x0.toByte(), 0x0, 0x0, 0x0,
            0x1, 0x2, 0x2, 0x2, 0x2, 0x3, 0x3, 0x3, 0x3, 0x4, 0x4, 0x4
        ), rs)
    }

    @Test
    fun preparePin_isCorrent() {
        val pin = "1234"
        val rs = Format3Codec.preparePin(pin)
        assertArrayEquals(arrayOf(
            0x3.toByte(), 0x4,
            0x1, 0x2, 0x3, 0x4,
        ), rs.copyOfRange(0, pin.length + 2))

        for (v in rs.copyOfRange(pin.length + 2, rs.size)) {
            assertTrue(v in 0..0xf)
        }

        assertEquals(16, rs.size)
    }

    @Test
    fun preparePin2_isCorrent() {
        val pin = "123456789012"
        val rs = Format3Codec.preparePin(pin)
        assertArrayEquals(arrayOf(
            0x3.toByte(), pin.length.toByte(),
            0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0x0, 0x1.toByte(), 0x2.toByte(),
        ), rs.copyOfRange(0, pin.length + 2))

        for (v in rs.copyOfRange(pin.length + 2, rs.size)) {
            assertTrue(v in 0..0xf)
        }

        assertEquals(16, rs.size)
    }
}
