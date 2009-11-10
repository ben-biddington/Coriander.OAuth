package org.coriander.oauth

import scala.collection.immutable._
import java.net._
import javax.crypto
import java.net.URI
import org.apache.http.protocol.HTTP.UTF_8
import org.apache.commons.codec.binary.Base64.encodeBase64

import org.coriander.oauth.uri._
import org.coriander.{NameValuePair, Query}

class SignatureBaseString (
    method              : String,
    uri                 : URI,
    query               : Query,
    consumerCredential  : OAuthCredential,
    nonce               : String,
    timestamp           : String
) {
    val signatureMethod = "HMAC-SHA1"
    var value : String  = null
    val defaultPorts = List(new Port("http", 80), new Port("https", 443))
    var urlEncoder = new OAuthURLEncoder()
    var version  =  "1.0"
    
    def this(
        uri                 : URI,
        query               : Query,
        consumerCredential  : OAuthCredential,
        nonce               : String,
        timestamp           : String
    ) {
        this("get", uri, query, consumerCredential, nonce, timestamp)
    }

    override def toString() : String = {
        return getSignatureBaseString
    }
    
    def getSignatureBaseString() : String = {
        return getSignatureBaseString(uri, query)
    }

    def getSignatureBaseString(uri : URI, query : Query) : String = {

        var tempQuery = Query.copy(query)
        
        getOAuthParameters.foreach(item => {
            val (name, value) = item
            tempQuery = tempQuery += new NameValuePair(name, value)
        })

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

    private def getOAuthParameters() : Map[String, String] = {
        return new org.coriander.oauth.Parameters(
            consumerCredential,
            signatureMethod,
            timestamp,
            nonce,
            version
        ) toMap
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
