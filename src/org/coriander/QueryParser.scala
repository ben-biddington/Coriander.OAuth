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