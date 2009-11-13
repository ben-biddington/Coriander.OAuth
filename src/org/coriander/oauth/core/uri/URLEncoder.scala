package org.coriander.oauth.uri

abstract class URLEncoder {
    def %%(value : String) : String  = {
        encode(value)
    }
    
    def encode(value : String) : String
}