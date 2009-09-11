package org.coriander.oauth.uri

class OAuthURLEncoder extends URLEncoder {
    def encode(value : String) : String = {
        if (null == value) return ""

        return java.net.URLEncoder encode(value) replace
            ("+", "%20") replace
            ("%7E", "~");
    }
}