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

// See: http://www.infoq.com/news/2009/07/junit-4.7-rules
class SignatureTest extends TestBase {
    val aValidUri =  new java.net.URI("http://xxx/")
    val consumerCredential : OAuthCredential = new OAuthCredential("key", "secret")
    val aValidSignature : Signature = new Signature(consumerCredential)

    @Rule
    def exception : ExpectedException = ExpectedException.none()

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerSecret_is_null_then_sign_throws_exception {
        val url = new java.net.URI("http://xxx/")
        val params : Map[String, String] = Map("x_name" -> "x_value")

        val credentialWithNullSecret = new OAuthCredential("key", null)

        val signature = new Signature(credentialWithNullSecret)

        signature sign(new SignatureBaseString(url, params, consumerCredential, 0, ""));
    }

    // TEST: Given an example baseString, then the correct signature is returned
}
