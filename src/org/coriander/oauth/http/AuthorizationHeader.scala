package org.coriander.oauth.http

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
    val headername = "Authorization"

    override def toString : String  = {
        formatHeader(headername, "");
    }

    private def formatHeader(name : String, value : String) : String = {
        name + ": OAuth " + createValue
    }

    private def createValue : String = {
        requireUrlEncoder
        
        "realm=\""                  + urlEncoder.%%(realm) + "\"," +
        "oauth_consumer_key=\""     + urlEncoder.%%(consumerKey) + "\"," +
        "oauth_token=\""            + urlEncoder.%%(tokenkey)  + "\"," +
        "oauth_signature_method=\"" + urlEncoder.%%(algorithm)  + " \"," +
        "oauth_signature=\""        + urlEncoder.%%(signature)  + "\"," +
        "oauth_timestamp=\""        + urlEncoder.%%(timestamp)  + "\"," +
        "oauth_nonce=\""            + urlEncoder.%%(nonce)  + "\"," +
        "oauth_version=\""          + urlEncoder.%%(version)  + "\""
    }

    private def requireUrlEncoder {
        if (null == urlEncoder)
            throw new Exception("No urlEncoder has been supplied.")
    }
}
