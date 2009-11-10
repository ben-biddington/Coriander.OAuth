package org.coriander.oauth

class Parameters(
    val consumer : OAuthCredential,
    val signatureMethod : String,
    val timestamp : String,
    val nonce : String,
    val version : String
) {

    def toMap : Map[String, String] = {
        Map(
            "oauth_consumer_key"        -> consumer.key,
            "oauth_signature_method"    -> signatureMethod,
            "oauth_timestamp"           -> timestamp,
            "oauth_nonce"               -> nonce,
            "oauth_version"             -> version
        )
    }
}
