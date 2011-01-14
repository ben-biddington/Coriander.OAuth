/*
Copyright 2011 Ben Biddington

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.coriander.oauth.core

import org.coriander.NameValuePair
import collection.mutable.ListBuffer

final class Parameters(
    val credentials : CredentialSet,
    val timestamp 	: String,
    val nonce 		: String,
    val options 	: Options
) {
	def toList : List[NameValuePair] = defaultList
	
    private def defaultList : List[NameValuePair] = {
        List(
            new NameValuePair(Parameters.Names.CONSUMER_KEY, credentials.consumer key),
            new NameValuePair(Parameters.Names.SIGNATURE_METHOD, options signatureMethod),
            new NameValuePair(Parameters.Names.TIMESTAMP, timestamp),
            new NameValuePair(Parameters.Names.NONCE, nonce),
            new NameValuePair(Parameters.Names.VERSION, options.version toString),
            new NameValuePair(Parameters.Names.TOKEN, tokenKey(credentials))
        )
    }

	private def tokenKey(credentials : CredentialSet) = {
		if (credentials.hasToken && credentials.token.key != null)
			credentials.token.key
		else ""
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
