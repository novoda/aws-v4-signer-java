/*
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  specific language governing permissions and limitations under the License.

  Copyright 2016 the original author or authors.
 */
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
