package org.coriander.oauth

import scala.collection.immutable._
import java.net._
import javax.crypto
import java.net.URI
import org.apache.http.protocol.HTTP.UTF_8
import org.apache.commons.codec.binary.Base64.encodeBase64

// OAuth, see: http://oauth.net/core/1.0/#anchor14
class SignatureBaseString (
    val method          : String,
    val uri             : URI,
    val queryParams     : Map[String, String],
    var consumerKey     : String,
    val consumerSecret  : String
) {
    val signatureMethod = "HMAC-SHA1"
    var value : String  = null

    def this(
        uri             : URI,
        queryParams     : Map[String, String],
        consumerKey     : String,
        consumerSecret  : String
    ) {
        this("get", uri, queryParams, consumerKey, consumerSecret)
    }

    override def toString() : String = {
        return getSignatureBaseString
    }
    
    def getSignatureBaseString() : String = {
        return getSignatureBaseString(uri, queryParams)
    }

    // See: http://www.docjar.com/docs/api/org/apache/commons/httpclient/util/ParameterFormatter.html
    // OAuth, see: http://oauth.net/core/1.0/#anchor14
    def getSignatureBaseString(uri : URI, queryParams : Map[String, String]) : String = {
        val combinedParameters = sort(queryParams ++ getOAuthParameters) map {
            case (name, value) => { %%(name) + "=" + %%(value) }
        } mkString "&"

        String format(
            "%1$s%2$s://%3$s%4$s?%5$s",
            method.toLowerCase,
            uri.getScheme,
            uri.getHost,
            uri.getPath,
            combinedParameters
        );
    }

    // OAuth, see: http://oauth.net/core/1.0/#anchor14 (9.1.1)
    private def sort(queryParams : Map[String, String]) : SortedMap[String, String] = {
        return new TreeMap[String, String] ++ queryParams
    }

    private def getOAuthParameters() : Map[String, String] = {
        return Map(
            "oauth_consumer_key"        -> consumerKey,
            "oauth_signature_method"    -> signatureMethod,
            "oauth_timestamp"           -> createTimestamp,
            "oauth_nonce"               -> createNonce
        )
    }

    private def createNonce : String = {
        System.nanoTime.toString;
    }

    private def createTimestamp : String = {
        (System.currentTimeMillis / 1000).toString;
    }
    
    private def %% (t: (String, String)) : (String, String) = {
        (%%(t._1), %%(t._2.toString))
    }

    private def %% (str : String) : String = {
      if (null == str) return ""

      return java.net.URLEncoder.encode(str.toString) replace
        ("+", "%20") replace
        ("%7E", "~");
    }
}

object SignatureBaseString {
    implicit def to_string(instance : SignatureBaseString) : String = {
        return instance.toString
    }

    implicit def to_uri(instance : SignatureBaseString) : URI = {
        return new URI(instance.toString)
    }
}
