package com.novoda.aws.v4.signer.hash

@ExperimentalUnsignedTypes
internal fun ByteArray.toHexString() = asUByteArray().toHexString()
