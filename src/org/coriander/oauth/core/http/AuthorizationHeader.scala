package org.coriander.oauth.core.http

import org.coriander.oauth.core.{CredentialSet, Options}
import org.coriander.oauth.core.uri.UrlEncoder

class AuthorizationHeader(
    realm       : String,
    credentials : CredentialSet,
    signature   : String,
    timestamp   : String,
    nonce       : String,
    options     : Options,
    urlEncoder  : UrlEncoder
) {
    val name = "Authorization"
    val value = formatValue(createValue)
    
    override def toString = name + ": " + value

	private def formatValue(value : String) = "OAuth " + value

    private def createValue : String = {
        requireUrlEncoder
        
        "realm=\""                  + realm + "\"," +
        "oauth_consumer_key=\""     + urlEncoder.%%(credentials.consumer.key) + "\"," +
        "oauth_token=\""            + (if (credentials.hasToken) urlEncoder.%%(credentials.token.key) else "") + "\"," +
        "oauth_signature_method=\"" + urlEncoder.%%(options.signatureMethod) + "\"," +
        "oauth_signature=\""        + urlEncoder.%%(signature) + "\"," +
        "oauth_timestamp=\""        + urlEncoder.%%(timestamp) + "\"," +
        "oauth_nonce=\""            + urlEncoder.%%(nonce) + "\"," +
        "oauth_version=\""          + urlEncoder.%%(options.version.toString) + "\""
    }

    private def requireUrlEncoder {
        require (urlEncoder != null, "No urlEncoder has been supplied.")
    }
}
