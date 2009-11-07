package org.coriander

import org.apache.commons.httpclient._
import org.apache.commons.httpclient.util._

class QueryParser {
    val DELIMITER = "&"

    def parse(uri : java.net.URI) : Map[String, String] = {
        parse(uri.getQuery)
    }

    def parse(query : String) : Map[String, String] = {
        parseNameValuePairs(query, DELIMITER)
    }

    private def parseNameValuePairs(
        value : String,
        delimiter : String
    ) : Map[String, String] = {

        var result : Map[String, String] = Map()

        if (null == value)
            return result

        value.split(delimiter).foreach((pair : String) => {
            val parts = pair.split("=");
            result += urlDecode(parts(0).trim) -> {
                if (parts.length ==1) null else urlDecode(parts(1).trim)
            }
        });

        result
    }

    private def urlDecode(str : String) : String = {
        java.net.URLDecoder.decode(str, "UTF-8")
    }
}