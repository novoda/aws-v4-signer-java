package com.novoda.aws.v4.signer

import com.novoda.aws.v4.signer.encoding.URLEncoding

private const val S3_SERVICE = "s3"
private const val QUERY_PARAMETER_SEPARATOR = '&'
private const val QUERY_PARAMETER_VALUE_SEPARATOR = '='

internal class CanonicalRequest(
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
            .sortedBy(Pair<String, String?>::first)
            .fold(StringBuilder()) { builder, (name, value) ->
                builder.append(URLEncoding.encodeQueryComponent(name))
                        .append(QUERY_PARAMETER_VALUE_SEPARATOR)
                        .append(URLEncoding.encodeQueryComponent(value ?: ""))
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
private fun String.extractQueryParameters(): List<Pair<String, String?>> {
    val results = ArrayList<Pair<String, String?>>()
    var index = 0
    while (index in 0 until length) {
        /*
         * Ideally we should first look for '&', then look for '=' before
         * the '&', but obviously that's not how AWS understand query
         * parsing; see the test "post-vanilla-query-nonunreserved" in the
         * test suite. A string such as "?foo&bar=qux" will be understood as
         * one parameter with name "foo&bar" and value "qux". Don't ask me
         * why.
         */
        index = when {
            isNotFound(indexOfValueSeparator(index)) -> addLastNameWithoutValue(index, results)
            else -> addNameValuePair(index, results)
        }
    }
    return results
}

private fun String.addLastNameWithoutValue(startIndex: Int, results: ArrayList<Pair<String, String?>>): Int {
    val name = substring(startIndex)
    results.add(name to null)

    return length
}

private fun String.addNameValuePair(startIndex: Int, results: ArrayList<Pair<String, String?>>): Int {
    val nameEndIndex = indexOfValueSeparator(startIndex)
    val name = substring(startIndex, nameEndIndex)
    val valueEndIndex = indexOfParameterSeparator(nameEndIndex)
    val value = substring(nameEndIndex + 1, valueEndIndex)
    results.add(name to value)

    return valueEndIndex + 1
}

private fun String.indexOfValueSeparator(startIndex: Int) = indexOf(QUERY_PARAMETER_VALUE_SEPARATOR, startIndex)

private fun String.indexOfParameterSeparator(startIndex: Int): Int {
    val parameterSeparatorIndex = indexOf(QUERY_PARAMETER_SEPARATOR, startIndex)
    return if (isNotFound(parameterSeparatorIndex)) {
        length
    } else {
        parameterSeparatorIndex
    }
}

private fun isNotFound(index: Int) = index < 0
