package org.coriander.oauth

import java.net.URI

// TODO: Consider combining consumer and token into single type, here and in
// all other places that take these two args.
class SignedUri(
    val uri : URI,
    val consumer : OAuthCredential,
    val token : OAuthCredential,
    val signatureMethod : String,
    val timestamp : String,
    val nonce : String,
    val version : String
) {
    val _normalizer = new Normalizer()

    def value() : URI = {
        val queryParams : Map[String, String] = new QueryParser().parse(uri)

        value(uri, queryParams)
    }

    private def value(uri : URI, params : Map[String, String]) : URI = {
        val oauthParams = new Parameters(
            consumer,
            signatureMethod,
            timestamp,
            nonce,
            version
        ) toMap

        val normalizedParams = normalize(params ++ oauthParams)

        val signedUrl : String =
            uri.getScheme + "://" +
            uri.getAuthority +
            uri.getPath + "?" +
            normalizedParams

        return new URI(signedUrl)
    }

    private def getParameters(uri : URI) : Map[String, String] = {
        val temp = new QueryParser().parse(uri)
        val result = Map()

        return Map()
    }

    private def normalize(nameValuePairs : Map[String, String]) : String = {
        _normalizer.normalize(nameValuePairs)
    }
}
