package org.coriander.unit.tests.oauth.core.cryptography

import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import java.security._
import interfaces.RSAPrivateKey
import spec.PKCS8EncodedKeySpec
import org.apache.commons.codec.binary.Base64._
import org.junit.Test
import org.coriander.oauth.core.cryptography.RsaSha1

class RsaSha1Test  {
	@Test
	def it_produces_correct_signature {
		val privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALRiMLAh9iimur8V" +
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

		val message =
			"GET&http%3A%2F%2Fxxx&oauth_consumer_key%3Dkey%26oauth_nonce%3Df5e81fc5" +
			"ef498a40f57d9131ea304d2e%26oauth_signature_method%3DRSA-SHA1" +
			"%26oauth_timestamp%3D1285817487%26oauth_version%3D1.0"

		val expected =
			"fUl84ba3SC0IeCmuXzNP80BmRvppNXCH4wO7C4yiNXIGgXhl+skHvDT92Keu2iyiQvcZ47" +
			"4m2wadL5dnXQAhB13wHlUo888lOMVeL3PBMHIwmRhMjFi/8wZaH8kqQPmT5cGOF3rpWtFB" +
			"KMVyIhz5qyuCd+xgPQCbv6wLsDRJeq0="

		val keyFactory : KeyFactory = KeyFactory.getInstance("RSA");
		val privSpec : PKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(decodeBase64(privateKey))
        val privKey : RSAPrivateKey = keyFactory.generatePrivate(privSpec).asInstanceOf[RSAPrivateKey]

		val rsaSha1 : RsaSha1 = new RsaSha1(privKey)
		val signature = rsaSha1.create(null, message)

		assertThat(new String(encodeBase64(signature)), is(equalTo(expected)))
	}
}