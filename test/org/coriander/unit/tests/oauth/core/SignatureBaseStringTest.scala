package org.coriander.unit.tests.oauth.core


import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers._
import org.junit.matchers.JUnitMatchers._
import org.hamcrest._
import org.coriander.oauth.core.{SignatureBaseString,OAuthCredential}
import org.hamcrest.Matcher._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit._
import org.junit.rules._
import scala.collection.immutable._
import scala.util.matching.Regex._
import java.util.regex._

import org.coriander._
import java.net.URI
import oauth.core.nonce.SystemNonceFactory
import oauth.core.timestamp.SystemTimestampFactory
import oauth.core.uri.OAuthURLEncoder
import unit.tests.TestBase

class SignatureBaseStringTest extends TestBase {
    val consumerCredential = new org.coriander.oauth.core.OAuthCredential("key", "secret")
	
    var aValidUri =  new java.net.URI("http://xxx/")
    val aValidNonce = new SystemNonceFactory createNonce
    val aValidTimestamp = new SystemTimestampFactory createTimestamp
    var query : Query = new Query
    var _signatureBaseString : SignatureBaseString = null;
    val _urlEncoder = new OAuthURLEncoder

    var done = false
    
    @Test
    def parameters_appear_in_the_result_twice_RFC3629_percent_encoded() {
        val originalValue = "http://some-url?param=value"

		var expectedEncodedValue : String = originalValue

		times(2) { expectedEncodedValue = urlEncode(expectedEncodedValue) }

        val query = Query.from("xxx" -> originalValue)

        val result = newSignatureBaseString(query) toString

        assertThat(
            "The result must conform to RFC3629 for percent-encoding",
            result, containsString(expectedEncodedValue)
        )
    }

    @Test
    def given_an_unsorted_list_of_parameters_then_result_contains_them_all_and_they_are_sorted() {
        given_an_unsorted_list_of_parameters
        when_signature_base_string_is_created

        var queryExcludingOAuth = trimOAuth(
            parseParameters(_signatureBaseString)
        )

        val expectedQuery = Query.from(
			"a" -> "a_value",
			"b" -> "b_value",
			"c" -> "c_value"
        )

        assertAreEqual(expectedQuery, queryExcludingOAuth)
    }

    @Test
    def given_a_list_of_parameters_then_result_contains_them_all() {
        given_a_list_of_parameters

        val result = new SignatureBaseString(
            aValidUri,
            query,
            consumerCredential,
			null,
            aValidNonce,
            aValidTimestamp
        )

        val allParameters : Query = parseParameters(result)

        query.foreach(nameValuePair => {
            assertTrue(allParameters.contains(nameValuePair.name))
        })
    }

    // See: http://oauth.net/core/1.0/#anchor13
    @Test
    def result_contains_all_expected_oauthparameters() {
        given_a_list_of_parameters
        when_signature_base_string_is_created
        
        val allParameters = parseParameters(_signatureBaseString)

        val requiredParameters = List(
            "oauth_consumer_key",
            "oauth_signature_method",
            "oauth_timestamp",
            "oauth_nonce",
            "oauth_version"
        )

        requiredParameters foreach(
            requiredName => assertTrue(allParameters.contains(requiredName))
        )
    }

    @Test
    def given_an_http_verb_in_uppercase_then_result_starts_with_the_uppercase_version() {
        val expectedMethod = "GET"

        given_a_list_of_parameters
       
        val example : String = createDefault(expectedMethod toLowerCase).toString

        var pattern = "^" + expectedMethod r

        assertTrue(
            String format(
                "Expected that the returned value would begin with <%1$s>, " +
                "but it did not. Actual: <%2$s>",
                expectedMethod,
                example
            ),
            pattern.findPrefixOf(example) != None
        )
    }

    // See: http://oauth.net/core/1.0#sig_base_example
    @Test
    def result_contains_method_and_url_separated_by_ampersand() {
        val method = "xxx"
        val expected = method.toUpperCase + "&"

        given_a_list_of_parameters

        val example : String = createDefault(method).toString

        var pattern = "^" + expected r

        assertTrue(
            String format(
                "Expected that the returned value would begin with <%1$s>, " +
                "but it did not. Actual: <%2$s>",
                expected,
                example
            ),
            pattern.findPrefixOf(example) != None
        )
    }

    @Test
    def given_a_uri_containing_non_default_port_number_then_result_includes_it() {
        given_a_uri(new java.net.URI("http://xxx:1337/"))
        
        given_a_list_of_parameters

        when_signature_base_string_is_created

        val plainTextValue = urlDecode(_signatureBaseString.toString)

        assertStartsWith("^GET&http://xxx:1337/&", plainTextValue)
    }

    @Test
    def result_excludes_default_port_80_for_http_and_443_for_https() {
        assertResultExcludesPort("http", 80)
        assertResultIncludesPort("http", 443)
        assertResultExcludesPort("https", 443)
        assertResultIncludesPort("https", 80)
    }

    @Test
    def given_a_uri_containing_path_then_result_contains_it() {
        given_a_uri(new java.net.URI("http://xxx.com/yyy/zzz/index.html"))

        given_a_list_of_parameters

        when_signature_base_string_is_created

        assertThat(
            _signatureBaseString.toString,
            containsString(_urlEncoder.%%(aValidUri.getPath))
        )
    }

    @Test
    def when_I_create_an_instance_without_supplying_a_method_then_method_defaults_to_get() {
        val signatureBaseString = new SignatureBaseString(
            aValidUri,
            query,
            consumerCredential,
			null,
            aValidNonce,
            aValidTimestamp
        )

        val expectedMethod = "GET"

        assertTrue(
            String format(
                "Expected that the returned value would begin with <%1$s>, " +
                "but it did not. Actual: <%2$s>",
                expectedMethod,
                signatureBaseString toString
            ),
            ("^" + expectedMethod).r.findPrefixOf(signatureBaseString toString) != None
        )
    }

    // See: http://term.ie/oauth/example/client.php
    // See also: http://oauth.net/core/1.0#sig_base_example
    @Test
    def examples() {
        val expected = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey" +
            "%26oauth_nonce%3Dddb61ca14d02e9ef7b55cc5c1f88616f%26" +
            "oauth_signature_method%3DHMAC-SHA1%26" +
            "oauth_timestamp%3D1252500234%26oauth_version%3D1.0"
        
        // Signature for above: oyg55+J+tiWduaXMdMFrCS/PMZQ=

        val actual = new SignatureBaseString(
            "get",
            new URI("http://xxx/"),
            new Query(),
            new OAuthCredential("key", "secret"),
			null,
            "ddb61ca14d02e9ef7b55cc5c1f88616f",
            "1252500234"
        ) toString;

        assertEquals("Actual does not match expected.", expected, actual)
    }

	// See: http://oauth.net/core/1.0a#RFC2617, Appendix A.5.1.  Generating Signature Base String
	@Test
	def an_example_with_token {
		val expected = "GET&http%3A%2F%2Fphotos.example.net%2Fphotos&file%3Dvacation.jpg" +
			"%26oauth_consumer_key%3Ddpf43f3p2l4k3l03%26oauth_nonce%3Dkllo9940pd9333jh%26" +
			"oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1191242096%26" +
			"oauth_token%3Dnnch734d00sl2jdk%26oauth_version%3D1.0%26size%3Doriginal"
				
		val timestamp = "1191242096"
		val nonce = "kllo9940pd9333jh"
		val version = "1.0"

		val consumer = new OAuthCredential("dpf43f3p2l4k3l03", "kd94hf93k423kf44")
		val token = new OAuthCredential("nnch734d00sl2jdk", "pfkkdhi9sl3r4s00")

		val uri : URI = new URI("http://photos.example.net/photos")
		val query = new QueryParser().
				parse("file=vacation.jpg&size=original").
				filter(nvp => false == nvp.name.startsWith("oauth_"))

		val signatureBaseString = new SignatureBaseString(uri, query, consumer, token, nonce, timestamp)

		assertThat(
			signatureBaseString toString,
			is(equalTo(expected))
		)
	}

	@Test
	def another_example_with_token {
		val expected = "GET&http%3A%2F%2Fxxx&" +
			"oauth_consumer_key%3Dkey%26oauth_nonce%3D1108721620a4c6093f92b24d5844e61b%26" +
			"oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1259051683%26" +
			"oauth_token%3DHZvFeX5T7XlRIcJme%252FEWTg%253D%253D%26oauth_version%3D1.0"

		val timestamp = "1259051683"
		val nonce = "1108721620a4c6093f92b24d5844e61b"
		val version = "1.0"

		val consumer = new OAuthCredential("key", "secret")
		val token = new OAuthCredential("HZvFeX5T7XlRIcJme/EWTg==", "Ao61gCJXIM20aqLDw7+Cow==")

		val uri : URI = new URI("http://xxx")
		val query = Query()

		val signatureBaseString = new SignatureBaseString(uri, query, consumer, token, nonce, timestamp)

		assertThat(
			signatureBaseString toString,
			is(equalTo(expected))
		)
	}

	// TEST: Result includes absolute URL (scheme, host (excluding port) and absolute path), and is in lower case
    // TEST: When URL contains ending slash, then it is included in the result
    // TEST: When URL contains query string, then it is excluded in the result
    // TEST: When I create 2 instances, then each has a different timestamp value
    // TEST: I can supply timestamp behaviour (or value) to create a SignatureBaseString instance
    // TEST: I can supply nonce behaviour (or value) to create a SignatureBaseString instance
    // TEST: This class only requires oauth_key, not an entire OAuthCredential

    private def assertResultExcludesPort(scheme: String, port : Int) {
        val expectedUriString = scheme + "://xxx/"
        val suppliedUriString = scheme + "://xxx:" + port.toString + "/"

        given_a_uri(new java.net.URI(suppliedUriString))

        given_a_list_of_parameters

        when_signature_base_string_is_created

        val plainTextValue = urlDecode(_signatureBaseString.toString)

        assertStartsWith("^GET&" + expectedUriString + "&", plainTextValue)
    }

     private def assertResultIncludesPort(scheme: String, port : Int) {
        val expectedUriString = scheme + "://xxx:" + port.toString + "/"
        val suppliedUriString = expectedUriString

        given_a_uri(new java.net.URI(suppliedUriString))

        given_a_list_of_parameters

        when_signature_base_string_is_created

        val plainTextValue = urlDecode(_signatureBaseString.toString)

        assertStartsWith("^GET&" + expectedUriString + "&", plainTextValue)
    }

    private def given_a_uri(uri: java.net.URI) {
        aValidUri = uri;
    }

    private def given_a_list_of_parameters() {
        query = Query.from(
            "a" -> "a_value",
            "b" -> "b_value",
            "c" -> "c_value"
        )
    }

    private def given_an_unsorted_list_of_parameters() {
        query = Query.from(
            "c" -> "c_value",
            "b" -> "b_value",
            "a" -> "a_value"
        )
    } 

    private def when_signature_base_string_is_created() {
        when_signature_base_string_is_created("get");
    }

    private def when_signature_base_string_is_created(method : String) {
        _signatureBaseString = createDefault(method)
    }

    private def createDefault(method : String) : SignatureBaseString = {
         new SignatureBaseString(
            method,
            aValidUri,
            query,
            consumerCredential,
		 	null,
            aValidNonce,
            aValidTimestamp
        );
    }

    private def newSignatureBaseString(query : Query) :
        SignatureBaseString = {
        new SignatureBaseString(
            aValidUri,
            query,
            consumerCredential,
			null,
            aValidNonce,
            aValidTimestamp
        );
    }

    private def parseParameters(signatureBaseString : SignatureBaseString) : Query = {
        val (method, url, query) = parse(signatureBaseString)
        query
    }

    private def parse(signatureBaseString : SignatureBaseString) : Tuple3[String, String, Query] = {        
        val parts = signatureBaseString.toString().split("&")

        val method = parts(0)
        val url = parts(1)
        val encodedParams = parts(2)

        val query = parseQuery(urlDecode(encodedParams))
        
        (method, url, query)
    }    
}