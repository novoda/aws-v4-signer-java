package com.novoda.aws.v4.signer.encoding

import io.ktor.http.encodeURLParameter
import io.ktor.http.encodeURLPath

object URLEncoding {
    fun encodePath(path: String): String = path.encodeURLPath()

    fun encodeQueryComponent(value: String): String = value.encodeURLParameter()
}
