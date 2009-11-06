package org.coriander

import org.apache.commons.httpclient._
import org.apache.commons.httpclient.util._

class QueryParser {
    val DELIMITER = "&"

    def parse(uri : java.net.URI) : List[NameValuePair] = {
        parse(uri.getQuery)
    }

    // TODO: Rename to parse
    def parse(query : String) : List[NameValuePair] = {
        parseNameValuePairs(query, DELIMITER)
    }

    private def parseNameValuePairs(
        value : String,
        delimiter : String
    ) : List[NameValuePair] = {
        var result : List[NameValuePair] = List[NameValuePair]()

        if (null == value)
            return result

        value.split(delimiter).foreach((pair : String) => {
            val parts = pair.split("=");
            result = new NameValuePair(parts(0) trim, parts(1) trim) :: result // TODO: This is prepending!
        });

        result.reverse; // TODO: Pretty naff
    }

    private def urlDecode(str : String) : String = {
        java.net.URLDecoder.decode(str, "UTF-8")
    }
}