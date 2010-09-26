package org.coriander.oauth.core

import cryptopgraphy.{Hmac, Sha1}
import org.apache.commons.codec.binary.Base64.encodeBase64
import org.coriander.oauth.core.uri._

class Signature(urlEncoder : UrlEncoder, credentials : CredentialSet) {
    def this(
        urlEncoder  : UrlEncoder,
        credentials : CredentialSet,
        hmac   		: Hmac
    ) {
        this(urlEncoder, credentials)
        this.hmac = hmac
    }

    def this(credentials : CredentialSet) = this(
		new OAuthUrlEncoder,
		credentials
	)

    def sign(baseString : String) = {
        validate
		
        getSignature(baseString);
    }

    private def getSignature(baseString : String) : String = {
		val theHmac = hmac.create(formatKey, baseString)
		new String(encodeBase64(theHmac))
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
        requireUrlEncoder
        validateConsumerCredential
        validateToken
    }

    private def requireUrlEncoder {
        require (urlEncoder != null, "Please supply a UrlEncoder.")
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

	private val DEFAULT_TOKEN_SECRET 	= ""
	private var hmac : Hmac 			= new Sha1
    private val encoding 				= org.apache.http.protocol.HTTP.UTF_8
}
