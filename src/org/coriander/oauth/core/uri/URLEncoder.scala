package org.coriander.oauth.core.uri

abstract class URLEncoder {
    def %%(value : String) : String  = {
        encode(value)
    }
    
    def encode(value : String) : String
}