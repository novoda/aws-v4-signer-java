package com.novoda.aws.v4.signer.hash

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA256

actual object Hmac256Encoder {
    @ExperimentalUnsignedTypes
    actual fun encode(key: ByteArray, value: String): ByteArray {
        val input = value.toUtf8()
        val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
        key.usePinned { keyPinned ->
            input.usePinned { inputPinned ->
                digest.usePinned { digestPinned ->
                    CCHmac(kCCHmacAlgSHA256, keyPinned.addressOf(0), key.size.convert(), inputPinned.addressOf(0), input.size.convert(), digestPinned.addressOf(0))
                }
            }
        }
        return digest.toByteArray()
    }
}
