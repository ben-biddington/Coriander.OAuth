package org.coriander.oauth.core

import org.coriander.NameValuePair
import collection.mutable.ListBuffer

final class Parameters(
    val consumer 	: OAuthCredential,
 	val token 		: OAuthCredential,
    val timestamp 	: String,
    val nonce 		: String,
    val options 	: Options
) {
	def toList : List[NameValuePair] = {
		var result = List(
			new NameValuePair(Parameters.Names.CONSUMER_KEY, consumer.key),
			new NameValuePair(Parameters.Names.SIGNATURE_METHOD, options.signatureMethod),
			new NameValuePair(Parameters.Names.TIMESTAMP, timestamp),
			new NameValuePair(Parameters.Names.NONCE, nonce),
			new NameValuePair(Parameters.Names.VERSION, options.version toString)
		)

		if (token != null) {
			var buffer = new ListBuffer[NameValuePair]
			buffer.appendAll(result)

			val tokenParameter = new NameValuePair(
				Parameters.Names.TOKEN,
				if (token != null) token.key else ""
			)

			buffer += tokenParameter

			result = buffer toList
		}

		result
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
