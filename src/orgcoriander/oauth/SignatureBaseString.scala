package org.coriander.oauth

import scala.collection.immutable._
import java.net._

//import collection.Map
//import collection.immutable.{TreeMap, Map=>IMap}

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

    def this(
        uri             : URI,
        queryParams     : Map[String, String],
        consumerKey     : String,
        consumerSecret  : String
    ) {
        this("GET", uri, queryParams, consumerKey, consumerSecret)
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
        val oauthParams = getOAuthParameters()

        val combined : SortedMap[String, String] = sort(queryParams ++ oauthParams)

        val combinedParameters = combined map {
            case (name, value) => {name + "=" + %%(value) }
        } mkString "&"

        //        val encodedOrderedParams = (
        //            (queryParams ++ oauthParams) map %%
        //        ) map { case (k, v) => k + "=" + v } mkString "&"

                //val message = %%(method :: url :: encoded_ordered_params :: Nil)

        uri + "?" + combinedParameters
    }

    // OAuth, see: http://oauth.net/core/1.0/#anchor14 (9.1.1)
    private def sort(queryParams : Map[String, String]) : SortedMap[String, String] = {
        return new TreeMap[String, String] ++ queryParams
    }

    private def getOAuthParameters() : Map[String, String] = {
        return Map(
            "oauth_consumer_key" -> consumerKey,
            "oauth_signature_method" -> "HMAC-SHA1",
            "oauth_timestamp" -> (System.currentTimeMillis / 1000).toString,
            "oauth_nonce" -> System.nanoTime.toString
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

    implicit def to_uri(instance : SignatureBaseString) : URI = {
        return new URI(instance.toString)
    }
}
