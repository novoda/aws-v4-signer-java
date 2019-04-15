package com.novoda.aws.v4.signer.hash

expect fun String.toHmacSha256(key: ByteArray): ByteArray
