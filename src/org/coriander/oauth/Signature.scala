package org.coriander.oauth

import scala.collection.immutable._
import java.net._
import javax.crypto
import java.net.URI
import org.apache.http.protocol.HTTP.UTF_8
import org.apache.commons.codec.binary.Base64.encodeBase64

class Signature(
    urlEncoder              : org.coriander.oauth.uri.URLEncoder,
    consumerCredential      : OAuthCredential,
    token                   : OAuthCredential
) {

    def this(
        urlEncoder          : org.coriander.oauth.uri.URLEncoder,
        consumerCredential  : OAuthCredential,
        token               : OAuthCredential,
        algorithm           : String
    ) {
        this(urlEncoder, consumerCredential, token)
        this.algorithm = algorithm
    }

    def this(
        urlEncoder          : org.coriander.oauth.uri.URLEncoder,
        consumerCredential  : OAuthCredential
    ) {
        this(urlEncoder, consumerCredential, null)
    }
    
    val DEFAULT_ALGORITHM = "HMacSha1"
    var algorithm = DEFAULT_ALGORITHM
    val DEFAULT_TOKEN_SECRET : String = ""
    val mac = crypto.Mac.getInstance(algorithm)
    val encoding = UTF_8

   

    def sign(baseString : String) : String = {
        validate

        getSignature(baseString);
    }

    private def getSignature(baseString : String) : String = {
        mac.init(getKey)
        new String(encodeBase64(mac.doFinal(baseString.getBytes)))
    }

    private def getKey : crypto.spec.SecretKeySpec = {
        new crypto.spec.SecretKeySpec(
            formatKey getBytes(encoding),
            algorithm
        );
    }

    // See: http://oauth.net/core/1.0, section 9.2
    private def formatKey : String = {
        %%(getConsumerSecret) + "&" + %%(getTokenSecret)
    }

    private def getConsumerSecret : String = {
        if (consumerCredential != null) consumerCredential.secret else null
    }

    private def getTokenSecret : String = {
       if (token != null) token.secret else DEFAULT_TOKEN_SECRET
    }

    private def %% (value : String) : String = {
        urlEncoder.encode(value)
    }

    private def validate {
        validateAlgorithm
        requireURLEncoder
        validateConsumerCredential
        validateToken
    }

    private def requireURLEncoder {
        if (null == urlEncoder)
            throw new Exception("Please supply a URLEncoder")
    }

    private def validateConsumerCredential {
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

    private def validateToken {
        if (token != null && token.secret == null)
            throw new Exception("The supplied token is missing a secret.")
    }

    private def validateAlgorithm {
        if (algorithm != DEFAULT_ALGORITHM)
            throw new Exception(
                "Unsupported algorithm. Currently only 'HMacSha1' is supported."
            )
    }
}
