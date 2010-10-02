package org.coriander.oauth.core.signing

import java.security.PrivateKey
import org.apache.commons.codec.binary.Base64._

class RsaSha1Signature(key : PrivateKey) extends org.coriander.oauth.core.signing.Signature {
	def sign(baseString : String) = {
		new String(encodeBase64(signCore(baseString.getBytes)))
	}

	private def signCore(message : Array[Byte]) = {
		val sig = java.security.Signature.getInstance("SHA1withRSA");
    	sig.initSign(key);
		sig.update(message);
		sig.sign
	}
}