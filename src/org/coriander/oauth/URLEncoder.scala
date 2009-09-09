package org.coriander.oauth

class URLEncoder {}

object URLEncoder {
    def %% (str : String) : String = {
        if (null == str) return ""

        return java.net.URLEncoder.encode(str) replace
            ("+", "%20") replace
            ("%7E", "~");
    }
}