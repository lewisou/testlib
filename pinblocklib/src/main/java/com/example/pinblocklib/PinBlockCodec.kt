package com.example.pinblocklib

import Format3Codec

/**
 * The PinBlock library public Interface.
 * It implements a family of PinBlock algorithms.
 */
abstract class PinBlockCodec {

    enum class Format {
        ISO_3
        // ... more codecs here
    }

    companion object {
        /**
         * The factory function encapsulates implementation details.
         * Example:
         *  val codec = PinBlockCodec.getCodec(PinBlockCodec.Format.ISO_3)
         *  codec.encodeToBytes("1234")
         */
        fun getCodec(format: Format): PinBlockCodec {
            return when(format) {
                Format.ISO_3 -> Format3Codec()
            }
        }
    }

    /**
     * Encode the pin to a byte array for transmission
     */
    abstract fun encodeToBytes(pin: String): Array<Byte>

    /**
     * Decode the pin from the received block byte array.
     */
    abstract fun decodeFromBytes(block: Array<Byte>): String

    /**
     * Encode the pin and return the block in string format. Each letter is a 4-bit long value (\x0 - \xF).
     */
    abstract fun encode(pin: String): String

    /**
     * Decode the block in string format. Each letter is a 4-bit long value (\x0 - \xF).
     */
    abstract fun decode(block: String): String

    /**
     * The method to update the pan.
     * Some other codecs don't have any parameters so a separate method is used to set the pan for IOS-3.
     * Example: codec.setParameters(mapOf("pan" to "43219876543210987"))
     */
    abstract fun setParameters(params: Map<String, Any>)
}

class CodecException(message: String) : Exception(message)