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

import io.ktor.http.URLBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpRequestTest {

    @Test
    fun shouldGetHttpMethod() {
        assertEquals(HttpRequest.create("GET", "/test").method, "GET")
    }

    @Test
    fun shouldGetQueryFromUrl() {
        assertEquals(HttpRequest.create("GET", URLBuilder("http://localhost/test?test=one&hello=world").build()).query, "test=one&hello=world")
    }

    @Test
    fun shouldGetQueryFromPathAndQuery() {
        assertEquals(HttpRequest.create("GET", "/test?test=one&hello=world").query, "test=one&hello=world")
    }
}