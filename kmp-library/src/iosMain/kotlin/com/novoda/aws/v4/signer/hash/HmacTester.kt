package com.novoda.aws.v4.signer.hash

class HmacTester {

    @ExperimentalUnsignedTypes
    fun signature(): String {
        val stringToSign = "AWS4-HMAC-SHA256\n" +
                "20150830T123600Z\n" +
                "20150830/us-east-1/iam/aws4_request\n" +
                "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59"

        val signingKey = "c4afb1cc5771d871763a393e44b703571b55cc28424d1a5e86da6ed3c154a4b9"
        val signature = Hmac256Encoder.encode(signingKey.toUtf8ByteArray(), stringToSign)
        return signature.toHexString()
    }
}

@ExperimentalUnsignedTypes
private fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
