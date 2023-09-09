import com.example.pinblocklib.CodecException
import com.example.pinblocklib.PinBlockCodec
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.random.Random

/**
 * ISO-3 Codec
 */
class Format3Codec() : PinBlockCodec() {
    var pan = preparePan("1111222233334444")

    /**
     * Encode the pin to a byte array for transmission
     */
    override fun encodeToBytes(pin: String): Array<Byte> {
        val pinNibbles = preparePin(pin)

        val blockNibbles = pan.zip(pinNibbles) { pa, pi ->
            pa xor pi
        }

        if(blockNibbles.size != 16) {
            throw CodecException("The block length is wrong.")
        }
        return nibblesToBytes(blockNibbles.toTypedArray())
    }

    /**
     * Decode the pin from the received block byte array.
     */
    override fun decodeFromBytes(block: Array<Byte>): String {
        if(block.size != 8) {
            throw CodecException("The block length is wrong.")
        }

        val nibbles = bytesToNibbles(block)
        return decodeFromNibbles(nibbles)
    }

    /**
     * Encode the pin and return the block in string format. Each letter is a 4-bit long value (\x0 - \xF).
     */
    override fun encode(pin: String): String {
        val bytes = encodeToBytes(pin)
        return bytes.joinToString("") { "%02x".format(it) }.uppercase()
    }

    /**
     * Decode the block in string format. Each letter is a 4-bit long value (\x0 - \xF).
     */
    override fun decode(block: String): String {
        if(block.length != 16) {
            throw CodecException("The block length is wrong.")
        }

        if(!isHexDigits(block)) {
            throw CodecException("The block can only contains hex digits.")
        }

        val nibbles = block.map { c -> c.digitToInt(16).toByte() }

        return decodeFromNibbles(nibbles.toTypedArray())
    }

    /**
     * The method to update the pan.
     * Some other codecs don't have any parameters so a separate method is used to set the pan for IOS-3.
     * Example: codec.setParameters(mapOf("pan" to "43219876543210987"))
     */
    override fun setParameters(params: Map<String, Any>) {
        val key = "pan"
        if(key in params) {
            pan = preparePan(params[key] as String)
        }
    }

    /**
     * Internal decode function that performs XOR and returns the original pin.
     */
    fun decodeFromNibbles(blockNibbles: Array<Byte>): String {
        val pinNibbles = pan.zip(blockNibbles) { pa, bl ->
            pa xor bl
        }

        if(pinNibbles[0] != 3.toByte()) {
            throw CodecException("The block format is invalid.")
        }

        val pinLen = pinNibbles[1]
        if(pinLen < 4 || pinLen > 12) {
            throw CodecException("The block is invalid and the pin length is invalid.")
        }

        return pinNibbles.subList(2, 2 + pinLen).joinToString("") { b -> b.toString() }
    }

    companion object {
        /**
         * The format 3 pan preparation
         * Take the right most 12 digits excluding the check digit and pad left with 4 zeros.
         */
        fun preparePan(panStr: String): Array<Byte> {
            val digitTake = 12

            // Plus the last digit. The total length is digitTake + 1
            if(panStr.length < digitTake + 1) {
                throw CodecException("Pan digits must be longer than ${digitTake}.")
            }

            if(!isHexDigits(panStr)) {
                throw CodecException("The pan can only contains hex digits.")
            }

            // The last digit is the check digit.
            val pan12 = panStr.substring(panStr.length - digitTake - 1, panStr.length - 1)
            return arrayOf(0x0.toByte(), 0x0, 0x0, 0x0) + pan12.map { c -> c.digitToInt(16).toByte() }
        }

        /**
         * The format 3 pin preparation:
         * The first nibble is 0x3 followed by the length of the PIN
         * and the PIN is padded with random values (0 - 15).
         */
        fun preparePin(pinStr: String): Array<Byte> {
            if(pinStr.length < 4 || pinStr.length > 12) {
                throw CodecException("The pin length must be in range (4 - 12).")
            }

            if(!isHexDigits(pinStr)) {
                throw CodecException("The pin can only contains digits.")
            }

            val padding = (1..16 - pinStr.length - 2).map { _ -> Random.nextInt(0, 0x0F + 1).toByte() }

            return arrayOf(0x3.toByte(), pinStr.length.toByte()) +
                    pinStr.map { c -> c.digitToInt(16).toByte() } +
                    padding
        }

        private fun setHiNibbleValue(value: Byte): Byte = (0xF0 and (value.toInt() shl 4)).toByte()

        private fun setLowNibbleValue(value: Byte): Byte = (0x0F and value.toInt()).toByte()

        /**
         * This method is to convert from bytes to nibbles.
         * Here a nibble is 4 bits long.
         * And a byte contains 2 nibbles and it is 8 bits long.
         */
        fun bytesToNibbles(bytes: Array<Byte>): Array<Byte> {
            val nibbles = mutableListOf<Byte>()
            for(i in bytes.indices) {
                val v1: Byte = (bytes[i].toInt() shr 4).toByte() and 0x0f
                val v2: Byte = bytes[i] and 0x0f
                nibbles.add(v1)
                nibbles.add(v2)
            }
            return nibbles.toTypedArray()
        }

        /**
         * This method is to convert from nibbles to bytes.
         * Here a nibble is 4 bits long.
         * And a byte contains 2 nibbles and it is 8 bits long.
         */
        fun nibblesToBytes(nibbles: Array<Byte>): Array<Byte> {
            if(nibbles.size % 2 != 0) {
                throw CodecException("Internal error, the nibbles length is not an even number.")
            }
            val bytes = mutableListOf<Byte>()
            var aByte: Byte = 0
            for(i in nibbles.indices) {
                if (i % 2 == 0) {
                    aByte = setHiNibbleValue(nibbles[i])
                } else {
                    aByte = aByte or setLowNibbleValue(nibbles[i])
                    bytes.add(aByte)
                }
            }
            return bytes.toTypedArray()
        }

        fun isHexDigits(str: String): Boolean {
            return hexReg.matches(str)
        }
        private val hexReg = Regex("[0-9a-fA-F]+")
    }
}