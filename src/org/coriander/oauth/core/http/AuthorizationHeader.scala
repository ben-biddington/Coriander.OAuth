package org.coriander.oauth.core.http

class AuthorizationHeader(
    realm       : String,
    consumerKey : String,
    tokenkey    : String,
    algorithm   : String,
    signature   : String,
    timestamp   : String,
    nonce       : String,
    version     : String,
    urlEncoder  : org.coriander.oauth.uri.URLEncoder
) {
    val name = "Authorization"
    val value = formatValue(createValue)
    
    override def toString : String  = {
         name + ": " + value
    }

    private def formatValue(value : String) : String = {
        "OAuth " + value
    }

    private def createValue : String = {
        requireUrlEncoder
        
        "realm=\""                  + realm + "\"," +
        "oauth_consumer_key=\""     + urlEncoder.%%(consumerKey) + "\"," +
        "oauth_token=\""            + urlEncoder.%%(tokenkey) + "\"," +
        "oauth_signature_method=\"" + urlEncoder.%%(algorithm) + "\"," +
        "oauth_signature=\""        + urlEncoder.%%(signature) + "\"," +
        "oauth_timestamp=\""        + urlEncoder.%%(timestamp) + "\"," +
        "oauth_nonce=\""            + urlEncoder.%%(nonce) + "\"," +
        "oauth_version=\""          + urlEncoder.%%(version) + "\""
    }

    private def requireUrlEncoder {
        if (null == urlEncoder)
            throw new Exception("No urlEncoder has been supplied.")
    }
}
