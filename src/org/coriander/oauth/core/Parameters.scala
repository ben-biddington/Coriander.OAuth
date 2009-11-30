package org.coriander.oauth.core

import org.coriander.NameValuePair
import collection.mutable.ListBuffer

final class Parameters(
    val credentials : CredentialSet,
    val timestamp 	: String,
    val nonce 		: String,
    val options 	: Options
) {
	def toList : List[NameValuePair] =
		if (credentials.hasToken) addToken(defaultList) else defaultList
	
    private def defaultList : List[NameValuePair] = {
        List(
            new NameValuePair(Parameters.Names.CONSUMER_KEY, credentials.consumer key),
            new NameValuePair(Parameters.Names.SIGNATURE_METHOD, options signatureMethod),
            new NameValuePair(Parameters.Names.TIMESTAMP, timestamp),
            new NameValuePair(Parameters.Names.NONCE, nonce),
            new NameValuePair(Parameters.Names.VERSION, options.version toString)
        )
    }

    private def addToken(to : List[NameValuePair]) : List[NameValuePair] = {
        var buffer = new ListBuffer[NameValuePair]
        buffer.appendAll(to)

        buffer += new NameValuePair(
            Parameters.Names.TOKEN,
            if (credentials.token.key != null) credentials.token.key else ""
        )
        
        buffer toList
    }
}

object Parameters {
	object Names {
		val CONSUMER_KEY 		= "oauth_consumer_key"
		val SIGNATURE_METHOD 	= "oauth_signature_method"
		val TIMESTAMP 			= "oauth_timestamp"
		val NONCE				= "oauth_nonce"
		val VERSION				= "oauth_version"
		val TOKEN				= "oauth_token"
		val SIGNATURE			= "oauth_signature"
	}
}
