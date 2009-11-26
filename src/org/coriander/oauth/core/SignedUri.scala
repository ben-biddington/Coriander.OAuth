package org.coriander.oauth.core

import java.net.URI
import org.coriander.{NameValuePair, QueryParser, Query}
import collection.mutable.ListBuffer
// TODO: Consider combining consumer and token into single type, here and in
// all other places that take these two args.
class SignedUri(
    uri : URI,
    consumer : OAuthCredential,
    token : OAuthCredential,
    timestamp : String,
    nonce : String,
    options : Options
) {
    val normalizer = new Normalizer()
    val queryParser = new QueryParser()
    val method = "GET"
    
    def value() : URI = {
        value(uri, queryParser.parse(uri))
    }

    private def value(resource : URI, query : Query) : URI = {
        var oauthParams = getOAuthParams + ("oauth_signature" -> sign(resource, query))

        var parameters : ListBuffer[NameValuePair] = new ListBuffer[NameValuePair]()

        oauthParams.foreach(item => {
            val (name, value) = item
            
            parameters += new NameValuePair(name, value)
        })

        query.foreach(nvp => {parameters += nvp})

        val normalizedParams : String = normalize(new Query(parameters.toList))

        val signedUrl : String =
            resource.getScheme + "://" +
            resource.getAuthority +
            resource.getPath + "?" +
            normalizedParams.toString

        return new URI(signedUrl)
    }

    private def getOAuthParams() : Map[String, String] = {
        new Parameters(
            consumer,
			token,
            options.signatureMethod,
            timestamp,
            nonce,
            options.version toString
        ) toMap
    }

    private def sign(resource : URI, query : Query) : String = {
        val signatureBaseString = new SignatureBaseString(
            method,
            uri,
            query,
            consumer,
			token,
            nonce,
            timestamp
        )

 		new Signature(consumer, token) sign(signatureBaseString toString)
    }

    private def normalize(query : Query) : String = {
        normalizer.normalize(query)
    }
}
