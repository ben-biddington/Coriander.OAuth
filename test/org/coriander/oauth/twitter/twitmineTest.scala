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
	val VALID_CONSUMER = null
    val VALID_REQUEST_TOKEN = null
	val VALID_ACCESS_TOKEN = null
	val VALID_PIN : Int = 0
	
	var consumerCredential = new OAuthCredential("","")
	var token = new OAuthCredential("","")
	var authHeader : AuthorizationHeader = null
    var signedUri : java.net.URI = null
    var result : HttpResponse = HttpResponse.empty
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

	@Test @Ignore("Integration test")
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

	@Test @Ignore("Integration test")
	def given_a_valid_request_token_then_i_can_authorize_it_via_browser {
		// UNTESTABLE: requires user interaction -- actually we could fake a form post
		// Redirect user to: https://twitter.com/oauth/authorize?oauth_token={request_token_key}
		// If they press okay they're given a PIN which is used in the next step
	}

	@Test @Ignore("Request tokens expire after a period.")
	def given_an_authorized_request_token_then_i_can_exchange_it_for_an_access_token {
		given_a_valid_consumer_credential
		given_an_authorized_valid_request_token
		given_an_authorization_header(ACCESS_TOKEN_URI)

		result = post(
			ACCESS_TOKEN_URI,
			List(new Header("Authorization", authHeader.value)),
			List(new org.apache.commons.httpclient.NameValuePair("oauth_verifier", VALID_PIN toString))
		)

		println(result.responseText)
		
		assertThat(
            "Should be able to authorize using header.",
            result.status, is(equalTo(HttpStatus.SC_OK))
        )
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
	def given_a_valid_access_token_then_i_can_read_rate_limit_status {
		val uri = new java.net.URI("http://twitter.com/account/rate_limit_status.xml")

		given_a_valid_consumer_credential
		given_a_valid_access_token
		given_an_authorization_header(uri)

		val result : HttpResponse = get(uri, authHeader)

		println(result.responseText)

		assertThat(
            "I think token is required, because when I try this I get 401 with \"basic auth required\".",
            result.status, is(equalTo(HttpStatus.SC_OK))
        )
	}

	@Test @Ignore("Integration test")
	def given_a_valid_access_token_then_i_can_get_user_timeline {
		val uri = new java.net.URI("http://twitter.com/statuses/user_timeline.xml?count=200")

		given_a_valid_consumer_credential
		given_a_valid_access_token
		given_an_authorization_header(uri)

		val result : HttpResponse = get(uri, authHeader)

		println(result.responseText)

		assertThat(
            "I think token is required, because when I try this I get 401 with \"basic auth required\".",
            result.status, is(equalTo(HttpStatus.SC_OK))
        )
	}

	@Test @Ignore
	def given_a_valid_access_token_then_i_can_get_user_mentions {
		val uri = new java.net.URI("http://twitter.com/statuses/mentions.format.xml")

		given_a_valid_consumer_credential
		given_a_valid_access_token
		given_an_authorization_header(uri)

		val result : HttpResponse = get(uri, authHeader)

		println(result.responseText)

		assertThat(
            "I think token is required, because when I try this I get 401 with \"basic auth required\".",
            result.status, is(equalTo(HttpStatus.SC_OK))
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
        consumerCredential = VALID_CONSUMER
    }

	private def given_an_authorized_valid_request_token {
        token = VALID_REQUEST_TOKEN
    }

	private def given_a_valid_access_token {
		token = VALID_ACCESS_TOKEN	
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

	private def post(uri : java.net.URI, headers : List[Header], params : List[NameValuePair]) : HttpResponse = {
		var postMethod = new PostMethod(uri toString)

		println(uri toString)

		headers.foreach(header => {
			postMethod.addRequestHeader(header)
			println(header toString)
		})

		params.foreach(parameter => {
			postMethod.addParameter(parameter)
			println(parameter toString)
		})

		val status = new HttpClient() executeMethod(postMethod)

		if (status != HttpStatus.SC_OK)
			return new HttpResponse(status, postMethod.getStatusText)

		return new HttpResponse(status, postMethod.getResponseBodyAsString)
	}
}

class HttpResponse(val status : Int, val responseText : String)
object HttpResponse {
	def empty = new HttpResponse(-1, "This response has not been initialized")
}