package org.coriander.oauth

import scala.collection.immutable._
import java.net._
import javax.crypto
import java.net.URI
import org.apache.http.protocol.HTTP.UTF_8
import org.apache.commons.codec.binary.Base64.encodeBase64

class Signature(consumerCredential : OAuthCredential, token : OAuthCredential) {

    val algorithm = "HmacSHA1"
    val DEFAULT_TOKEN_SECRET : String = ""

    def this(consumerCredential : OAuthCredential) {
        this(consumerCredential, null)
    }

    def sign(baseString : String) : String = {
        validate

        getSignature(baseString);
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

    // See: http://oauth.net/core/1.0, section 9.2
    private def getKey : crypto.spec.SecretKeySpec = {
        val key = getConsumerSecret + "&" + getTokenSecret

        new crypto.spec.SecretKeySpec(
            key getBytes(UTF_8),
            algorithm
        );
    }

    private def getConsumerSecret() : String = {
        return consumerCredential.secret
    }

    private def getTokenSecret() : String = {
       if (token != null) token.secret else DEFAULT_TOKEN_SECRET
    }

    private def validate() {
        if (null == consumerCredential)
            throw new Exception("Missing the 'consumerCredential'.")

        if (null == consumerCredential.secret)
            throw new Exception(
                "The supplied ConsumerCredential has no secret defined."
            )

        if (null == consumerCredential.key)
            throw new Exception(
                "The supplied ConsumerCredential has no key defined."
            )
    }
}
