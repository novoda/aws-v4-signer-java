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

data class HttpRequest(val method: String, val path: String, val query:String?) {

    companion object {
        fun create(method: String, pathAndQuery:String): HttpRequest {
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
    }


}
