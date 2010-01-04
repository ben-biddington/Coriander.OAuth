package org.coriander.oauth.core.uri

abstract class UrlEncoder {
    def %%(value : String) : String  = {
        encode(value)
    }
    
    def encode(value : String) : String
}