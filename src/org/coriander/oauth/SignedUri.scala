package org.coriander.oauth

import java.net.URI

// TODO: Consider combining consumer and token into single type, here and in
// all other places that take these two args.
class SignedUri(
    uri : URI,
    consumer : OAuthCredential,
    token : OAuthCredential,
    signatureMethod : String,
    timestamp : String,
    nonce : String,
    version : String
) {
    val _normalizer = new Normalizer()
    val _queryParser = new QueryParser()
    val _method = "GET"
    
    def value() : URI = {
        value(uri, _queryParser.parse(uri))
    }

    private def value(resource : URI, params : Map[String, String]) : URI = {
        var oauthParams = new Parameters(
            consumer,
            signatureMethod,
            timestamp,
            nonce,
            version
        ) toMap

        oauthParams += "oauth_signature" -> sign(resource, params)

        val normalizedParams = normalize(params ++ oauthParams)

        val signedUrl : String =
            resource.getScheme + "://" +
            resource.getAuthority +
            resource.getPath + "?" +
            normalizedParams

        return new URI(signedUrl)
    }

    private def sign(resource : URI, params : Map[String, String]) : String = {
        val signatureBaseString = new SignatureBaseString(
            _method,
            uri,
            params,
            consumer,
            nonce,
            timestamp
        )

        new Signature(consumer).sign(signatureBaseString toString)
    }

    private def normalize(nameValuePairs : Map[String, String]) : String = {
        _normalizer.normalize(nameValuePairs)
    }
}
