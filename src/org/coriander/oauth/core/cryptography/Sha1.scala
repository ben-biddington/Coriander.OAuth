package org.coriander.oauth.core.cryptography

import javax.crypto

class Sha1 extends Hmac {
	def create(key : String, message : String) = hmac(bytes(key), bytes(message))

	private def hmac(key : Array[Byte], message : Array[Byte]) : Array[Byte] = {
        val secret = new crypto.spec.SecretKeySpec(key, algorithm)
		val mac = newMac

		mac.init(secret)
        mac.doFinal(message)
    }

	private def bytes(what : String) = what.getBytes(encoding)

	private def newMac 		= crypto.Mac.getInstance(algorithm)
	private val algorithm 	= "HMacSha1"
    private val encoding 	= org.apache.http.protocol.HTTP.UTF_8
}