package com.novoda.aws.v4.signer.hash

class HmacTestUtils {

    @ExperimentalUnsignedTypes
    fun createSignature(stringToSign: String, signingKey: String): String {
        val signature = Hmac256Encoder.encode(signingKey.toUtf8ByteArray(), stringToSign)
        return signature.toHexString()
    }
}

@ExperimentalUnsignedTypes
private fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
