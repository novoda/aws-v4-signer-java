package com.novoda.aws.v4.signer.hash

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HMAC_SHA256 = "HmacSHA256"

actual object Hmac256Encoder {

    actual fun encode(key: ByteArray, value: String): ByteArray {
        val algorithm = HMAC_SHA256
        val mac = Mac.getInstance(algorithm)
        val signingKey = SecretKeySpec(key, algorithm)
        mac.init(signingKey)
        return mac.doFinal(value.toByteArray())
    }
}
