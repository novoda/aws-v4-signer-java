package com.novoda.aws.v4.signer


import com.novoda.aws.v4.signer.credentials.AwsCredentials
import com.novoda.aws.v4.signer.hash.Hmac256Encoder
import com.novoda.aws.v4.signer.hash.Sha256Encoder
import com.novoda.aws.v4.signer.hash.toHexString
import com.novoda.aws.v4.signer.hash.toUtf8ByteArray
import kotlin.jvm.JvmStatic

class Signer private constructor(
        private val request: CanonicalRequest,
        private val awsCredentials: AwsCredentials,
        private val date: String,
        private val scope: CredentialScope) {

    val canonicalRequest: String
        get() = request.get()

    val stringToSign: String
        get() {
            val hashedCanonicalRequest = Sha256Encoder.encode(canonicalRequest)
            return buildStringToSign(date, scope.get(), hashedCanonicalRequest)
        }

    @ExperimentalUnsignedTypes
    val signature: String
        get() {
            val signature = buildSignature(awsCredentials.secretKey, scope, stringToSign)
            return buildAuthHeader(awsCredentials.accessKey, scope.get(), request.headers.names, signature)
        }

    class Builder {

        private var awsCredentials: AwsCredentials? = null
        private var region = DEFAULT_REGION
        private val headersList = ArrayList<Header>()

        private val canonicalHeaders: CanonicalHeaders
            get() {
                val builder = CanonicalHeaders.Builder()
                for ((name, value) in headersList) {
                    builder.add(name, value)
                }
                return builder.build()
            }

        fun awsCredentials(awsCredentials: AwsCredentials): Builder {
            this.awsCredentials = awsCredentials
            return this
        }

        fun region(region: String): Builder {
            this.region = region
            return this
        }

        fun header(name: String, value: String): Builder {
            headersList.add(Header(name, value))
            return this
        }

        fun header(header: Header): Builder {
            headersList.add(header)
            return this
        }

        fun headers(vararg headers: Header): Builder {
            headersList.addAll(listOf(*headers))
            return this
        }

        fun build(request: HttpRequest, service: String, contentSha256: String): Signer {
            val canonicalHeaders = canonicalHeaders
            val date = getDate(canonicalHeaders)
            val dateWithoutTimestamp = formatDateWithoutTimestamp(date)
            val awsCredentials = getAwsCredentials()
            val canonicalRequest = CanonicalRequest(service, request, canonicalHeaders, contentSha256)
            val scope = CredentialScope(dateWithoutTimestamp, service, region)
            return Signer(canonicalRequest, awsCredentials, date, scope)
        }

        private fun getDate(canonicalHeaders: CanonicalHeaders): String {
            return canonicalHeaders.getFirstValue(X_AMZ_DATE) ?: error("headers missing '$X_AMZ_DATE' header")
        }

        fun buildS3(request: HttpRequest, contentSha256: String): Signer {
            return build(request, S3, contentSha256)
        }

        fun buildGlacier(request: HttpRequest, contentSha256: String): Signer {
            return build(request, GLACIER, contentSha256)
        }

        private fun getAwsCredentials(): AwsCredentials {
            val localAwsCredential = awsCredentials
            checkNotNull(localAwsCredential, { "Missing required aws credentials" })
            return localAwsCredential
        }

        companion object {

            private const val DEFAULT_REGION = "us-east-1"
            private const val S3 = "s3"
            private const val GLACIER = "glacier"
        }

    }

    companion object {

        private const val AUTH_TAG = "AWS4"
        private const val ALGORITHM = "$AUTH_TAG-HMAC-SHA256"
        private const val X_AMZ_DATE = "X-Amz-Date"

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }

        private fun formatDateWithoutTimestamp(date: String): String {
            return date.substring(0, 8)
        }

        private fun buildStringToSign(date: String, credentialScope: String, hashedCanonicalRequest: String): String {
            return ALGORITHM + "\n" + date + "\n" + credentialScope + "\n" + hashedCanonicalRequest
        }

        private fun buildAuthHeader(accessKey: String, credentialScope: String, signedHeaders: String, signature: String): String {
            return "$ALGORITHM Credential=$accessKey/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"
        }

        private fun hmacSha256(key: ByteArray, value: String): ByteArray {
            return Hmac256Encoder.encode(key, value)
        }

        @ExperimentalUnsignedTypes
        private fun buildSignature(secretKey: String, scope: CredentialScope, stringToSign: String): String {
            val kSecret = (AUTH_TAG + secretKey).toUtf8ByteArray()
            val kDate = hmacSha256(kSecret, scope.dateWithoutTimestamp)
            val kRegion = hmacSha256(kDate, scope.region)
            val kService = hmacSha256(kRegion, scope.service)
            val kSigning = hmacSha256(kService, CredentialScope.TERMINATION_STRING)
            return hmacSha256(kSigning, stringToSign).toHexString()
        }
    }
}
