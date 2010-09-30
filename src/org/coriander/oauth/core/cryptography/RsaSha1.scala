package org.coriander.oauth.core.cryptography

import java.security.{Signature, PrivateKey}

class RsaSha1(key : PrivateKey) extends Hmac {
	def create(key : String, message : String) : Array[Byte] = {
		sign(message.getBytes);
	}

	private def sign(message : Array[Byte]) = {
		val sig = Signature.getInstance("SHA1withRSA");
    	sig.initSign(key);
		sig.update(message);
		sig.sign
	}
}