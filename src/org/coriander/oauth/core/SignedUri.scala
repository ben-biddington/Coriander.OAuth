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

import cryptography.signing.HmacSha1Signature
import http.HttpVerb
import java.net.URI
import org.coriander.{NameValuePair, QueryParser, Query}
import collection.mutable.ListBuffer
import uri.OAuthUrlEncoder

// TODO: Consider adding support for RSA-SHA1
class SignedUri(
    uri 		: URI,
    credentials : CredentialSet,
    timestamp 	: String,
    nonce 		: String,
    options 	: Options
) {
	def this(
		uri 		: URI,
    	credentials : CredentialSet,
    	timestamp 	: String,
    	nonce 		: String
	) {
		this(uri, credentials, timestamp, nonce, Options.DEFAULT)
	}
    
    lazy val value : URI = value(uri, queryParser.parse(uri))            

    private def value(resource : URI, query : Query) : URI = {
		val parameters = combineParameters(resource, query).map(nvp => %%(nvp))
		
        val normalizedParams : String = normalize(new Query(parameters))

        val signedUrl : String =
            resource.getScheme	 		+
			"://" 						+
            resource.getAuthority 		+
            resource.getPath 			+
			"?" 						+
            normalizedParams.toString

        return new URI(signedUrl)
    }

	private def %%(nameValuePair : NameValuePair) : NameValuePair =
		new NameValuePair(
			urlEncoder.%%(nameValuePair.name),
			urlEncoder.%%(nameValuePair.value)
		)
	
	private def combineParameters(resource : URI, query : Query) : List[NameValuePair] = {
		var oauthParams = getOAuthParamsWithSignature(sign(resource, query))

        var parameters = new ListBuffer[NameValuePair]()

        oauthParams foreach(item =>
			parameters += new NameValuePair(item.name, item.value)
		)

        query foreach(parameters += _)

		parameters toList
	}

	private def getOAuthParamsWithSignature(signature : String) : ListBuffer[NameValuePair] = {
        var oauthParams = getOAuthParameters
		oauthParams += new NameValuePair(Parameters.Names.SIGNATURE, signature)
		oauthParams
    }
	
    private def getOAuthParameters() : ListBuffer[NameValuePair] = {
        var result = new ListBuffer[NameValuePair]

		result appendAll(
			new Parameters(
				credentials,
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
            credentials,
            nonce,
            timestamp,
			Options.DEFAULT
        )

 		new HmacSha1Signature(credentials) sign(signatureBaseString)
    }

    private def normalize(query : Query) = normalizer normalize(query)

	val normalizer 	= new Normalizer
    val queryParser = new QueryParser
    val method 		= HttpVerb.GET
	val urlEncoder 	= new OAuthUrlEncoder
}