package org.coriander.oauth

import scala.collection.immutable._
import java.net._
import javax.crypto
import java.net.URI
import org.apache.http.protocol.HTTP.UTF_8
import org.apache.commons.codec.binary.Base64.encodeBase64

// Encapsulates signature behaviour, this is a signature abstraction.
// As an object, it operates on a supplied SignatureBaseString.
// 
// See: http://databinder.net/sxr/dispatch/0.4.2/main/OAuth.scala.html#14048
class Signature(consumerCredential : OAuthCredential) {

    val algorithm = "HmacSHA1"

    def sign(baseString : SignatureBaseString) : String = {
        validate

        getSignature(baseString.toString);

        // TODO: This returns a URI, i.e., it is meant to assemble a signed URI.
        // Consider redefining this to a class that simply creates the signature
        // and leave the assembly to something else.
    }

    private def getSignature(baseString : String) : String = {
        val key = getKey

        val sig = {
            val mac = crypto.Mac.getInstance(algorithm)

            mac.init(key)
            
            new String(encodeBase64(mac.doFinal(baseString.getBytes)))
        }

        sig;
    }

    private def getKey : crypto.spec.SecretKeySpec = {
        new crypto.spec.SecretKeySpec(
            consumerCredential.secret.getBytes,
            algorithm
        );
    }

    private def validate() {
        if (null == consumerCredential || null == consumerCredential.secret)
            throw new Exception("Missing the 'consumerCredential'.")
    }

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
