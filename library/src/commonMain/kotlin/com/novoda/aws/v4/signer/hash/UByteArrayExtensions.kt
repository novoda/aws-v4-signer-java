package com.novoda.aws.v4.signer.hash

@ExperimentalUnsignedTypes
fun UByteArray.toHexString() = joinToString("") { it.toString(16).padStart(2, '0') }
