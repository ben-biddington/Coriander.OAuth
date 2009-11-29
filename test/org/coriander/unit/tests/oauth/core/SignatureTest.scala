package org.coriander.unit.tests.oauth.core


import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit._
import org.apache.commons.httpclient._
import org.apache.commons.httpclient.util._
import org.junit.rules._
import scala.collection.immutable._
import org.coriander.oauth._
import core.uri.OAuthURLEncoder
import core.{OAuthCredentialSet, Signature, OAuthCredential}
import core.OAuthCredentialSet._
import org.coriander.unit.tests.TestBase

// See: http://www.infoq.com/news/2009/07/junit-4.7-rules#
// For signature examples, see: http://term.ie/oauth/example/client.php
class SignatureTest extends TestBase {

    val validConsumerCredential = new OAuthCredential("key", "secret")
    val validToken = new OAuthCredential("token_key", "token_secret")
    val urlEncoder = new OAuthURLEncoder

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerCredential_is_null_then_sign_throws_exception {
        newSignature(null) sign("anything");
    }

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerSecret_is_null_then_sign_throws_exception {
        newSignature(new OAuthCredential("key", null)) sign("anything")
    }

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_token_is_supplied_with_null_secret_then_sign_throws_exception {
        val tokenWithNullSecret = new OAuthCredential("key", null)

        newSignature(validConsumerCredential, tokenWithNullSecret) sign("anything");
    }

    @Test 
    def when_just_consumer_secret_supplied_then_sign_returns_correct_signature {
        val baseString = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26" +
			"oauth_nonce%3Df3df23228e40e2905e305a893895f115%26oauth_signature_method%3DHMAC-SHA1%26" +
			"oauth_timestamp%3D1252657173%26oauth_version%3D1.0"

        val expected : String = "2/MMtvuImh4H+clAdThQWk916lo="
        val actual = newSignature(validConsumerCredential) sign(baseString)

        assertEquals(expected, actual)
    }

    @Test
    def when_consumer_secret_and_token_secret_supplied_then_sign_returns_correct_signature {
        val baseString = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26" +
			"oauth_nonce%3D35d48708b951a3adfaa64a9d0632e19a%26oauth_signature_method%3DHMAC-SHA1%26" +
			"oauth_timestamp%3D1252669302%26oauth_token%3Dtoken_key%26oauth_version%3D1.0"

        val expected : String = "a6D1JJVdKIxKe7L/AW+gtSzBT24="

        val actual = newSignature(validConsumerCredential, validToken) sign(baseString);

        assertEquals(expected, actual)
    }

    @Test
    def when_consumer_secret_contains_uri_reserved_characters_then_sign_returns_correct_signature_having_escaped_them {
        val baseString = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26" +
			"oauth_nonce%3D26db6028882d344cccad2227f4a9dae8%26oauth_signature_method%3DHMAC-SHA1%26" +
			"oauth_timestamp%3D1252670619%26oauth_version%3D1.0"

        val expected : String = "DD+dh4ZaBlgf4WrUBfFoah/gfZg="
        
        val actual = newSignature(new OAuthCredential("key", "secret with spaces")) sign(baseString);

        assertEquals(expected, actual)
    }

    @Test
    def when_token_secret_contains_uri_reserved_characters_then_sign_returns_correct_signature_having_escaped_them {
        val baseString = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26" +
			"oauth_nonce%3D69ed8b3bd8cab8a40d4069f422de9854%26oauth_signature_method%3DHMAC-SHA1%26" +
			"oauth_timestamp%3D1252671920%26oauth_token%3Dtoken_key%26oauth_version%3D1.0"

        val token = new OAuthCredential("token_key", "token secret with spaces")
        val expected : String = "aNXBLy2UtMF99dgrJa+9PSWYYUI="

        val credentials = OAuthCredentialSet(
            forConsumer(validConsumerCredential),
            andToken(token)
        )

        val actual = new Signature(urlEncoder, credentials) sign(baseString);

        assertEquals(expected, actual)
    }

    @Test
    def sign_currently_only_supports_hmac_sha1_algorithm() {
        val expectedMessage = "Unsupported algorithm. Currently only 'HMacSha1' is supported."
        var success = false

        try {
            new Signature(null, null, "Anything but hmacsha1") sign("Any string");
        } catch {
            case e : Exception => {
                assertEquals(expectedMessage, e.getMessage)
                success = true
            }
        }

        assertTrue("The expected exception was not thrown", success)
    }

	@Test
	def example {
		val baseString = "GET&http%3A%2F%2Fxxx&" +
			"oauth_consumer_key%3Dkey%26oauth_nonce%3D1108721620a4c6093f92b24d5844e61b%26" +
			"oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1259051683%26" +
			"oauth_token%3DHZvFeX5T7XlRIcJme%252FEWTg%253D%253D%26oauth_version%3D1.0"

		val consumer = new OAuthCredential("key", "secret")
		val token = new OAuthCredential("HZvFeX5T7XlRIcJme/EWTg==", "Ao61gCJXIM20aqLDw7+Cow==")
        val credentials = OAuthCredentialSet(consumer, token)
        
		val expected = "ZZ3oRaIMA4dg4HrS63qokpKwGbY="
		val actual = new Signature(credentials) sign(baseString)

		assertThat(actual, is(equalTo(expected)))
	}

    def newSignature(consumerCredential : OAuthCredential) : Signature = {
        newSignature(consumerCredential, null)
    }

    def newSignature(consumer  : OAuthCredential, token : OAuthCredential) : Signature = {
        val credentials = OAuthCredentialSet(
            forConsumer(consumer),
            andToken(token)
        )
        new Signature(urlEncoder, credentials)
    }
}