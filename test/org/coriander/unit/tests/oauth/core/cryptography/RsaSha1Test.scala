package org.coriander.unit.tests.oauth.core.cryptography

import org.junit._
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import java.io.FileInputStream
import java.security._
import com.sun.xml.internal.bind.Util


class RsaSha1Test  {
	@Test
	def an_example { // http://www.informit.com/articles/article.aspx?p=170967&seqNum=7
		val kp = newKeyPair

    	val privateKey	= kp.getPrivate

    	val sigbytes = signCore(new Array[Byte](1), privateKey, "SHA1withRSA");

		println("Signature(in hex):: " + sigbytes)
	}

	private def signCore(
		message	: Array[Byte],
		prvKey 	: PrivateKey,
     	sigAlg 	: String
	) = {
		val sig = Signature.getInstance(sigAlg);
    	sig.initSign(prvKey);
		sig.update(message, 0, message.size);
		sig.sign()
	}

	private def newKeyPair = {
		val kpg = KeyPairGenerator.getInstance("RSA");
		val KEYSIZE : Int = 512
		kpg.initialize(KEYSIZE);

		kpg.generateKeyPair()
	}
}