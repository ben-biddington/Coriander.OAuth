package org.coriander.oauth.core

class Parameters(
    val consumer : OAuthCredential,
 	val token : OAuthCredential,
    val signatureMethod : String,
    val timestamp : String,
    val nonce : String,
    val version : String
) {

    def toMap : Map[String, String] = {
        var result = Map(
            "oauth_consumer_key"        -> consumer.key,
            "oauth_signature_method"    -> signatureMethod,
            "oauth_timestamp"           -> timestamp,
            "oauth_nonce"               -> nonce,
            "oauth_version"             -> version
        )

		if (token != null) {
			result += ("oauth_token" -> {if (token != null) token.key else ""})
		}

		result
    }
}
