package com.novoda.aws.v4.signer


import com.novoda.aws.v4.signer.credentials.AwsCredentials
import com.novoda.aws.v4.signer.hash.Hmac256Encoder
import com.novoda.aws.v4.signer.hash.Sha256Encoder
import com.novoda.aws.v4.signer.hash.toHexString
import com.novoda.aws.v4.signer.hash.toUtf8ByteArray

class Signer private constructor(
        private val request: CanonicalRequest,
        private val awsCredentials: AwsCredentials,
        private val date: String,
        private val scope: CredentialScope) {

    companion object {
        private const val AUTH_TAG = "AWS4"
        private const val ALGORITHM = "$AUTH_TAG-HMAC-SHA256"
        private const val X_AMZ_DATE = "X-Amz-Date"
    }

    val canonicalRequest: String
        get() = request.get()

    val stringToSign: String
        get() {
            val hashedCanonicalRequest = Sha256Encoder.encode(canonicalRequest)
            return ALGORITHM + "\n" + date + "\n" + scope.get() + "\n" + hashedCanonicalRequest
        }

    @ExperimentalUnsignedTypes
    val signature: String
        get() {
            val signature = buildSignature(awsCredentials.secretKey, scope, stringToSign)
            return "$ALGORITHM Credential=${awsCredentials.accessKey}/${scope.get()}, SignedHeaders=${request.headers.names}, Signature=$signature"
        }

    @ExperimentalUnsignedTypes
    private fun buildSignature(secretKey: String, scope: CredentialScope, stringToSign: String): String {
        val kSecret = (AUTH_TAG + secretKey).toUtf8ByteArray()
        val kDate = Hmac256Encoder.encode(kSecret, scope.dateWithoutTimestamp)
        val kRegion = Hmac256Encoder.encode(kDate, scope.region)
        val kService = Hmac256Encoder.encode(kRegion, scope.service)
        val kSigning = Hmac256Encoder.encode(kService, CredentialScope.TERMINATION_STRING)
        return Hmac256Encoder.encode(kSigning, stringToSign).toHexString()
    }

    data class Builder(
            private var awsCredentials: AwsCredentials? = null,
            private var region: String = DEFAULT_REGION,
            private val headersList: ArrayList<Header> = arrayListOf()) {

        companion object {
            private const val DEFAULT_REGION = "us-east-1"
            private const val S3 = "s3"
            private const val GLACIER = "glacier"
        }

        fun awsCredentials(awsCredentials: AwsCredentials) = apply { this.awsCredentials = awsCredentials }
        fun region(region: String) = apply { this.region = region }
        fun header(name: String, value: String) = apply { headersList.add(Header(name, value)) }
        fun header(header: Header) = apply { headersList.add(header) }
        fun headers(vararg headers: Header) = apply { headersList.addAll(listOf(*headers)) }

        fun build(request: HttpRequest, service: String, contentSha256: String): Signer {
            val canonicalHeaders = canonicalHeaders()
            val date = date(canonicalHeaders)
            val dateWithoutTimestamp = formatDateWithoutTimestamp(date)
            val awsCredentials = awsCredentials()
            val canonicalRequest = CanonicalRequest(service, request, canonicalHeaders, contentSha256)
            val scope = CredentialScope(dateWithoutTimestamp, service, region)
            return Signer(canonicalRequest, awsCredentials, date, scope)
        }

        private fun canonicalHeaders(): CanonicalHeaders {
            val builder = CanonicalHeaders.Builder()
            for ((name, value) in headersList) {
                builder.add(name, value)
            }
            return builder.build()
        }

        private fun date(canonicalHeaders: CanonicalHeaders): String {
            return canonicalHeaders.getFirstValue(X_AMZ_DATE) ?: error("headers missing '$X_AMZ_DATE' header")
        }

        private fun formatDateWithoutTimestamp(date: String): String {
            return date.substring(0, 8)
        }

        private fun awsCredentials(): AwsCredentials {
            val localAwsCredential = awsCredentials
            checkNotNull(localAwsCredential, { "Missing required aws credentials" })
            return localAwsCredential
        }

        fun buildS3(request: HttpRequest, contentSha256: String): Signer {
            return build(request, S3, contentSha256)
        }

        fun buildGlacier(request: HttpRequest, contentSha256: String): Signer {
            return build(request, GLACIER, contentSha256)
        }
    }
}
