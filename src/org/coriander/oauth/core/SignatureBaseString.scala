package org.coriander.oauth.core

import http.HttpVerb
import java.net.URI

import org.coriander.oauth.core.uri._
import org.coriander.{NameValuePair, Query}
import CredentialSet._

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
    var urlEncoder 		= new OAuthURLEncoder
    
    def this(
        uri         : URI,
        query       : Query,
        credentials : CredentialSet,
        nonce       : String,
        timestamp   : String
    ) {
        this(HttpVerb.GET, uri, query, credentials, nonce, timestamp, Options.DEFAULT)
    }

    override def toString : String = getSignatureBaseString
    
    private def getSignatureBaseString : String = getSignatureBaseString(uri, query)

    private def getSignatureBaseString(uri : URI, query : Query) : String = {
		var tempQuery = Query.copy(query)

        getOAuthParameters.foreach(item =>
            tempQuery = tempQuery += new NameValuePair(item.name, item.value)
        )

        val requestUrl = uri.getScheme + "://" + selectAuthority(uri) + uri.getPath

        String format(
            "%1$s%2$s%3$s",
            method.toUpperCase 	+ "&",
            %%(requestUrl) 		+ "&",
            %%(normalize(tempQuery))
        );
    }

    private def selectAuthority(uri : URI) : String =
        return if (containsDefaultPort(uri)) uri getHost else uri getAuthority;

    private def containsDefaultPort(uri : URI) : Boolean =
        defaultPorts exists(port =>
			port.scheme == uri.getScheme &&
			port.number == uri.getPort
        )

    private def getOAuthParameters : List[NameValuePair] =
        return new Parameters(
            credentials,
            timestamp,
            nonce,
            options
        ) toList
   
    private def %% (str : String) : String =
		return if (str != null) urlEncoder.%%(str.toString) else ""

    private def normalize(query : Query) : String = new Normalizer normalize(query)
}

object SignatureBaseString {
    implicit def to_string(instance : SignatureBaseString) : String = return instance toString
}
