package org.coriander.oauth.core

import javax.crypto
import org.apache.commons.codec.binary.Base64.encodeBase64
import org.coriander.oauth.core.uri._

class Signature(urlEncoder : URLEncoder, credentials : CredentialSet) {
    def this(
        urlEncoder  : URLEncoder,
        credentials : CredentialSet,
        algorithm   : String
    ) {
        this(urlEncoder, credentials)
        this.algorithm = algorithm
    }

    def this(credentials : CredentialSet) = this(
		new OAuthURLEncoder,
		credentials
	)

    def sign(baseString : String) = {
        validate

        getSignature(baseString);
    }

    private def getSignature(baseString : String) : String = {
        mac.init(getSecretKey)
        new String(encodeBase64(mac.doFinal(baseString.getBytes)))
    }

    private def getSecretKey : crypto.spec.SecretKeySpec = {
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
        if (credentials hasConsumer) credentials.consumer.secret else null
    }

    private def getTokenSecret : String = {
    	if (credentials hasToken) credentials.token.secret else DEFAULT_TOKEN_SECRET
    }

    private def %% (value : String) : String = urlEncoder.encode(value)

    private def validate {
        validateAlgorithm
        requireURLEncoder
        validateConsumerCredential
        validateToken
    }

    private def requireURLEncoder {
        require (urlEncoder != null, "Please supply a URLEncoder.")
    }

    private def validateConsumerCredential {
        require (credentials.hasConsumer, "The supplied 'credentials' is missing a consumer.")
		require (credentials.consumer.key != null, "The supplied consumer has no key defined.")
		require (credentials.consumer.secret != null, "The supplied consumer has no secret defined.")
    }

    private def validateToken {
        if (credentials.hasToken) {
			require(credentials.token.secret != null, "The supplied token is missing a secret.")
		} 
    }

    private def validateAlgorithm {
        require (
			algorithm eq DEFAULT_ALGORITHM,
			"Unsupported algorithm. Currently only '" + DEFAULT_ALGORITHM + "' is supported."
		)
    }

	private val DEFAULT_ALGORITHM 		= "HMacSha1"
	private val DEFAULT_TOKEN_SECRET 	= ""
	private var algorithm 				= DEFAULT_ALGORITHM
    private val mac 					= crypto.Mac.getInstance(algorithm)
    private val encoding 				= org.apache.http.protocol.HTTP.UTF_8
}
