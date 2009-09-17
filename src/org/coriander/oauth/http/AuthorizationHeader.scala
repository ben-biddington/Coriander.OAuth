package org.coriander.oauth.http

// TODO: Consider putting all of this oauth info (realm to version) in a data structure.
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
        "realm=\"http://sp.example.com/\"," +
        "oauth_consumer_key=\"0685bd9184jfhq22\"," +
        "oauth_token=\"ad180jjd733klru7\"," +
        "oauth_signature_method=\"HMAC-SHA1\", " +
        "oauth_signature=\"wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D\"," +
        "oauth_timestamp=\"137131200\", " +
        "oauth_nonce=\"4572616e48616d6d65724c61686176\", " +
        "oauth_version=\"1.0\""
    }
}
