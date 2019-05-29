package com.novoda.aws.v4.signer

internal data class CredentialScope(val dateWithoutTimestamp: String, val service: String, val region: String) {

    fun get(): String {
        return "$dateWithoutTimestamp/$region/$service/$TERMINATION_STRING"
    }

    companion object {
        const val TERMINATION_STRING = "aws4_request"
    }
}
