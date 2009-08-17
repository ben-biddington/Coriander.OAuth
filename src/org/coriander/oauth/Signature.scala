package org.coriander.oauth

import scala.collection.immutable._
import java.net._
import javax.crypto
import java.net.URI
import org.apache.http.protocol.HTTP.UTF_8
import org.apache.commons.codec.binary.Base64.encodeBase64

// Encapsulates signature behaviour, this is a signature abstraction.
// As an object, it operates on data. 
// 
// See: http://databinder.net/sxr/dispatch/0.4.2/main/OAuth.scala.html#14048
class Signature(val consumerKey : String, val consumerSecret : String) {
    
    def sign(uri : URI, queryParams : Map[String, String]) : URI = {
        if (null == consumerKey)
            throw new Exception("Missing the 'consumerKey'.")

        if (null == consumerSecret)
            throw new Exception("Missing the 'consumerSecret'.")

        val theStringToSign = new SignatureBaseString(
            uri,
            queryParams,
            consumerKey, 
            consumerSecret
        );
        
        return uri;
    }

  // normalize to OAuth percent encoding
  //private def %% (str: String) : String = (Http % str) replace ("+", "%20") replace ("%7E", "~")
  //private def %% (s: Seq[String]) : String = s map %% mkString "&"


//    def sign_xxx(method: String, url: String, user_params: Map[String, String], consumer: Consumer,
//        token: Option[Token], verifier: Option[String]) = {
//        val oauth_params = IMap(
//      "oauth_consumer_key" -> consumer.key,
//      "oauth_signature_method" -> "HMAC-SHA1",
//      "oauth_timestamp" -> (System.currentTimeMillis / 1000).toString,
//      "oauth_nonce" -> System.nanoTime.toString
//        ) ++ token.map { "oauth_token" -> _.value } ++
//        verifier.map { "oauth_verifier" -> _ }
//
//        val encoded_ordered_params = (
//          new TreeMap[String, String] ++ (user_params ++ oauth_params map %%)
//        ) map { case (k, v) => k + "=" + v } mkString "&"
//
//        val message = %%(method :: url :: encoded_ordered_params :: Nil)
//
//        val SHA1 = "HmacSHA1";
//        val key_str = %%(consumer.secret :: (token map { _.secret } getOrElse "") :: Nil)
//        val key = new crypto.spec.SecretKeySpec(bytes(key_str), SHA1)
//        val sig = {
//          val mac = crypto.Mac.getInstance(SHA1)
//          mac.init(key)
//          new String(encodeBase64(mac.doFinal(bytes(message))))
//        }
//        oauth_params + ("oauth_signature" -> sig)
//  }
}
