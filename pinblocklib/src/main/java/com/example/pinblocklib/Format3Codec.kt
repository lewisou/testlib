import com.example.pinblocklib.CodecException
import com.example.pinblocklib.PinBlockCodec
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.random.Random

class Format3Codec() : PinBlockCodec() {
    var pan = preparePan("1111222233334444")

    // Xor the pan and the pin
    override fun encode(pin: String): String {
        val pinBytes = preparePin(pin)

        val pinBlockBytes = pan.zip(pinBytes) { pa, pi ->
            pa xor pi
        }

        if(pinBlockBytes.size != 16) {
            throw CodecException("The block should be 16 digits long")
        }

        val rsBytes = mutableListOf<Byte>()
        var element: Byte = 0
        for(i in pinBlockBytes.indices) {
            if (i % 2 == 0) {
                element = setHiNibbleValue(pinBlockBytes[i])
            } else {
                element = element or setLowNibbleValue(pinBlockBytes[i])
                rsBytes.add(element)
            }
        }
        return rsBytes.joinToString("") { "%02x".format(it) }.uppercase()
    }

    // Perform the Xor and get back the pin.
    override fun decode(block: String): String {
        if(block.length != 16) {
            throw CodecException("The block length must be 16.")
        }

        if(!block.all { char -> isHexDigit(char) }) {
            throw CodecException("The block can only contains digits.")
        }

        val blockBytes = block.map { c -> c.digitToInt(16).toByte() }

        val pinBytes = pan.zip(blockBytes) { pa, bl ->
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

    // A method used to update the pan.
    // Some other codecs don't have any parameters so we use a separate method to set the pan for IOS-3.
    override fun setParameters(params: Map<String, Any>) {
        if("pan" in params) {
            pan = preparePan(params["pan"] as String)
        }
    }

    companion object {
        // Take the right most 12 digits excluding the check digit and pad left with 4 zeros.
        fun preparePan(panStr: String): Array<Byte> {
            val digitTake = 12
            if(panStr.length < digitTake + 1) {
                throw CodecException("pan digits must be longer than 13.")
            }

            if(!panStr.all { char -> isHexDigit(char) }) {
                throw CodecException("The pan can only contains digits.")
            }

            val pan12 = panStr.substring(panStr.length - digitTake - 1, panStr.length - 1)
            return arrayOf(0x0.toByte(), 0x0, 0x0, 0x0) + pan12.map { c -> c.digitToInt(16).toByte() }
        }

        // Prepare a PIN – The first nibble is 0x3 followed by the length of the PIN
        // and the PIN padded with random values.
        // The random value from X’0′ to X’F’
        fun preparePin(pinStr: String): Array<Byte> {
            if(pinStr.length < 4 || pinStr.length > 12) {
                throw CodecException("The pin length must be in range (4 - 12).")
            }

            if(!pinStr.all { char -> isHexDigit(char) }) {
                throw CodecException("The pin can only contains digits.")
            }

            val lastPadding = (1..16 - pinStr.length - 2).map { _ -> Random.nextInt(0, 0x0F + 1).toByte() }

            return arrayOf(0x3.toByte(), pinStr.length.toByte()) +
                    pinStr.map { c -> c.digitToInt(16).toByte() } +
                    lastPadding
        }

        private fun setHiNibbleValue(value: Byte): Byte = (0xF0 and (value.toInt() shl 4)).toByte()

        private fun setLowNibbleValue(value: Byte): Byte = (0x0F and value.toInt()).toByte()
    }
}