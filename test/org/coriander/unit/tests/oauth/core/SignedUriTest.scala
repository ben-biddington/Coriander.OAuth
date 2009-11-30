package org.coriander.unit.tests.oauth.core


import java.net.URI
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.matchers.JUnitMatchers._
import org.coriander.oauth._
import core.{CredentialSet, Options, SignedUri, Credential}
import org.coriander.{Query, NameValuePair}
import org.coriander.unit.tests.TestBase
import CredentialSet._

class SignedUriTest extends TestBase {

    val consumerCredential = new Credential("key", "secret")
    val anyUri : URI = new URI("http://any-host/default.html")

    var instance : SignedUri = null

    @Before
    def given_instance_starts_as_null() {
        instance = null
    }

    // TEST: The sort order of the parameters does not matter

    @Test
    def value_preserves_uri_scheme() {
        val httpsUri = new URI("https://an-https-uri")
        given_a_signed_uri(httpsUri)

        assertThat(instance.value.toString, containsString(httpsUri.getScheme))
        
        val plainUri = new URI("http://a-plain-http-uri")
        given_a_signed_uri(plainUri)

        assertThat(instance.value.toString, containsString(plainUri.getScheme))
    }

    @Test
    def value_preserves_port_even_transparent_ones {
        given_a_signed_uri(new URI("https://an-https-uri-with-default-port:443"))

        assertThat(
            "Port 443 should not be excluded.",
            instance.value.getPort, is(equalTo(443))
        )

        given_a_signed_uri(new URI("https://an-https-uri-with-any-port:1337"))

        assertThat(
            "Port 1337 should not be excluded.",
            instance.value.getPort, is(equalTo(1337))
        )

        given_a_signed_uri(new URI("http://a-plain-http-uri-with-default-port:80"))

        assertThat(
            "Port 80 should not be excluded.",
            instance.value.getPort, is(equalTo(80))
        )

        given_a_signed_uri(new URI("http://a-plain-http-uri-with-any-port:1337"))

        assertThat(
            "Port 1337 should not be excluded.",
            instance.value.getPort, is(equalTo(1337))
        )
    }

    @Test
    def value_contains_all_of_the_original_parameters {
        val expectedQuery = new Query(
            List(
                new NameValuePair("a", "a_value"),
                new NameValuePair("b", "b_value"),
                new NameValuePair("c", "c_value")
            )
        )

        val uriWithParameters = new URI("http://xxx/?a=a_value&b=b_value&c=c_value")

        given_a_signed_uri(uriWithParameters)

        then_value_contains_all_query_parameters(expectedQuery)
    }

    @Test
    def value_contains_expected_oauth_parameters() {
        given_a_signed_uri(new URI("http://abcdefg/"))

        val requiredQueryParameters = List(
            "oauth_consumer_key",
            "oauth_signature_method",
            "oauth_timestamp",
            "oauth_nonce",
            "oauth_version",
            "oauth_signature"
        )

        val signedUri : URI = instance.value

        val actualQuery : Query = parseQuery(signedUri getQuery)

        requiredQueryParameters foreach(
            requiredName => {
                assertTrue(
                    "The query <" + actualQuery.toString + "> " +
                    "should contain <" + requiredName + "> query parameter.",
                    actualQuery.contains(requiredName)
                )

                assertThat(
                    "Expected each oauth parameter to appear exactly once. " +
                    "The <" + requiredName + "> parameter appears " +
                    "<" + actualQuery.get(requiredName).size + "> times",
                    actualQuery.get(requiredName).size, is(equalTo(1))
                )
            }
        )
    }

	@Test
	def both_consumer_and_token_are_used_to_sign {
		val uri = new URI("http://xxx")
		val timestamp = "1259067839"
		val nonce = "73f0f93345d76d6cd1bab30af14a99e3"

		val credentials : CredentialSet = new CredentialSet(
			new Credential("key", "secret"),
			new Credential("token", "token_secret")
		)

		val signedUri = new SignedUri(
			uri,
			credentials,
			timestamp,
			nonce
		)

		val expectedSignature = "q89vhbqDzUX9aVeuavDhAP4TTPA="

		var actualSignature = parseQuery(signedUri.value).get("oauth_signature")(0).value

		assertThat(actualSignature, is(equalTo(expectedSignature)))
	}

    @Test
    def value_contains_all_expected_parameters {
        val uri = new URI("http://xxx/")
        val timestamp = "1257608197"
        val nonce = "ea757706c42e2b14a7a8999acdc71089"

        val signedUri = new SignedUri(
            uri,
            CredentialSet(forConsumer(consumerCredential), andNoToken),
            timestamp,
            nonce
        )

        val expectedSignedUrl = "http://xxx/?oauth_version=1.0&" +
			"oauth_nonce=ea757706c42e2b14a7a8999acdc71089&oauth_timestamp=1257608197&" +
			"oauth_consumer_key=key&oauth_signature_method=HMAC-SHA1&" +
			"oauth_signature=RO8XXXVxGl1kzYs%2FC7ueQzo974k%3D"

        val expectedSignature = "RO8XXXVxGl1kzYs/C7ueQzo974k="

        val expectedParams : Query = parseQuery(new URI(expectedSignedUrl))
        val actualParams : Query = parseQuery(signedUri.value)
       
        expectedParams.foreach(nameValuePair => assertTrue(actualParams.contains(nameValuePair.name)))
    }

	// TODO: Refactor to match org.coriander.unit.tests.oauth.core.SignatureBaseStringTest.result_excludes_default_port_80_for_http_and_443_for_https
	@Test
	def value_excludes_default_port_80_for_http {
		var uri = new URI("http://xxx:80/")
        var timestamp = "1259226604"
        var nonce = "e2d77f64c61903a24d9b6ad8e9c4e71c"
		                                  
        var signedUri = new SignedUri(
            uri,
            CredentialSet(forConsumer(consumerCredential), andNoToken),
            timestamp,
            nonce,
            Options.DEFAULT
        )

		var expectedSignature = "lL9UsKRGi6y9UT5Rlgaag56RgT8="
		val actualSignature = parseQuery(signedUri.value).get("oauth_signature")(0).value

		assertThat(actualSignature, is(equalTo(expectedSignature)))
	}

	// TEST: When I create 2 instances, then each has a different timestamp value

    private def give_a_signed_uri {
        given_a_signed_uri(anyUri)
    }

    private def given_a_signed_uri(uri : URI) {
        val signatureMethod = "any-signature-method"
        val timestamp = "any-timestamp"
        val nonce = "any-nonce"
        val version = "any-version"

        instance = new SignedUri(
            uri,
            CredentialSet(forConsumer(consumerCredential), andNoToken),
            timestamp,
            nonce,
            Options.DEFAULT
        )
    }

    private def then_value_contains_all_query_parameters(expectedQueryParameters : Query) {
        val actualQueryParameters = parseQuery(instance.value getQuery)

        assertContainsAll(
            expectedQueryParameters,
            actualQueryParameters
        )
    }
}
