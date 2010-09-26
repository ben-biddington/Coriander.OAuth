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
		val kpg = KeyPairGenerator.getInstance("RSA");
    	kpg.initialize(512); // 512 is the keysize.
    	val kp = kpg.generateKeyPair();
    	val pubk = kp.getPublic();
    	val prvk = kp.getPrivate();

    	val datafile = "C:/Users/Ben/Documents/Sauce/Coriander.OAuth/README";
    	val sigbytes = sign(datafile, prvk, "SHA1withRSA");

		println("Signature(in hex):: " + sigbytes)
	}

	private def sign(
		datafile: String,
		prvKey : PrivateKey ,
     	sigAlg : String
	) : Array[Byte] = {
    	val sig : Signature = Signature.getInstance(sigAlg);
    	sig.initSign(prvKey);

		var fis = new FileInputStream(datafile);
    	val dataBytes = new Array[Byte](1024)
    	var nread = fis.read(dataBytes);

		while (nread > 0) {
    		sig.update(dataBytes, 0, nread);
      		nread = fis.read(dataBytes);
    	}

		fis.close

    	sig.sign()
  }
}