package org.coriander.oauth.core.cryptography.keys

import java.io.{IOException, File}
import java.security.interfaces.RSAPrivateKey
import java.security.{KeyFactory, PrivateKey}
import java.security.spec.PKCS8EncodedKeySpec
import scala.io.Source._
import org.apache.commons.codec.binary.Base64._

object RsaPrivateKeyReader {
	def read(file : String): PrivateKey = {
		val theFile = new File(file)

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
}