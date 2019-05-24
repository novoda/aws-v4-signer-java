package com.novoda.aws.v4.signer

import kotlin.test.Test
import kotlin.test.assertEquals

private const val EXPECTED_GLACIER = "PUT\n" +
        "/-/vaults/examplevault\n" +
        "\n" +
        "host:glacier.us-east-1.amazonaws.com\n" +
        "x-amz-date:20120525T002453Z\n" +
        "x-amz-glacier-version:2012-06-01\n" +
        "\n" +
        "host;x-amz-date;x-amz-glacier-version\n" +
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

private const val EXPECTED_S3 = "PUT\n" +
        "/my-object//example//photo.user\n" +
        "\n" +
        "host:s3.us-east-1.amazonaws.com\n" +
        "x-amz-date:20120525T002453Z\n" +
        "\n" +
        "host;x-amz-date\n" +
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

/**
 * @author Richard Lucas
 */
class CanonicalRequestTest {

    @Test
    fun shouldGetGlacierCanonicalRequest() {
        val request = HttpRequest.create("PUT", "/-/vaults///./examplevault")
        val headers = CanonicalHeaders.Builder()
                .add("Host", "glacier.us-east-1.amazonaws.com")
                .add("x-amz-date", "20120525T002453Z")
                .add("x-amz-glacier-version", "2012-06-01")
                .build()
        val hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

        val actualRequest = CanonicalRequest("glacier", request, headers, hash).get()

        assertEquals(EXPECTED_GLACIER, actualRequest)
    }

    @Test
    fun shouldGetS3CanonicalRequest() {
        val request = HttpRequest.create("PUT", "/my-object//example//photo.user")
        val headers = CanonicalHeaders.Builder()
                .add("Host", "s3.us-east-1.amazonaws.com")
                .add("x-amz-date", "20120525T002453Z")
                .build()
        val hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

        val actualRequest = CanonicalRequest("s3", request, headers, hash).get()

        assertEquals(EXPECTED_S3, actualRequest)
    }
}
