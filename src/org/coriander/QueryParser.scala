package org.coriander

class QueryParser {
    val DELIMITER = "&"

    def parse(uri : java.net.URI) : Query = {
        parse(uri.getQuery)
    }

    def parse(query : String) : Query = {
        parseNameValuePairs(query, DELIMITER)
    }

    private def parseNameValuePairs(value : String, delimiter : String) : Query = {

        var temp : List[NameValuePair] = List()

        if (null == value || value.trim == "")
            return new Query()

        value split(delimiter) foreach(pair => {
            val nameAndValue = parseNameValuePair(pair)
            
            if (nameAndValue != null) {
                temp += nameAndValue
            }
        });

        new Query(temp)
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

    private def urlDecode(str : String) : String = {
        java.net.URLDecoder.decode(str, "UTF-8")
    }
}