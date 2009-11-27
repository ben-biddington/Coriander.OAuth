package org.coriander.oauth.core

import scala.collection.immutable._
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
        this("get", uri, query, consumerCredential, token, nonce, timestamp, Options.DEFAULT)
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

        val result = String format(
            "%1$s%2$s%3$s",
            method.toUpperCase + "&",
            %%(requestUrl) + "&",
            %%(normalizedParams)
        );

        result
    }

    private def selectAuthority(uri : URI) : String = {
        if (containsDefaultPort(uri))
            return uri getHost
        else
            return uri getAuthority
    }

    private def containsDefaultPort(uri : URI) : Boolean = {
        defaultPorts exists((port) => {
            port.scheme == uri.getScheme &&
            port.number == uri.getPort
        })
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
    
    private def %% (t: (String, String)) : (String, String) = {
        (%%(t._1), %%(t._2.toString))
    }

    private def %% (str : String) : String = {
      if (null == str) return ""

      return urlEncoder.%%(str.toString)
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
