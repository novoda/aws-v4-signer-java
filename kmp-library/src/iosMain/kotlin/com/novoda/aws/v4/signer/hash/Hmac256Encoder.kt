package com.novoda.aws.v4.signer.hash

import kotlinx.cinterop.*
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.Foundation.*

actual object Hmac256Encoder {
    @ExperimentalUnsignedTypes
    actual fun encode(key: ByteArray, value: String): ByteArray {
        memScoped {
            val dataIn = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding.toULong())!!
            val macOut = NSMutableData.dataWithLength(CC_SHA256_DIGEST_LENGTH.convert())!!
            val keyRef = key as CValuesRef<ByteVar>
            val keyLength = key.size.toULong()

            CCHmac(kCCHmacAlgSHA256, keyRef, keyLength, (dataIn.bytes as CValuesRef<ByteVar>), dataIn.length, macOut.mutableBytes as CValuesRef<UByteVar>)

            val ba = macOut.bytes!!
            val len = macOut.length.toInt()

            return ba.readBytes(len)
        }
    }
}
