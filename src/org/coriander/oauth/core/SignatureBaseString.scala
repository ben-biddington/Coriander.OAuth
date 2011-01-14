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

import http.HttpVerb
import java.net.URI

import org.coriander.oauth.core.uri._
import org.coriander.{NameValuePair, Query}
import java.lang.String

final class SignatureBaseString (
    method      : String,
    uri         : URI,
    query       : Query,
    credentials : CredentialSet,
    nonce       : String,
    timestamp   : String,
	options		: Options
) {
    var value           = null
    val defaultPorts 	= List(Port("http", 80), Port("https", 443))
    var urlEncoder 		= new OAuthUrlEncoder
    val DELIMITER		= "&"

    def this(
        uri         : URI,
        query       : Query,
        credentials : CredentialSet,
        nonce       : String,
        timestamp   : String
    ) {
        this(HttpVerb.GET, uri, query, credentials, nonce, timestamp, Options.DEFAULT)
    }

    override def toString = getSignatureBaseString
    
    private def getSignatureBaseString : String = getSignatureBaseString(uri, query)

    private def getSignatureBaseString(uri : URI, query : Query) : String = {
		val allParameters = combineOAuthParametersWith(query).map(nvp => %%(nvp))

		String format(
            "%1$s%2$s%3$s",
            method.toUpperCase 			+ DELIMITER,
            %%(getAbsolute(uri)) 		+ DELIMITER,
            %%(normalize(allParameters))
        );
    }

	private def combineOAuthParametersWith(query : Query) : Query = {
		var tempQuery = Query.copy(query)

        getOAuthParameters.foreach(item => tempQuery = tempQuery += item)

		tempQuery
	}

	private def getAbsolute(uri : URI) =
		uri.getScheme.toLowerCase + "://" + selectAuthority(uri).toLowerCase + uri.getPath

    private def selectAuthority(uri : URI) : String =
        return if (containsDefaultPort(uri)) uri getHost else uri getAuthority;

    private def containsDefaultPort(uri : URI) : Boolean =
        defaultPorts exists(port =>
			port.scheme == uri.getScheme &&
			port.number == uri.getPort
        )

    private def getOAuthParameters : List[NameValuePair] = new Parameters(
		credentials,
		timestamp,
		nonce,
		options
	) toList

    private def %%(nameValuePair : NameValuePair) : NameValuePair =
		new NameValuePair(%%(nameValuePair.name), %%(nameValuePair.value))

    private def %%(value : String) : String =
		return if (value != null) urlEncoder.%%(value.toString) else ""

    private def normalize(query : Query) : String = new Normalizer normalize(query)
}

object SignatureBaseString {
    implicit def to_string(instance : SignatureBaseString) = instance toString
}
