package com.novoda.aws.v4.signer.hash

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

actual object Sha256Encoder {

    @ExperimentalUnsignedTypes
    actual fun encode(value: String): String {
        val input = value.toUtf8()
        val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
        input.usePinned { inputPinned ->
            digest.usePinned { digestPinned ->
                CC_SHA256(inputPinned.addressOf(0), input.size.convert(), digestPinned.addressOf(0))
            }
        }
        return digest.toHexString()
    }
}
