package org.coriander.unit.tests.oauth.core.cryptography

import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import java.security._
import org.bouncycastle.openssl.PEMReader
import org.bouncycastle.jce.provider.{X509CertificateObject, BouncyCastleProvider}
import java.io.{FileReader, BufferedReader}
import org.apache.commons.codec.binary.Base64._

class RsaSha1Test  {
	@Test
	def an_example { // http://www.informit.com/articles/article.aspx?p=170967&seqNum=7
		val kp = newKeyPair

    	val privateKey	= kp.getPrivate

    	val sigbytes = signCore(new Array[Byte](1), privateKey, "SHA1withRSA");
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
		val keypair = load(RSA_PEM_FILE)

		val message = "any message".getBytes

		val signature = signCore(message, keypair.getPrivate, "SHA1withRSA")

		val expected : String = "ABUG06UaJebDw3RsaLCrYgC6qnGia2REAAANBTmDa/zo27mNwHvKHACsdT+tDsbsDAZG5c7SVT9SOeXQJ+cZ3Y3Bqrm7tl0YdX1oSXRoh/RWa2jcPivLEzsVC4hYho0oIlkp6SH1pRaa2eIT5GbZnRCVxMh/Evu12u2qLcNGK4M="

		val actual = new String(encodeBase64(signature))

		assertThat(actual, is(equalTo(expected)))
	}

	@Test
	def how_to_verify_a_signature {
		val message = "any message".getBytes
		val signature = "ABUG06UaJebDw3RsaLCrYgC6qnGia2REAAANBTmDa/zo27mNwHvKHACsdT+tDsbsDAZG5c7SVT9SOeXQJ+cZ3Y3Bqrm7tl0YdX1oSXRoh/RWa2jcPivLEzsVC4hYho0oIlkp6SH1pRaa2eIT5GbZnRCVxMh/Evu12u2qLcNGK4M="

		val bytes = decodeBase64(signature)
		
		val keypair = load(RSA_PEM_FILE)

		val sig : Signature = Signature.getInstance("SHA1withRSA")
		sig.initVerify(keypair.getPublic)
		sig.update(message)

		val okay = sig.verify(bytes)

		assertTrue("The signature did not verify", okay)
	}

	private def load(pemFile : String) : KeyPair = {
		var reader : BufferedReader = null

		try {
			reader = new BufferedReader(new FileReader(pemFile))
			Security.addProvider(new BouncyCastleProvider())
			new PEMReader(reader).readObject().asInstanceOf[KeyPair]
		} finally {
			if (reader != null) {
				reader.close
			}
		}
	}

	private def signCore(
		message	: Array[Byte],
		prvKey 	: PrivateKey,
     	sigAlg 	: String
	) = {
		val sig = Signature.getInstance(sigAlg);
    	sig.initSign(prvKey);
		sig.update(message);
		sig.sign
	}

	private def newKeyPair = {
		val kpg = KeyPairGenerator.getInstance("RSA");
		val KEYSIZE : Int = 512
		kpg.initialize(KEYSIZE);

		kpg.generateKeyPair()
	}

	private val RSA_PEM_FILE = "test/rsa_cert.pem"
}