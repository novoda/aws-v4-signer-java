package com.novoda.aws.v4.signer.hash

import kotlinx.serialization.toUtf8Bytes

internal fun String.toUtf8ByteArray() = toUtf8Bytes()
