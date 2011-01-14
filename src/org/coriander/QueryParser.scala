/*
Copyright 2011 Ben Biddington

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.coriander

import collection.mutable.ListBuffer
import java.net.URI

class QueryParser {
    def parse(uri : URI) : Query = parse(uri.getQuery)

    def parse(query : String) = parseNameValuePairs(query, DELIMITER)

    private def parseNameValuePairs(queryString : String, delimiter : String) : Query = {
        var buffer = new ListBuffer[NameValuePair]()

        if (null == queryString || queryString.trim == "")
            return new Query()

        queryString split(delimiter) foreach(pair => {
            val nameAndValue = parseNameValuePair(pair)
            
            if (nameAndValue != null) {
                buffer += nameAndValue
            }
        });

        new Query(buffer toList)
    }

    private def parseNameValuePair(parameter : String) : NameValuePair = {
        val parts = parameter split("=");

        val name = parts(0) trim

        if (name.length == 0)
            return null

        new NameValuePair(
            urlDecode(name),
            if (parts.length == 1) null else urlDecode(parts(1).trim)
        )
    }

    private def urlDecode(str : String) = java.net.URLDecoder.decode(str, "UTF-8")

	val DELIMITER = "&"
}