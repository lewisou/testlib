package com.example.pinblocklib

import Format3Codec

abstract class PinBlockCodec {

    enum class Format {
        ISO_3
    }

    // In case you want the byte array to transmit
    abstract fun encodeToBytes(pin: String): Array<Byte>

    // In case you want to decode the received Byte array.
    abstract fun decodeFromBytes(block: Array<Byte>): String

    // In  case you want the block in the form of a string
    abstract fun encode(pin: String): String

    // In  case you want to decode the block of a string
    abstract fun decode(block: String): String

    // A method used to update the pan.
    // Some other codecs don't have any parameters so we use a separate method to set the pan for IOS-3.
    abstract fun setParameters(params: Map<String, Any>)

    companion object {
        fun getCodec(format: Format): PinBlockCodec {
            return when(format) {
                Format.ISO_3 -> Format3Codec()
            }
        }
    }
}

class CodecException(message: String) : Exception(message)