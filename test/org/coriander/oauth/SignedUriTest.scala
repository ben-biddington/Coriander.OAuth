package org.coriander.oauth.tests

import java.net.URI
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import org.apache.commons.httpclient._
import org.apache.commons.httpclient.util._

class SignedUriTest extends TestBase {

    val consumerCredential = new OAuthCredential("key", "secret")
    val anyUri : URI = new URI("http://any-host/default.html")

    var _instance : SignedUri = null

    @Before
    def given_instance_starts_as_null() {
        _instance = null
    }

    // TEST: Contains all parameters sorted
    // TEST: Has the same URI scheme
    // TEST: Adheres to the same rules about transparent ports

    @Test
    def value_contains_all_of_the_original_parameters {
        val expectedQueryParameters : List[NameValuePair] = List(
            new NameValuePair("a", "a_value"),
            new NameValuePair("b", "b_value"),
            new NameValuePair("c", "c_value")
        )

        val uriWithParameters = new URI("http://xxx/?a=a_value&b=b_value&c=c_value")

        give_a_signed_uri(uriWithParameters)

        then_value_contains_all_query_parameters(expectedQueryParameters)
    }

    @Test
    def value_contains_expected_oauth_paremeters() {
        give_a_signed_uri
    }

    private def give_a_signed_uri {
        give_a_signed_uri(anyUri)

        val requiredQueryParameters = List(
            "oauth_consumer_key",
            "oauth_signature_method",
            "oauth_timestamp",
            "oauth_nonce",
            "oauth_version"
        )

        val actualQueryParameters = parseQuery(_instance.value getQuery)

        requiredQueryParameters foreach(
            requiredName => assertContainsName(actualQueryParameters, requiredName)
        )
    }

    private def give_a_signed_uri(uri : URI) {
        _instance = new SignedUri(uri, consumerCredential);
    }

    private def then_value_contains_all_query_parameters(expectedQueryParameters : List[NameValuePair]) {
        val actualQueryParameters = parseQuery(_instance.value getQuery)

        assertContainsAll(
            expectedQueryParameters,
            actualQueryParameters
        )
    }
}
