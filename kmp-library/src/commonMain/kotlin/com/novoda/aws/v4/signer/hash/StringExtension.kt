package com.novoda.aws.v4.signer.hash

import kotlinx.serialization.toUtf8Bytes

fun String.toUtf8ByteArray(): ByteArray = toUtf8Bytes()
