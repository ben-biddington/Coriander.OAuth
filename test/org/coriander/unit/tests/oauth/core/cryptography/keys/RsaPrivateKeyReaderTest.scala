package org.coriander.unit.tests.oauth.core.cryptography.keys

import org.junit.{Ignore, Test}
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._

import java.security._
import interfaces.RSAPrivateKey
import java.io.{File, IOException}
import scala.io.Source._
import spec.PKCS8EncodedKeySpec
import org.apache.commons.codec.binary.Base64._

class RsaPrivateKeyReaderTest {
	@Test {val expected = classOf[IOException]}
	def it_fails_when_file_not_found {
		RsaPrivateKeyReader.read("xxx_bung_file")
	}

	@Test {val expected = classOf[IOException]}
	def it_fails_when_file_is_not_in_dsa_format{
		RsaPrivateKeyReader.read("test/rsa_cert.pem")				
	}

	@Test
	def it_works {
		val expected = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALRiMLAh9iimur8V" +
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

		val actual : PrivateKey = RsaPrivateKeyReader.read("test/example_der.key");
		val actualAsString = new String(encodeBase64(actual.getEncoded))

		assertThat(actualAsString, is(equalTo(expected)))
	}

	@Test @Ignore
	def large_files_are_rejected { }
}

object RsaPrivateKeyReader {
	def read(file : String): PrivateKey = {
		val theFile = new File(file)

		requireExists(theFile)

		val allLines : List[String] = fromFile(theFile.getCanonicalPath).getLines.toList
		val START_TOKEN = "-----BEGIN PRIVATE KEY-----"
		val END_TOKEN 	= "-----END PRIVATE KEY-----"

		val isDsaFormat = allLines.count((line : String) => line.trim.matches(START_TOKEN)) == 1

		if (false == isDsaFormat)
			throw new IOException("The supplied file does not appear to be in DSA format.")

		val justTheKey = linesBetween(file, START_TOKEN, END_TOKEN)

		toPrivateKey(justTheKey.mkString)
	}

	private def linesBetween(file : String, start : String, end : String) = {
		val allLines : List[String] = fromFile(new File(file).getCanonicalPath).getLines.toList
		
		var done = false
		var started = false

	 	val lines = allLines.takeWhile((line : String) => {
			if (false == started) {
				started = line.trim.matches(start)
			}

			done = line.trim.matches(end)

			started && !done
		})

		lines.slice(1, lines.size)
	}

	private def toPrivateKey(encodedKey : String) = {	
		val privSpec = new PKCS8EncodedKeySpec(decodeBase64(encodedKey))
		val keyFactory = KeyFactory.getInstance("RSA");
		keyFactory.generatePrivate(privSpec).asInstanceOf[RSAPrivateKey]
	}

	private def requireExists(file : File) {
		if (false == file.exists)
			throw new IOException("File not found. Unable to load key.")
	}
}