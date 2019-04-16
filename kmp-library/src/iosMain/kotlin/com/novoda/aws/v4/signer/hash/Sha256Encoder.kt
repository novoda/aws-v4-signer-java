package com.novoda.aws.v4.signer.hash

import kotlinx.cinterop.*
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.*

class Sha256Encoder {

    @ExperimentalUnsignedTypes
    fun encode(text: String): String {
        memScoped {
            val dataIn = (text as NSString).dataUsingEncoding(NSUTF8StringEncoding.toULong())!!
            val macOut = NSMutableData.dataWithLength(CC_SHA256_DIGEST_LENGTH.convert())!!

            CC_SHA256(dataIn.bytes as CValuesRef<ByteVar>, dataIn.length.toUInt(), macOut.mutableBytes as CValuesRef<UByteVar>)

            val ba = macOut.bytes!!
            val len = macOut.length.toInt()

            val bytes = ba.readBytes(len)
            return bytes.toHexString()
        }
    }
}
