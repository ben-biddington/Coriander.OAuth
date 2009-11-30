package org.coriander.oauth.core.http

import org.coriander.oauth.core.{CredentialSet, Options}

class AuthorizationHeader(
    realm       : String,
    credentials : CredentialSet,
    signature   : String,
    timestamp   : String,
    nonce       : String,
    options     : Options,
    urlEncoder  : org.coriander.oauth.core.uri.URLEncoder
) {
    val name = "Authorization"
    val value = formatValue(createValue)
    
    override def toString : String  = name + ": " + value

    private def formatValue(value : String) : String = "OAuth " + value

    private def createValue : String = {
        requireUrlEncoder
        
        "realm=\""                  + realm + "\"," +
        "oauth_consumer_key=\""     + urlEncoder.%%(credentials.consumer.key) + "\"," +
        "oauth_token=\""            + urlEncoder.%%(credentials.token.key) + "\"," +
        "oauth_signature_method=\"" + urlEncoder.%%(options.signatureMethod) + "\"," +
        "oauth_signature=\""        + urlEncoder.%%(signature) + "\"," +
        "oauth_timestamp=\""        + urlEncoder.%%(timestamp) + "\"," +
        "oauth_nonce=\""            + urlEncoder.%%(nonce) + "\"," +
        "oauth_version=\""          + urlEncoder.%%(options.version.toString) + "\""
    }

    private def requireUrlEncoder {
        if (null == urlEncoder)
            throw new Exception("No urlEncoder has been supplied.")
    }
}
