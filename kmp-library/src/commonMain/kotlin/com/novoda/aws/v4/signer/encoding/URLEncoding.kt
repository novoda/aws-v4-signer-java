package com.novoda.aws.v4.signer.encoding

import io.ktor.http.encodeURLPath
import io.ktor.http.encodeURLQueryComponent

fun encodePath(path: String): String = path.encodeURLPath()

fun encodeQueryComponent(value: String): String = value.encodeURLQueryComponent()
