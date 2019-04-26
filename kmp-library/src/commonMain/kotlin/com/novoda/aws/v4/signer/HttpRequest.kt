package com.novoda.aws.v4.signer

import io.ktor.http.Url
import io.ktor.http.formUrlEncode

data class HttpRequest(val method: String, val path: String, val query: String?) {

    companion object {
        fun create(method: String, pathAndQuery: String): HttpRequest {
            val queryStart = pathAndQuery.indexOf('?')
            val path: String
            val query: String?
            if (queryStart >= 0) {
                path = pathAndQuery.substring(0, queryStart)
                query = pathAndQuery.substring(queryStart + 1)
            } else {
                path = pathAndQuery
                query = null
            }
            return HttpRequest(method, path, query)
        }

        fun create(method: String, url: Url): HttpRequest {
            return HttpRequest(method, url.encodedPath, url.parameters.formUrlEncode())
        }
    }

}
