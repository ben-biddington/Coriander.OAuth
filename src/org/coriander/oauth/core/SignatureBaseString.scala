package org.coriander.oauth.core

import http.HttpVerb
import java.net.URI

import org.coriander.oauth.core.uri._
import org.coriander.{NameValuePair, Query}

class SignatureBaseString (
    method              : String,
    uri                 : URI,
    query               : Query,
    consumerCredential  : OAuthCredential,
	token 				: OAuthCredential,
    nonce               : String,
    timestamp           : String,
	options				: Options
) {
    var value : String  = null
    val defaultPorts 	= List(new Port("http", 80), new Port("https", 443))
    var urlEncoder 		= new OAuthURLEncoder()
    
    def this(
        uri                 : URI,
        query               : Query,
        consumerCredential  : OAuthCredential,
		token 				: OAuthCredential,
        nonce               : String,
        timestamp           : String
    ) {
        this(HttpVerb.GET, uri, query, consumerCredential, token, nonce, timestamp, Options.DEFAULT)
    }

    override def toString() : String = {
        return getSignatureBaseString
    }
    
    def getSignatureBaseString() : String = {
        return getSignatureBaseString(uri, query)
    }

    def getSignatureBaseString(uri : URI, query : Query) : String = {

        var tempQuery = Query.copy(query)

        getOAuthParameters.foreach(item =>
            tempQuery = tempQuery += new NameValuePair(item.name, item.value)
        )

        val normalizedParams = normalize(tempQuery)
        
        val requestUrl = uri.getScheme + "://" + selectAuthority(uri) + uri.getPath

        String format(
            "%1$s%2$s%3$s",
            method.toUpperCase + "&",
            %%(requestUrl) + "&",
            %%(normalizedParams)
        );
    }

    private def selectAuthority(uri : URI) : String = {
        return if (containsDefaultPort(uri)) uri getHost else uri getAuthority
    }

    private def containsDefaultPort(uri : URI) : Boolean = {
        defaultPorts exists(port =>
			port.scheme == uri.getScheme &&
			port.number == uri.getPort
        )
    }

    private def getOAuthParameters : List[NameValuePair] = {
        return new Parameters(
            consumerCredential,
			token,
            timestamp,
            nonce,
            options
        ) toList
    }
   
    private def %% (str : String) : String = {
		return if (str != null) urlEncoder.%%(str.toString) else ""
    }

    private def normalize(query : Query) : String = {
		new Normalizer().normalize(query)
    }
}

object SignatureBaseString {
    implicit def to_string(instance : SignatureBaseString) : String = {
        return instance.toString
    }
}
