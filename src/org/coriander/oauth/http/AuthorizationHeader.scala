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
        name + ": " + value;
    }
}
