package com.example.pinblocklib

import Format3Codec
import org.junit.Test
import org.junit.Assert.*

class Format3CodecTest {
    @Test
    fun encodeDecodeStr_isCorrect() {
        val pin = "1234567890"
        val codec = Format3Codec()

        assertEquals(16, codec.encode(pin).length)
        assertEquals(pin, codec.decode(codec.encode(pin)))
    }

    @Test
    fun encodeDecodeBytes_isCorrect() {
        val pin = "1234567890"
        val codec = Format3Codec()

        assertEquals(8, codec.encodeToBytes(pin).size)
        assertEquals(pin, codec.decodeFromBytes(codec.encodeToBytes(pin)))
    }


    @Test
    fun decode_isCorrect() {
        val codec = Format3Codec()
        codec.setParameters(mapOf("pan" to "43219876543210987"))
        val pin = codec.decode("3412AC0EFC34066C")
        assertEquals("1234", pin)
    }

    @Test
    fun decodeFromBytes_isCorrect() {
        val codec = Format3Codec()
        codec.setParameters(mapOf("pan" to "43219876543210987"))
        val pin = codec.decodeFromBytes(arrayOf(
            0x34.toByte(), 0x12.toByte(), 0xAC.toByte(), 0x0E.toByte(), 0xFC.toByte(), 0x34.toByte(), 0x06.toByte(), 0x6C.toByte()
        ))
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
    fun encodeToBytes_isCorrect() {
        val codec = Format3Codec()
        codec.setParameters(mapOf("pan" to "43219876543210987"))
        val block = codec.encodeToBytes("1234")

        assertArrayEquals(arrayOf(0x34.toByte(), 0x12.toByte(), 0xAC.toByte()), block.copyOfRange(0, 3))
        assertEquals(8, block.size)
    }

    @Test
    fun nibblesToBytes_isCorrect() {
        val rs = Format3Codec.nibblesToBytes(arrayOf(
            0x3.toByte(), 0x4.toByte(),
            0x5.toByte(), 0x6.toByte(),
            0xf.toByte(), 0xf.toByte(),
            0xe.toByte(), 0xf.toByte(),
            0xd.toByte(), 0xf.toByte(),
        ))

        assertArrayEquals(arrayOf(0x34.toByte(), 0x56.toByte(), 0xff.toByte(), 0xef.toByte(), 0xdf.toByte()),
            rs)
    }

    @Test
    fun bytesToNibbles_isCorrect() {
        val rs = Format3Codec.bytesToNibbles(arrayOf(
            0x34.toByte(), 0x56.toByte(), 0xff.toByte(),
            0xef.toByte(), 0xdf.toByte()))

        assertArrayEquals(arrayOf(
            0x3.toByte(), 0x4.toByte(),
            0x5.toByte(), 0x6.toByte(),
            0xf.toByte(), 0xf.toByte(),
            0xe.toByte(), 0xf.toByte(),
            0xd.toByte(), 0xf.toByte(),
        ), rs)
    }

    @Test
    fun decodeFromNibbles_isCorrect() {
        val codec = Format3Codec()
        codec.setParameters(mapOf("pan" to "43219876543210987"))
        val pin = codec.decodeFromNibbles(arrayOf(
            0x3.toByte(), 0x4,
            0x1.toByte(), 0x2,
            0xA.toByte(), 0xC,
            0x0.toByte(), 0xE,
            0xF.toByte(), 0xC,
            0x3.toByte(), 0x4,
            0x0.toByte(), 0x6,
            0x6.toByte(), 0xC
        ))
        assertEquals("1234", pin)
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
            0x3.toByte(), 0x4, 0x1, 0x2, 0x3, 0x4,
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

    @Test
    fun isHexDigit_isCorrent() {
        assertTrue(Format3Codec.isHexDigit('1'))
        assertTrue(Format3Codec.isHexDigit('9'))
        assertTrue(Format3Codec.isHexDigit('a'))
        assertTrue(Format3Codec.isHexDigit('F'))

        assertFalse(Format3Codec.isHexDigit('G'))
        assertFalse(Format3Codec.isHexDigit('g'))
    }
}
