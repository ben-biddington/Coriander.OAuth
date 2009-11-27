package org.coriander.oauth.core

import http.HttpVerb
import java.net.URI
import org.coriander.{NameValuePair, QueryParser, Query}
import collection.mutable.ListBuffer

// TODO: Consider combining consumer and token into single type
class SignedUri(
    uri 		: URI,
    consumer 	: OAuthCredential,
    token 		: OAuthCredential,
    timestamp 	: String,
    nonce 		: String,
    options 	: Options
) {
    val normalizer 	= new Normalizer()
    val queryParser = new QueryParser()
    val method 		= HttpVerb.GET
    
    def value() : URI = {
        value(uri, queryParser.parse(uri))
    }

    private def value(resource : URI, query : Query) : URI = {
		val parameters = combineParameters(resource, query)
        val normalizedParams : String = normalize(new Query(parameters))

        val signedUrl : String =
            resource.getScheme + "://" +
            resource.getAuthority +
            resource.getPath + "?" +
            normalizedParams.toString

        return new URI(signedUrl)
    }

	private def combineParameters(resource : URI, query : Query) : List[NameValuePair] = {
		var oauthParams = getOAuthParamsWithSignature(sign(resource, query))

        var parameters : ListBuffer[NameValuePair] = new ListBuffer[NameValuePair]()

        oauthParams.foreach(item => parameters += new NameValuePair(item.name, item.value))

        query.foreach(nvp => parameters += nvp)

		parameters toList
	}

	private def getOAuthParamsWithSignature(signature : String) : ListBuffer[NameValuePair] = {
        var oauthParams = getOAuthParams
		oauthParams += new NameValuePair(Parameters.Names.SIGNATURE, signature)
		oauthParams
    }
	
    private def getOAuthParams() : ListBuffer[NameValuePair] = {
        var result = new ListBuffer[NameValuePair]

		result.appendAll(
			new Parameters(
				consumer,
				token,
				timestamp,
				nonce,
				options
        	) toList
		)
		
		result
    }

    private def sign(resource : URI, query : Query) : String = {
        val signatureBaseString = new SignatureBaseString(
            method,
            uri,
            query,
            consumer,
			token,
            nonce,
            timestamp,
			Options.DEFAULT
        )

 		new Signature(consumer, token) sign(signatureBaseString toString)
    }

    private def normalize(query : Query) : String = {
        normalizer.normalize(query)
    }
}
