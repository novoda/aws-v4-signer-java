package com.novoda.aws.v4.signer.hash

expect object Sha256Encoder {
    fun encode(value: String): String
}
