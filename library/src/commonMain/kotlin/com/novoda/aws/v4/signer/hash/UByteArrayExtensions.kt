package com.novoda.aws.v4.signer.hash

@ExperimentalUnsignedTypes
internal fun UByteArray.toHexString() = joinToString("") { it.toString(16).padStart(2, '0') }
