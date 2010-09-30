package org.coriander.unit.tests.oauth.core.cryptography.keys

import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import java.security._
import interfaces.RSAPrivateKey
import spec.PKCS8EncodedKeySpec
import org.apache.commons.codec.binary.Base64._
import org.coriander.oauth.core.cryptography.RsaSha1
import java.io.{File, IOException}

class RsaPrivateKeyReaderTest {
	@Test {val expected = classOf[IOException]}
	def it_fails_when_file_not_found {
		RsaPrivateKeyReader.read("xxx_bung_file")				
	}
}

object RsaPrivateKeyReader {
	def read(file : String) {
		val theFile = new File(file)

		requireExists(theFile)
	}

	private def requireExists(file : File) {
		if (false == file.exists)
			throw new IOException("File not found. Unable to load key.")
	}
}