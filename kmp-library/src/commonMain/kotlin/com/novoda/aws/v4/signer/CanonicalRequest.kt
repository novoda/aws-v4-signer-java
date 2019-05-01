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

import com.novoda.aws.v4.signer.encoding.URLEncoding

private const val S3_SERVICE = "s3"
private const val QUERY_PARAMETER_SEPARATOR = '&'
private const val QUERY_PARAMETER_VALUE_SEPARATOR = '='

/**
 * @author Richard Lucas
 */
class CanonicalRequest(
        private val service: String,
        private val httpRequest: HttpRequest,
        val headers: CanonicalHeaders,
        private val contentSha256: String
) {

    fun get(): String {
        return httpRequest.method +
                "\n" + normalizePath(httpRequest.path) +
                "\n" + normalizeQuery(httpRequest.query) +
                "\n" + headers.canonicalizedHeaders +
                "\n" + headers.names +
                "\n" + contentSha256
    }

    override fun toString(): String {
        return get()
    }

    private fun normalizePath(path: String?): String {
        if (path.isNullOrEmpty()) {
            return "/"
        }
        // Encode characters as mandated by AWS
        val encoded = URLEncoding.encodePath(path)
        if (S3_SERVICE == service) {
            /*
             * S3 requests should not be normalized.
             * See http://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-header-based-auth.html#canonical-request
             */
            return encoded
        }
        return PathUtil.normalize(encoded)
    }
}

private class Parameter(val name: String, val value: String?)

private fun normalizeQuery(rawQuery: String?): String {
    if (rawQuery.isNullOrEmpty()) {
        return ""
    }

    /*
     * Sort query parameters. Simply sort lexicographically by character
     * code, which is equivalent to comparing code points (as mandated by
     * AWS)
     */
    return rawQuery
            .extractQueryParameters()
            .sortedBy(Parameter::name)
            .fold(StringBuilder()) { builder, parameter ->
                val name = parameter.name
                val value = parameter.value ?: "" // if no value use an empty string as per the spec
                builder.append(URLEncoding.encodeQueryComponent(name))
                        .append(QUERY_PARAMETER_VALUE_SEPARATOR)
                        .append(URLEncoding.encodeQueryComponent(value))
                        .append(QUERY_PARAMETER_SEPARATOR)
            }
            .removeSuffix(QUERY_PARAMETER_SEPARATOR.toString())
            .toString()
}

/**
 * Extract parameters from a query string, preserving encoding.
 *
 *
 * We can't use Apache HTTP Client's URLEncodedUtils.parse, mainly because
 * we don't want to decode names/values.
 *
 * @param this@extractQueryParameters
 * the query to parse
 * @return The list of parameters, in the order they were found.
 */
private fun String.extractQueryParameters(): List<Parameter> {
    val results = ArrayList<Parameter>()
    val endIndex = length - 1
    var index = 0
    while (index in 0..endIndex) {
        /*
     * Ideally we should first look for '&', then look for '=' before
     * the '&', but obviously that's not how AWS understand query
     * parsing; see the test "post-vanilla-query-nonunreserved" in the
     * test suite. A string such as "?foo&bar=qux" will be understood as
     * one parameter with name "foo&bar" and value "qux". Don't ask me
     * why.
     */
        val nameValueSeparatorIndex = indexOf(QUERY_PARAMETER_VALUE_SEPARATOR, index)
        index = if (isNotFound(nameValueSeparatorIndex)) {
            val name = substring(index)
            results.add(Parameter(name, null))

            break
        } else {
            val name = substring(index, nameValueSeparatorIndex)
            val value = substring(nameValueSeparatorIndex + 1, indexOfParameterSeparatorWithin(nameValueSeparatorIndex, endIndex))
            results.add(Parameter(name, value))

            index + name.length + 1 + value.length + 1
        }
    }
    return results
}

private fun String.indexOfParameterSeparatorWithin(startIndex: Int, endIndex: Int): Int {
    val parameterSeparatorIndex = indexOf(QUERY_PARAMETER_SEPARATOR, startIndex)
    return when {
        isNotFound(parameterSeparatorIndex) -> endIndex + 1
        else -> parameterSeparatorIndex
    }
}

private fun isNotFound(index: Int) = index < 0
