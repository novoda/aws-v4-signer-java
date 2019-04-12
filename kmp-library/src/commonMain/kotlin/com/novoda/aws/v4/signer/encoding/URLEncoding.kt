package com.novoda.aws.v4.signer.encoding

import io.ktor.http.encodeURLPath

fun encodePath(path: String): String = path.encodeURLPath()
