package org.coriander.unit.tests.oauth.core.cryptography

import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import java.security._
import org.bouncycastle.openssl.PEMReader
import org.bouncycastle.jce.provider.{X509CertificateObject, BouncyCastleProvider}
import spec.{PKCS8EncodedKeySpec, KeySpec, X509EncodedKeySpec}
import java.io.{IOException, ByteArrayInputStream, FileReader, BufferedReader}

class RsaSha1Test  {
	@Test
	def an_example { // http://www.informit.com/articles/article.aspx?p=170967&seqNum=7
		val kp = newKeyPair

    	val privateKey	= kp.getPrivate

    	val sigbytes = signCore(new Array[Byte](1), privateKey, "SHA1withRSA");

		println("Signature(in hex):: " + sigbytes)
	}

	@Test
	def how_to_load_x509_certificate_with_bouncy_castle {
		val path =  "test/cert.pem"
		val br = new BufferedReader(new FileReader(path))
		Security.addProvider(new BouncyCastleProvider())

		val certificate : X509CertificateObject = new PEMReader(br).readObject().asInstanceOf[X509CertificateObject]
	    assertTrue("Certificate load failed", certificate != null)
		assertThat(certificate.getPublicKey.getAlgorithm, is(equalTo("RSA")))
		assertThat(certificate.getSignature.size, is(equalTo(128)))
	}

	@Test
	def how_to_load_key_pair_from_pem_files_with_bouncy_castle {
		val path =  "test/rsa_cert.pem"
		val br = new BufferedReader(new FileReader(path))
		Security.addProvider(new BouncyCastleProvider())
		val kp : KeyPair = new PEMReader(br).readObject().asInstanceOf[KeyPair]

		assertThat(kp.getPrivate.getAlgorithm, is(equalTo("RSA")))
		assertThat(kp.getPublic.getAlgorithm, is(equalTo("RSA")))		
	}

	@Test // See: http://download.oracle.com/javase/1.4.2/docs/api/java/security/Signature.html
	def how_to_sign_something_with_private_key_from_pem_file {
		val path =  "test/rsa_cert.pem"
		val br = new BufferedReader(new FileReader(path))
		Security.addProvider(new BouncyCastleProvider())
		val kp : KeyPair = new PEMReader(br).readObject().asInstanceOf[KeyPair]

		val message = "any message".getBytes

		val privateKey = kp.getPrivate
		val sig : Signature = Signature.getInstance("SHA1withRSA")
		sig.initSign(privateKey)
		sig.update(message)

		val signature = sig.sign;
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