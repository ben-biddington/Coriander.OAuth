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

    private def parseNameValuePairs(value : String, delimiter : String) : Map[String, String] = {

        var result : Map[String, String] = Map()

        if (null == value || value.trim == "")
            return result

        value split(delimiter) foreach(pair => {
            val (name, value) : Tuple2[String, String] = parseParameter(pair)
            
            if (name != null) {
                if (result.contains(name)) {
                    val updatedValue : String = result(name) + "," + value
                    
                    result -= name
                    
                    result += Tuple2(name, updatedValue)
                } else {
                    result += Tuple2(name, value)
                }
            }
        });

        result
    }

    private def parseParameter(parameter : String) : Tuple2[String, String] = {
        val parts = parameter split("=");

        val name = parts(0) trim

        if (name.length == 0)
            return (null, null)
        
        (
            urlDecode(name),
            if (parts.length == 1) null else urlDecode(parts(1).trim)
        )
    }

    private def urlDecode(str : String) : String = {
        java.net.URLDecoder.decode(str, "UTF-8")
    }
}