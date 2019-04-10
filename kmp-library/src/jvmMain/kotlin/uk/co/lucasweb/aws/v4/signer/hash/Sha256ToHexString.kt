package uk.co.lucasweb.aws.v4.signer.hash

object Sha256ToHexString {

    @ExperimentalUnsignedTypes
    fun digest(message: String): String = Sha256.digest(message.toByteArray()).toHexString()
}
