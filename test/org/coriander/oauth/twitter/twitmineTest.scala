package org.coriander.oauth.twitter.integration.tests

import org.junit._
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.matchers.JUnitMatchers._

import java.io._;

import org.apache.commons.httpclient._;
import org.apache.commons.httpclient.methods._;

import org.coriander.oauth.tests._

import org.coriander.oauth.nonce._
import org.coriander.oauth.timestamp._
import org.coriander.oauth.uri._
import org.coriander.oauth.http._

class twitmineTest extends TestBase {

    // See: http://twitter.com/oauth_clients/details/42032

    val REQUEST_TOKEN_URI   = new java.net.URI("http://twitter.com/oauth/request_token")
    val ACCESS_TOKEN_URI    = new java.net.URI("http://twitter.com/oauth/access_token")
    val AUTHORIZE_URI       = new java.net.URI("http://twitter.com/oauth/authorize")
    var nonceFactory        = new SystemNonceFactory
    var timestampFactory    = new SystemTimestampFactory
    val urlEncoder          = new OAuthURLEncoder

    val signatureMethod = "HMAC-SHA1"
    val version = "1.0"
    var consumerCredential = new OAuthCredential("","")
    var signedUri : java.net.URI = null
    var httpStatus : Int = 0

    @Test
    def given_an_invalid_consumer_credential_then_twitmine_get_request_token_returns_unauthorized() {
        given_an_invalid_credential

        when_a_request_token_is_requested

        assertThat(
            "Supplying invalid credential should result in 401 status returned.",
            httpStatus, is(equalTo(HttpStatus.SC_UNAUTHORIZED))
        )
    }

    @Test @Ignore("Credential issues")
    def given_a_valid_consumer_credential_then_twitmine_get_request_token_returns_ok() {
        given_a_valid_credential

        when_a_request_token_is_requested

        println(signedUri.toString)

        assertThat(
            "Supplying valid credential should result in 200 status returned.",
            httpStatus, is(equalTo(HttpStatus.SC_OK))
        )
    }

    private def given_a_valid_credential {
        consumerCredential = new OAuthCredential(
            "invalid-on-purpose",
            "also-invalid-on-purpose"
        )
    }
    
    private def given_an_invalid_credential {
        consumerCredential = new OAuthCredential("key", "secret")
    }

    private def when_a_request_token_is_requested {
        signedUri = new SignedUri(
            REQUEST_TOKEN_URI,
            consumerCredential,
            null,
            signatureMethod,
            timestampFactory.createTimestamp,
            nonceFactory.createNonce,
            version
        ).value

        execute(signedUri)
    }

    private def execute(uri : java.net.URI) {
        httpStatus = new HttpClient().
            executeMethod(new GetMethod(uri toString))
    }
}