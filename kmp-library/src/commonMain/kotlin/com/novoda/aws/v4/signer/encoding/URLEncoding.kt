package com.novoda.aws.v4.signer.encoding

import io.ktor.http.URLBuilder
import io.ktor.http.fullPath

fun encodePath(path: String): String = URLBuilder(path).build().fullPath
