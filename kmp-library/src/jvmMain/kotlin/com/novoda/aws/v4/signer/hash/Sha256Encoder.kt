package com.novoda.aws.v4.signer.hash

import java.nio.charset.StandardCharsets
import java.security.MessageDigest


private const val ALGORITHM = "SHA-256"

actual object Sha256Encoder {

    actual fun encode(value: String): String {
        val digest = MessageDigest.getInstance(ALGORITHM)
        val encodedHash = digest.digest(value.toByteArray(StandardCharsets.UTF_8))
        return bytesToHex(encodedHash)
    }

    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuffer()
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}
