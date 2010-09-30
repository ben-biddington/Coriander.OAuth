package org.coriander.unit.tests.oauth.core.cryptography

import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import java.security._
import interfaces.RSAPrivateKey
import spec.PKCS8EncodedKeySpec
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

		certificate.checkValidity
	}

	@Test
	def how_to_load_private_key_from_memory_and_then_sign_with_it {
		val bytes = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALRiMLAh9iimur8V" +
		"A7qVvdqxevEuUkW4K+2KdMXmnQbG9Aa7k7eBjK1S+0LYmVjPKlJGNXHDGuy5Fw/d" +
		"7rjVJ0BLB+ubPK8iA/Tw3hLQgXMRRGRXXCn8ikfuQfjUS1uZSatdLB81mydBETlJ" +
		"hI6GH4twrbDJCR2Bwy/XWXgqgGRzAgMBAAECgYBYWVtleUzavkbrPjy0T5FMou8H" +
		"X9u2AC2ry8vD/l7cqedtwMPp9k7TubgNFo+NGvKsl2ynyprOZR1xjQ7WgrgVB+mm" +
		"uScOM/5HVceFuGRDhYTCObE+y1kxRloNYXnx3ei1zbeYLPCHdhxRYW7T0qcynNmw" +
		"rn05/KO2RLjgQNalsQJBANeA3Q4Nugqy4QBUCEC09SqylT2K9FrrItqL2QKc9v0Z" +
		"zO2uwllCbg0dwpVuYPYXYvikNHHg+aCWF+VXsb9rpPsCQQDWR9TT4ORdzoj+Nccn" +
		"qkMsDmzt0EfNaAOwHOmVJ2RVBspPcxt5iN4HI7HNeG6U5YsFBb+/GZbgfBT3kpNG" +
		"WPTpAkBI+gFhjfJvRw38n3g/+UeAkwMI2TJQS4n8+hid0uus3/zOjDySH3XHCUno" +
		"cn1xOJAyZODBo47E+67R4jV1/gzbAkEAklJaspRPXP877NssM5nAZMU0/O/NGCZ+" +
		"3jPgDUno6WbJn5cqm8MqWhW1xGkImgRk+fkDBquiq4gPiT898jusgQJAd5Zrr6Q8" +
		"AO/0isr/3aa6O6NLQxISLKcPDk2NOccAfS/xOtfOz4sJYM3+Bs4Io9+dZGSDCA54" +
		"Lw03eHTNQghS0A=="

		val message = "GET&http%3A%2F%2Fxxx&oauth_consumer_key%3Dkey%26oauth_nonce%3Df5e81fc5ef498a40f57d9131ea304d2e%26oauth_signature_method%3DRSA-SHA1%26oauth_timestamp%3D1285817487%26oauth_version%3D1.0"
		val expected = "fUl84ba3SC0IeCmuXzNP80BmRvppNXCH4wO7C4yiNXIGgXhl+skHvDT92Keu2iyiQvcZ474m2wadL5dnXQAhB13wHlUo888lOMVeL3PBMHIwmRhMjFi/8wZaH8kqQPmT5cGOF3rpWtFBKMVyIhz5qyuCd+xgPQCbv6wLsDRJeq0="
		
		val keyFactory : KeyFactory = KeyFactory.getInstance("RSA");
		val privSpec : PKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(decodeBase64(bytes))
        val privKey : RSAPrivateKey = keyFactory.generatePrivate(privSpec).asInstanceOf[RSAPrivateKey]
		
		val sig = signCore(message.getBytes, privKey, "SHA1withRSA");

		assertThat(new String(encodeBase64(sig)), is(equalTo(expected)))
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