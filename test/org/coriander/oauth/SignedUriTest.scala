package org.coriander.oauth.tests

import java.net.URI
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.matchers.JUnitMatchers._

class SignedUriTest extends TestBase {

    val consumerCredential = new OAuthCredential("key", "secret")
    val anyUri : URI = new URI("http://any-host/default.html")

    val signatureMethod = "HMAC-SHA1"
    val version = "1.0"

    var instance : SignedUri = null

    @Before
    def given_instance_starts_as_null() {
        instance = null
    }

    // TEST: The sort order of the parameters does not matter
    // TEST: Adheres to the same rules about transparent ports

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
    def result_always_includes_port {
        given_a_signed_uri(new URI("https://an-https-uri-with-default-port:443"))

        assertThat(
            "Port 443 should not be excluded.",
            instance.value.getPort, is(equalTo(443))
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
    def xxx {
        var parameters : List[NameValuePair] = List(new NameValuePair("c", "c"))

        val oauthParams = Map("name" -> "value", "another_name" -> "value")

        oauthParams.foreach(item => {
            val (name, value) = item

            parameters += new NameValuePair(name, value)
        })

        val query = new Query()
 
        query.foreach(nvp => {parameters += nvp})
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
    def examples {
        val uri = new URI("http://xxx/")
        val token = null
        val timestamp = "1257608197"
        val nonce = "ea757706c42e2b14a7a8999acdc71089"

        val signedUri = new SignedUri(
            uri,
            consumerCredential,
            token,
            signatureMethod,
            timestamp,
            nonce,
            version
        )
        
        val expectedSignedUrl = "http://xxx/?oauth_version=1.0&oauth_nonce=ea757706c42e2b14a7a8999acdc71089&oauth_timestamp=1257608197&oauth_consumer_key=key&oauth_signature_method=HMAC-SHA1&oauth_signature=RO8XXXVxGl1kzYs%2FC7ueQzo974k%3D"
        val expecteSignature = "RO8XXXVxGl1kzYs/C7ueQzo974k="

        val expectedParams : Query = parseQuery(new URI(expectedSignedUrl))
        val actualParams : Query = parseQuery(signedUri.value)
       
        expectedParams.foreach(nameValuePair => {
            assertTrue(actualParams.contains(nameValuePair.name))
        })
    }

    private def give_a_signed_uri {
        given_a_signed_uri(anyUri)
    }

    private def given_a_signed_uri(uri : URI) {
        val token = null
        val signatureMethod = "any-signature-method"
        val timestamp = "any-timestamp"
        val nonce = "any-nonce"
        val version = "any-version"
        
        instance = new SignedUri(
            uri,
            consumerCredential,
            null,
            signatureMethod,
            timestamp,
            nonce,
            version
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
