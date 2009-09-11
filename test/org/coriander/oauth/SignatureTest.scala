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
// Online SHA-1 generator, see: http://hash.online-convert.com/sha1-generator
class SignatureTest extends TestBase {

    @Rule
    def exception : ExpectedException = ExpectedException.none()

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerCredential_is_null_then_sign_throws_exception {
        val credentialWithNullSecret = new OAuthCredential("key", null)

        val signature = new Signature(null)

        signature sign("anything");
    }

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerSecret_is_null_then_sign_throws_exception {
        val credentialWithNullSecret = new OAuthCredential("key", null)

        val signature = new Signature(credentialWithNullSecret)

        signature sign("anything");
    }

    @Test 
    def given_an_example_base_string_then_the_correct_signature_is_returned {
       var signatureBaseString = """
            GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26
            oauth_nonce%3Df3df23228e40e2905e305a893895f115%26o
            auth_signature_method%3DHMAC-SHA1%26
            oauth_timestamp%3D1252657173%26oauth_version%3D1.0"""

       signatureBaseString = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26oauth_nonce%3Df3df23228e40e2905e305a893895f115%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1252657173%26oauth_version%3D1.0"

       val credential = new OAuthCredential("key", "secret")
       val expected : String = "2/MMtvuImh4H+clAdThQWk916lo="
       val actual = new Signature(credential) sign(signatureBaseString);

        assertEquals(expected, actual)
    }

    // TEST: Signature uses empty string for token secret when token is supplied
    // TEST: Token is an optional parameter
}
