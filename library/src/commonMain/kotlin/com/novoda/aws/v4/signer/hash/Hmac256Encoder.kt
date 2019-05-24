package com.novoda.aws.v4.signer.hash

expect object Hmac256Encoder {
    fun encode(key: ByteArray, value: String) : ByteArray
}
