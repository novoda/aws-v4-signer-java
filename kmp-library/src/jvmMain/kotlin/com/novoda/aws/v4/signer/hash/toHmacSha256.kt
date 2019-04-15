package com.novoda.aws.v4.signer.hash

import java.nio.charset.StandardCharsets.UTF_8
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HMAC_SHA256 = "HmacSHA256"

actual fun String.toHmacSha256(key: ByteArray): ByteArray {
    val algorithm = HMAC_SHA256
    val mac = Mac.getInstance(algorithm)
    val signingKey = SecretKeySpec(key, algorithm)
    mac.init(signingKey)
    return mac.doFinal(toByteArray(UTF_8))
}
