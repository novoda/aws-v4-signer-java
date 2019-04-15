package com.novoda.aws.v4.signer.hash

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HMAC_SHA256 = "HmacSHA256"

actual fun String.toHmacSha256(key: ByteArray): ByteArray {
    val algorithm = HMAC_SHA256
    val mac = Mac.getInstance(algorithm)
    val signingKey = SecretKeySpec(key, algorithm)
    mac.init(signingKey)
    return mac.doFinal(toByteArray())
}
