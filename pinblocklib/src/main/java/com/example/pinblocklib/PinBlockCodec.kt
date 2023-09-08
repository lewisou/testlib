package com.example.pinblocklib

import Format3Codec

abstract class PinBlockCodec {

    enum class Format {
        ISO_3
    }

    abstract fun encode(pin: String): String
    abstract fun decode(block: String): String

    // A method used to update the pan.
    // Some other codecs don't have any parameters so we use a separate method to set the pan for IOS-3.
    abstract fun setParameters(params: Map<String, Any>)

    companion object {
        private val hexReg = Regex("[0-9a-fA-F]")
        fun getCodec(format: Format): PinBlockCodec {
            return when(format) {
                Format.ISO_3 -> Format3Codec()
            }
        }

        fun isHexDigit(c: Char): Boolean {
            return hexReg.matches(c.toString())
        }
    }
}



class CodecException(message: String) : Exception(message)