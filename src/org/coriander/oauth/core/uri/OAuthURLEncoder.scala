package org.coriander.oauth.core.uri

class OAuthURLEncoder extends URLEncoder {
    def encode(value : String) : String = {
        if (null == value) return ""

        java.net.URLEncoder.encode(value).replace("+", "%20").replace("%7E", "~");
    }
}