package org.coriander.oauth.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers._
import org.hamcrest.CoreMatchers._
import org.junit._
import org.apache.commons.httpclient._
import org.apache.commons.httpclient.util._
import org.junit.rules._
import scala.collection.immutable._
import org.coriander.oauth._

// See: http://www.infoq.com/news/2009/07/junit-4.7-rules#
// For signaure examples, see: http://term.ie/oauth/example/client.php
class SignatureTest extends TestBase {

    val validConsumerCredential = new OAuthCredential("key", "secret")
    val validToken = new OAuthCredential("token_key", "token_secret")
    val urlEncoder = new org.coriander.oauth.uri.OAuthURLEncoder

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerCredential_is_null_then_sign_throws_exception {
        val credentialWithNullSecret = new OAuthCredential("key", null)

        newSignature(null) sign("anything");
    }

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerSecret_is_null_then_sign_throws_exception {
        val credentialWithNullSecret = new OAuthCredential("key", null)

        val signature = new Signature(urlEncoder, credentialWithNullSecret)

        signature sign("anything");
    }

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_token_is_supplied_with_null_secret_then_sign_throws_exception {
        val tokenCredentialWithNullSecret = new OAuthCredential("key", null)

        newSignature(validConsumerCredential, tokenCredentialWithNullSecret) sign("anything");
    }

    @Test 
    def when_just_consumer_secret_supplied_then_sign_returns_correct_signature {
        // [!] Multiline strings cause failure
        val baseString = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26oauth_nonce%3Df3df23228e40e2905e305a893895f115%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1252657173%26oauth_version%3D1.0"

        val expected : String = "2/MMtvuImh4H+clAdThQWk916lo="
        val actual = newSignature(validConsumerCredential) sign(baseString);

        assertEquals(expected, actual)
    }

    @Test
    def when_consumer_secret_and_token_secret_supplied_then_sign_returns_correct_signature {
        // [!] Multiline strings cause failure
        val baseString = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26oauth_nonce%3D35d48708b951a3adfaa64a9d0632e19a%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1252669302%26oauth_token%3Dtoken_key%26oauth_version%3D1.0"

        val expected : String = "a6D1JJVdKIxKe7L/AW+gtSzBT24="

        val actual = newSignature(validConsumerCredential, validToken) sign(baseString);

        assertEquals(expected, actual)
    }

    @Test
    def when_consumer_secret_contains_uri_reserved_characters_then_sign_returns_correct_signature_having_escaped_them {
        // [!] Multiline strings cause failure
        val baseString = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26oauth_nonce%3D26db6028882d344cccad2227f4a9dae8%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1252670619%26oauth_version%3D1.0"

        val credential = new OAuthCredential("key", "secret with spaces")
        val expected : String = "DD+dh4ZaBlgf4WrUBfFoah/gfZg="
        
        val actual = newSignature(credential) sign(baseString);

        assertEquals(expected, actual)
    }

    @Test
    def when_token_secret_contains_uri_reserved_characters_then_sign_returns_correct_signature_having_escaped_them {
        // [!] Multiline strings cause failure
        val baseString = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26oauth_nonce%3D69ed8b3bd8cab8a40d4069f422de9854%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1252671920%26oauth_token%3Dtoken_key%26oauth_version%3D1.0"

        val token = new OAuthCredential("token_key", "token secret with spaces")
        val expected : String = "aNXBLy2UtMF99dgrJa+9PSWYYUI="
        
        val actual = new Signature(urlEncoder, validConsumerCredential, token) sign(baseString);

        assertEquals(expected, actual)
    }

    @Test
    def sign_currently_only_supports_hmacsha1_algorithm() {
        val expectedMessage = "Unsupported algorithm. Currently only 'HMacSha1' is supported."
        var success = false

        try {
            new Signature(null, null, null, "Anything but hmacsha1") sign("Any string");
        } catch {
            case e : Exception => {
                assertEquals(expectedMessage, e.getMessage)
                success = true
            }
        }

        assertTrue("The expected exception was not thrown", success)
    }


    def newSignature(consumerCredential : OAuthCredential) : Signature = {
        newSignature(consumerCredential, null)
    }

    def newSignature(
        consumerCredential : OAuthCredential,
        token : OAuthCredential
    ) : Signature = {
        new Signature(urlEncoder, consumerCredential, token)
    }
}