package com.novoda.aws.v4.signer.hash

import kotlinx.serialization.toUtf8Bytes

expect fun String.toHmacSha256(key: ByteArray): ByteArray

fun String.toUtf8ByteArray() = toUtf8Bytes()
