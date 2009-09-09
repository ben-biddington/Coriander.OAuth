package org.coriander.oauth

import scala.collection.immutable._
import java.net._
import javax.crypto
import java.net.URI
import org.apache.http.protocol.HTTP.UTF_8
import org.apache.commons.codec.binary.Base64.encodeBase64

// OAuth, see: http://oauth.net/core/1.0/#anchor14
// OAuth, see: http://oauth.net/core/1.0#sig_base_example
class SignatureBaseString (
    method              : String,
    uri                 : URI,
    queryParams         : Map[String, String],
    consumerCredential  : OAuthCredential,
    nonce               : String,
    timestamp           : String
) {
    val signatureMethod = "HMAC-SHA1"
    var value : String  = null

    def this(
        uri                 : URI,
        queryParams         : Map[String, String],
        consumerCredential  : OAuthCredential,
        nonce               : String,
        timestamp           : String
    ) {
        this("get", uri, queryParams, consumerCredential, nonce, timestamp)
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
        val normalizedParams : String = normalize(queryParams ++ getOAuthParameters)
        
        val requestUrl : String = uri.getScheme + "://" + uri.getHost + uri.getPath

        // METHOD&URL&NORMALIZED_PARAMS

        val result = String format(
            "%1$s%2$s%3$s",
            method.toUpperCase + "&",
            %%(requestUrl) + "&",
            %%(normalizedParams)
        );

        result
    }

    // OAuth, see: http://oauth.net/core/1.0/#anchor14 (9.1.1)
    private def sort(queryParams : Map[String, String]) : SortedMap[String, String] = {
        return new TreeMap[String, String] ++ queryParams
    }

    private def normalize(params : Map[String, String]) : String = {
        sort(params) map {
            case (name, value) => { %%(name) + "=" + %%(value) }
        } mkString "&"
    }

    private def getOAuthParameters() : Map[String, String] = {
        return Map(
            "oauth_consumer_key"        -> consumerCredential.key,
            "oauth_signature_method"    -> signatureMethod,
            "oauth_timestamp"           -> timestamp,
            "oauth_nonce"               -> nonce.toString,
            "oauth_version"             -> "1.0"
        )
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
}
