package org.coriander.integration.tests

import org.junit._
import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.matchers.JUnitMatchers._

import org.apache.commons.httpclient._
import org.coriander.oauth.core.{SignatureBaseString, Signature, SignedUri}
import org.apache.commons.httpclient.methods._
import org.coriander.{QueryParser, Query}

import org.coriander.oauth.core._
import http.AuthorizationHeader
import timestamp.SystemTimestampFactory
import nonce.SystemNonceFactory
import org.coriander.unit.tests.TestBase
import uri.OAuthURLEncoder

class TwitmineTest extends TestBase {

    // See: http://twitter.com/oauth_clients/details/42032

    val REQUEST_TOKEN_URI   = new java.net.URI("https://twitter.com/oauth/request_token")
    val ACCESS_TOKEN_URI    = new java.net.URI("https://twitter.com/oauth/access_token")
    val AUTHORIZE_URI       = new java.net.URI("https://twitter.com/oauth/authorize")
    var nonceFactory        = new SystemNonceFactory
    var timestampFactory    = new SystemTimestampFactory
    val urlEncoder          = new OAuthURLEncoder

    val SIGNATURE_METHOD 	= "HMAC-SHA1"
    val VERSION 			= "1.0"
	val VALID_CONSUMER 		= null
    val VALID_REQUEST_TOKEN = null
	val VALID_ACCESS_TOKEN 	= null

	val VALID_AUTHORIZATION_PIN : Int = 0
	
	var consumerCredential = new OAuthCredential("","")
	var token = new OAuthCredential("","")
	var authHeader : AuthorizationHeader = null
    var signedUri : java.net.URI = null
    var result : HttpResponse = HttpResponse.empty
	val TWITTER_REALM = "Twitter API"

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
		when_a_request_token_is_requested

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
			List(new org.apache.commons.httpclient.NameValuePair("oauth_verifier", VALID_AUTHORIZATION_PIN toString))
		)

		println(result.responseText)
		
		assertThat(
            "Should be able to authorize using header.",
            result.status, is(equalTo(HttpStatus.SC_OK))
        )
	}

	@Test @Ignore("Integration test")
	def given_a_valid_screen_name_then_i_can_read_my_own_status_without_authenticating {
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
		assertReturnsOk(new java.net.URI("http://twitter.com/account/rate_limit_status.xml"))
	}

	@Test @Ignore("Integration test")
	def given_a_valid_access_token_then_i_can_get_user_time_line {
		assertReturnsOk(new java.net.URI("http://twitter.com/statuses/user_timeline.xml?count=200"))
	}

	@Test @Ignore("Integration test")
	def given_a_valid_access_token_then_i_can_get_user_mentions {
		assertReturnsOk(new java.net.URI("http://twitter.com/statuses/mentions.xml"))
	}

	private def assertReturnsOk(uri : java.net.URI) {
		given_a_valid_consumer_credential
		given_a_valid_access_token
		given_an_authorization_header(uri)

		val result : HttpResponse = get(uri)

		println(result.responseText)

		assertThat(
            "Expected to be able to authenticate against <" + uri.toString + ">.",
            result.status, is(equalTo(HttpStatus.SC_OK))
        )
	}

	private def given_an_authorization_header(uri : java.net.URI) {
		if (null == consumerCredential)
			throw new Exception("Missing consumerCredential.")

		if (null == token)
			throw new Exception("Missing token.")

		this.authHeader = newAuthHeader(uri)
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

	private def when_a_request_token_is_requested {
		get(REQUEST_TOKEN_URI)
	}

    private def when_a_request_token_is_requested_using_uri_signing {
        signedUri = sign(REQUEST_TOKEN_URI)

        get(signedUri)
    }	

	private def sign(uri : java.net.URI) : java.net.URI = {
		new SignedUri(
            uri,
            consumerCredential,
            token,
            SIGNATURE_METHOD,
            timestampFactory.createTimestamp,
            nonceFactory.createNonce,
            VERSION
        ).value
	}

	private def get(uri : java.net.URI) : HttpResponse = {
		execute(newGet(uri))
	}

	private def post(uri : java.net.URI, params : List[NameValuePair]) : HttpResponse = {
		var post = newPost(uri)
		
		params.foreach(parameter => post addParameter(parameter))

		execute(post)
	}

	private def execute(method : HttpMethodBase) : HttpResponse = {
		val status = new HttpClient() executeMethod(method)

		if (status != HttpStatus.SC_OK)
			return new HttpResponse(status, method.getStatusText)

		new HttpResponse(status, method.getResponseBodyAsString)
	}

	private def newGet(uri : java.net.URI) : HttpMethodBase = authenticate(new GetMethod(uri toString))

	private def newPost(uri : java.net.URI) : PostMethod = {
		var post = new PostMethod(uri toString)

		authenticate(post)

		post
	}

	private def authenticate(method : HttpMethodBase) : HttpMethodBase = {
        if (authHeader != null) {
			method.addRequestHeader(authHeader.name, authHeader.value)
		}

		method
	}

	private def newAuthHeader(uri : java.net.URI) : AuthorizationHeader = {
		val timestamp = timestampFactory.createTimestamp
		val nonce = nonceFactory.createNonce
		val query = new QueryParser().parse(uri)

		val baseString = new SignatureBaseString(uri, query, consumerCredential, token, nonce, timestamp)

		val signature = new Signature(urlEncoder, consumerCredential, token).sign(baseString)

		new AuthorizationHeader(
			TWITTER_REALM,
			consumerCredential.key,
			token.key,
			SIGNATURE_METHOD,
			signature,
			timestamp,
			nonce,
			VERSION,
			urlEncoder
		)
	}
}

class HttpResponse(val status : Int, val responseText : String)
object HttpResponse {
	def empty = new HttpResponse(-1, "This response has not been initialized")
}