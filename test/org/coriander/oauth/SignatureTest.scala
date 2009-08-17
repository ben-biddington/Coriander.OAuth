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
    val aValidSignature : Signature = new Signature("tokenKey", "tokenSecret")

    val consumerKey = "consumerKey"
    val consumerSecret = "consumerSecret"

    @Rule
    def exception : ExpectedException = ExpectedException.none()

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerKey_is_null_then_sign_throws_exception {
        val url = new java.net.URI("http://xxx/")
        val params : Map[String, String] = Map("x_name" -> "x_value")

        val signature = new Signature(null, consumerSecret)

        signature.sign(url, params);
    }

    @Test @Ignore
    def exception_rule_works {
        // Does not work yet
        exception.expect(classOf[IllegalArgumentException]);
        exception.expectMessage("xxx");

        println(exception.toString)

        throw new IllegalArgumentException("xxx")
    }

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerSecret_is_null_then_sign_throws_exception {
        val url = new java.net.URI("http://xxx/")
        val params : Map[String, String] = Map("x_name" -> "x_value")

        val signature = new Signature(consumerKey, null)

        signature.sign(url, params);
    }
}
