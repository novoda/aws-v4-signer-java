package com.novoda.aws.v4.signer.hash

@ExperimentalUnsignedTypes
fun ByteArray.toHexString() = asUByteArray().toHexString()
