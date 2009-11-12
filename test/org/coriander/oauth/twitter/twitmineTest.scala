package org.coriander.oauth.twitter.integration.tests

import org.junit._
import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.matchers.JUnitMatchers._

import org.apache.commons.httpclient._
import org.coriander.oauth.{SignatureBaseString, Signature, SignedUri, OAuthCredential}

import org.apache.commons.httpclient.methods._
import org.coriander.{QueryParser, Query}


import org.coriander.oauth.tests._

import org.coriander.oauth.nonce._
import org.coriander.oauth.timestamp._
import org.coriander.oauth.uri._
import org.coriander.oauth.http._

class twitmineTest extends TestBase {

    // See: http://twitter.com/oauth_clients/details/42032

    val REQUEST_TOKEN_URI   = new java.net.URI("https://twitter.com/oauth/request_token")
    val ACCESS_TOKEN_URI    = new java.net.URI("https://twitter.com/oauth/access_token")
    val AUTHORIZE_URI       = new java.net.URI("https://twitter.com/oauth/authorize")
    var nonceFactory        = new SystemNonceFactory
    var timestampFactory    = new SystemTimestampFactory
    val urlEncoder          = new OAuthURLEncoder

    val signatureMethod = "HMAC-SHA1"
    val version = "1.0"
    var consumerCredential = new OAuthCredential("","")
	var token = new OAuthCredential("","")
	var authHeader : AuthorizationHeader = null
    var signedUri : java.net.URI = null
    var result : HttpResponse = null
	val twitterRealm = "Twitter API"

    @Test @Ignore("Integration test")
    def given_an_invalid_consumer_credential_then_twitmine_get_request_token_returns_unauthorized() {
        given_an_invalid_credential

        when_a_request_token_is_requested_using_uri_signing

        assertThat(
            "Supplying invalid credential should result in 401 status returned.",
            result.status, is(equalTo(HttpStatus.SC_UNAUTHORIZED))
        )
    }

    @Test @Ignore("Integration test")
    def i_can_obtain_a_request_token_using_uri_signing() {
        given_a_valid_consumer_credential

        when_a_request_token_is_requested_using_uri_signing

        assertThat(
            "Supplying valid credential should result in 200 status returned.",
            result.status, is(equalTo(HttpStatus.SC_OK))
        )

		println("Request token: " + result.responseText)
    }

	@Test @Ignore("In progress")
    def i_can_obtain_a_request_token_using_auth_header() {
        given_a_valid_consumer_credential
		given_an_authorization_header(REQUEST_TOKEN_URI)
		when_a_request_token_is_requested_using_header_authentication

		println(result.responseText)

        assertThat(
            "Should be able to authorize using header.",
            result.status, is(equalTo(HttpStatus.SC_OK))
        )
    }

	@Test @Ignore("In progress")
	def given_a_valid_request_token_then_i_can_convert_it_to_access_token {
		// Redirect user to: http://twitter.com/oauth/authorize?oauth_token={request_token_key}
		// If they press okay they're given a PIN
	}

	@Test @Ignore("Integration test")
	def i_can_read_my_own_status_without_authenticating {
		given_a_valid_consumer_credential
		val uri = new java.net.URI("http://twitter.com/users/show.xml?screen_name=benbiddington")

		val result : HttpResponse = get(uri)

		assertThat(
            "Should be able to read users.",
            result.status, is(equalTo(HttpStatus.SC_OK))
        )
	}

	@Test @Ignore("Integration test")
	def i_can_not_read_my_own_first_ten_mentions_with_consumer_signature {
		given_a_valid_consumer_credential

		val uri = sign(new java.net.URI("http://twitter.com/statuses/mentions.xml?count=10"))

		val result : HttpResponse = get(uri)

		assertThat(
            "I think token is required, because when I try this I get 401 with \"basic auth required\".",
            result.status, is(equalTo(HttpStatus.SC_UNAUTHORIZED))
        )
	}

	private def given_an_authorization_header(uri : java.net.URI) {
		if (null == consumerCredential)
			throw new Exception("Missing consumerCredential.")

		if (null == token)
			throw new Exception("Missing token.")

		val timestamp = timestampFactory.createTimestamp
		val nonce = nonceFactory.createNonce
		val query = new QueryParser().parse(uri).filter(nvp => false == nvp.value.startsWith("oauth_"))

		val baseString = new SignatureBaseString(uri, query, consumerCredential, token, nonce, timestamp)
		
		val signature = new Signature(urlEncoder, consumerCredential, token).
				sign(baseString)

		this.authHeader = new AuthorizationHeader(
			twitterRealm,
			consumerCredential.key,
			token.key,
			signatureMethod,
			signature,
			timestamp,
			nonce,
			version,
			urlEncoder
		)
	}

    private def given_a_valid_consumer_credential {
        consumerCredential = new OAuthCredential("", "")
    }

	private def given_a_valid_request_token() {
        this.token = new OAuthCredential("", "")
    }
    
    private def given_an_invalid_credential {
        consumerCredential = new OAuthCredential("key", "secret")
    }

    private def when_a_request_token_is_requested_using_uri_signing {
        signedUri = sign(REQUEST_TOKEN_URI)

        execute(signedUri)
    }

	private def when_a_request_token_is_requested_using_header_authentication {
        result = get(REQUEST_TOKEN_URI, authHeader)
    }	

	private def sign(uri : java.net.URI) : java.net.URI = {
		new SignedUri(
            uri,
            consumerCredential,
            token,
            signatureMethod,
            timestampFactory.createTimestamp,
            nonceFactory.createNonce,
            version
        ).value
	}
	
	private def execute(uri : java.net.URI) {
        result = get(uri)
    }

	private def get(uri : java.net.URI, authHeader : AuthorizationHeader) : HttpResponse =
		get(uri, List(new Header("Authorization", authHeader.value)))

	private def get(uri : java.net.URI) : HttpResponse = get(uri, List())

	private def get(uri : java.net.URI, headers : List[Header]) : HttpResponse = {
		var get = new GetMethod(uri toString)

		headers.foreach(header => {
			get.addRequestHeader(header)
			println(header toString)
		})

		val status = new HttpClient() executeMethod(get)

		if (status != HttpStatus.SC_OK)
			return new HttpResponse(status, get.getStatusText)

		return new HttpResponse(status, get.getResponseBodyAsString)
	}
}

class HttpResponse(val status : Int, val responseText : String)