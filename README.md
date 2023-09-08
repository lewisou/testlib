# Format3 Implementation:
https://github.com/lewisou/testlib/blob/main/pinblocklib/src/main/java/com/example/pinblocklib/Format3Codec.kt

# Unit tests:
https://github.com/lewisou/testlib/blob/main/pinblocklib/src/test/java/com/example/pinblocklib/Format3CodecTest.kt

# The library interface:
https://github.com/lewisou/testlib/blob/main/pinblocklib/src/main/java/com/example/pinblocklib/PinBlockCodec.kt

# Exmaples:

``` 
val codec = PinBlockCodec.getCodec(PinBlockCodec.Format.ISO_3)

codec.setParameters(mapOf("pan" to "43219876543210987"))

val block : Array<Byte> = codec.encodeToBytes("1234")

// ...

val pin : String = codec.decodeFromBytes(arrayOf(...))

// ...

val blockString : String = codec.encode("1234")

// ...

val pin2 : String = codec.decode("blockString")


``` 