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
        val pinBytes = preparePin(pin)

        val nibbles = pan.zip(pinBytes) { pa, pi ->
            pa xor pi
        }

        if(nibbles.size != 16) {
            throw CodecException("The block should be 16 digits long")
        }
        return nibblesToBytes(nibbles.toTypedArray())
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
            throw CodecException("The block length must be 16.")
        }

        if(!isHexDigits(block)) {
            throw CodecException("The block can only contains digits.")
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
        if("pan" in params) {
            pan = preparePan(params["pan"] as String)
        }
    }

    /**
     * Internal decode function that performs XOR and returns the original pin.
     */
    fun decodeFromNibbles(blockInNibbles: Array<Byte>): String {
        val pinBytes = pan.zip(blockInNibbles) { pa, bl ->
            pa xor bl
        }

        if(pinBytes[0] != 3.toByte()) {
            throw CodecException("Invalid block format.")
        }

        val pinLen = pinBytes[1]
        if(pinLen < 4 || pinLen > 12) {
            throw CodecException("Invalid block with the wrong pin length.")
        }

        val orgPinBytes = pinBytes.subList(2, 2 + pinLen)
        return orgPinBytes.joinToString("") { b -> b.toString() }
    }

    companion object {
        /**
         * The format 3 pan preparation
         * Take the right most 12 digits excluding the check digit and pad left with 4 zeros.
         */
        fun preparePan(panStr: String): Array<Byte> {
            val digitTake = 12
            if(panStr.length < digitTake + 1) {
                throw CodecException("pan digits must be longer than 13.")
            }

            if(!isHexDigits(panStr)) {
                throw CodecException("The pan can only contains digits.")
            }

            // The last digit is the check digit.
            val pan12 = panStr.substring(panStr.length - digitTake - 1, panStr.length - 1)
            return arrayOf(0x0.toByte(), 0x0, 0x0, 0x0) + pan12.map { c -> c.digitToInt(16).toByte() }
        }

        /**
         * The format 3 pin preparation
         * The first nibble is 0x3 followed by the length of the PIN
         * and the PIN padded with random values (0 - 15).
         * The random value from X’0′ to X’F’
         */
        fun preparePin(pinStr: String): Array<Byte> {
            if(pinStr.length < 4 || pinStr.length > 12) {
                throw CodecException("The pin length must be in range (4 - 12).")
            }

            if(!isHexDigits(pinStr)) {
                throw CodecException("The pin can only contains digits.")
            }

            val lastPadding = (1..16 - pinStr.length - 2).map { _ -> Random.nextInt(0, 0x0F + 1).toByte() }

            return arrayOf(0x3.toByte(), pinStr.length.toByte()) +
                    pinStr.map { c -> c.digitToInt(16).toByte() } +
                    lastPadding
        }

        private fun setHiNibbleValue(value: Byte): Byte = (0xF0 and (value.toInt() shl 4)).toByte()

        private fun setLowNibbleValue(value: Byte): Byte = (0x0F and value.toInt()).toByte()

        /**
         * Here a nibble is 4 bits long
         * A byte contains 2 nibbles and it is 8 bits long
         * The conversion from bytes to nibbles
         */
        fun bytesToNibbles(block: Array<Byte>): Array<Byte> {
            val nibbles = mutableListOf<Byte>()
            for(i in block.indices) {
                val v1: Byte = (block[i].toInt() shr 4).toByte() and 0x0f
                val v2: Byte = block[i] and 0x0f
                nibbles.add(v1)
                nibbles.add(v2)
            }
            return nibbles.toTypedArray()
        }

        /**
         * Here a nibble is 4 bits long
         * A byte contains 2 nibbles and it is 8 bits long
         * The conversion from nibbles to bytes
         */
        fun nibblesToBytes(nibbles: Array<Byte>): Array<Byte> {
            if(nibbles.size % 2 != 0) {
                throw CodecException("Internal error, the nibbles length is not a even number.")
            }
            val rs = mutableListOf<Byte>()
            var element: Byte = 0
            for(i in nibbles.indices) {
                if (i % 2 == 0) {
                    element = setHiNibbleValue(nibbles[i])
                } else {
                    element = element or setLowNibbleValue(nibbles[i])
                    rs.add(element)
                }
            }
            return rs.toTypedArray()
        }

        private val hexReg = Regex("[0-9a-fA-F]+")

        fun isHexDigits(str: String): Boolean {
            return hexReg.matches(str)
        }
    }
}