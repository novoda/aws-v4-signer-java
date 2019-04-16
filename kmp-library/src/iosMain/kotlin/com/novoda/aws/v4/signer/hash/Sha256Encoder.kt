package com.novoda.aws.v4.signer.hash

import kotlinx.cinterop.*
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.Foundation.dataWithLength

actual object Sha256Encoder {

    @ExperimentalUnsignedTypes
    actual fun encode(value: String): String {
        memScoped {
            val dataIn = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding.toULong())!!
            val macOut = platform.Foundation.NSMutableData.dataWithLength(CC_SHA256_DIGEST_LENGTH.convert())!!

            CC_SHA256(dataIn.bytes as CValuesRef<ByteVar>, dataIn.length.toUInt(), macOut.mutableBytes as CValuesRef<UByteVar>)

            val ba = macOut.bytes!!
            val len = macOut.length.toInt()

            val bytes = ba.readBytes(len)
            return bytes.toHexString()
        }
    }
}

@ExperimentalUnsignedTypes
private fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
