package com.novoda.aws.v4.signer.hash

internal expect object Hmac256Encoder {
    fun encode(key: ByteArray, value: String) : ByteArray
}
