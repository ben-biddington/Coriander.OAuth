package org.coriander.unit.tests.oauth.core

import org.junit.Assert._
import org.junit.matchers.JUnitMatchers._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit._
import org.junit.rules._

import org.coriander._
import java.net.URI
import oauth.core._
import oauth.core.nonce.SystemNonceFactory
import oauth.core.timestamp.SystemTimestampFactory
import CredentialSet._
import oauth.core.uri.{Port, OAuthUrlEncoder}
import unit.tests.TestBase
import java.lang.String

class SignatureBaseStringTest extends TestBase {
    val consumerCredential 	= new Credential("key", "secret")
    var uri 				= new java.net.URI("http://xxx/")
    val aValidNonce 		= new SystemNonceFactory newNonce
    val aValidTimestamp 	= new SystemTimestampFactory newTimestamp
    var query           	= Query()
    val urlEncoder 	    	= new OAuthUrlEncoder
	var signatureBaseString : SignatureBaseString = null

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
            parseParameters(signatureBaseString)
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
            uri,
            query,
            CredentialSet(forConsumer(consumerCredential), andNoToken),
            aValidNonce,
            aValidTimestamp
        )

        val allParameters : Query = parseParameters(result)

        query.foreach(nameValuePair =>
            assertTrue(allParameters.contains(nameValuePair.name))
        )
    }

    // See: http://oauth.net/core/1.0/#anchor13
    @Test
    def result_contains_all_expected_oauth_parameters() {
        given_a_list_of_parameters
        when_signature_base_string_is_created
        
        val allParameters = parseParameters(signatureBaseString)

        val requiredParameters = List(
			Parameters.Names.CONSUMER_KEY,
            Parameters.Names.SIGNATURE_METHOD,
            Parameters.Names.TIMESTAMP,
            Parameters.Names.NONCE,
            Parameters.Names.VERSION
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

        val plainTextValue = urlDecode(signatureBaseString.toString)

        assertStartsWith("^GET&http://xxx:1337/&", plainTextValue)
    }

    @Test
    def result_excludes_default_port_80_for_http_and_443_for_https() {
        assertResultExcludes(Port("http"	, 80))
        assertResultIncludes(Port("http"	, 443))
        assertResultExcludes(Port("https"	, 443))
        assertResultIncludes(Port("https"	, 80))
    }

    @Test
    def given_a_uri_containing_path_then_result_contains_it() {
        given_a_uri(new java.net.URI("http://xxx.com/yyy/zzz/index.html"))

        given_a_list_of_parameters

        when_signature_base_string_is_created

		val expectedPath: String = uri getPath

		assertThat(
            signatureBaseString toString,
            containsString(urlEncoder.%%(expectedPath))
        )
    }

    @Test
    def when_I_create_an_instance_without_supplying_a_method_then_method_defaults_to_get() {
        val signatureBaseString = new SignatureBaseString(
            uri,
            query,
            CredentialSet(consumerCredential),
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

	// See: http://oauth.net/core/1.0/#anchor14, 9.1.2. Construct Request URL
	@Test
	def result_includes_url_scheme_lowercase {
		val expected = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey" +
            "%26oauth_nonce%3Dddb61ca14d02e9ef7b55cc5c1f88616f%26" +
            "oauth_signature_method%3DHMAC-SHA1%26" +
            "oauth_timestamp%3D1252500234%26oauth_token%3D%26oauth_version%3D1.0"

        // [i] Signature for above: oyg55+J+tiWduaXMdMFrCS/PMZQ=

        val actual = new SignatureBaseString(
            "get",
            new URI("HTTP://xxx/"),
            new Query(),
            CredentialSet(forConsumer(new Credential("key", "secret")), andNoToken),
            "ddb61ca14d02e9ef7b55cc5c1f88616f",
            "1252500234",
			Options.DEFAULT
        ) toString

        assertEquals("Actual does not match expected.", expected, actual)
	}

	// See: http://oauth.net/core/1.0/#anchor14, 9.1.2. Construct Request URL
	@Test
	def result_includes_url_authority_in_lowercase {
		val expected = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey" +
            "%26oauth_nonce%3Dddb61ca14d02e9ef7b55cc5c1f88616f%26" +
            "oauth_signature_method%3DHMAC-SHA1%26" +
            "oauth_timestamp%3D1252500234%26oauth_token%3D%26oauth_version%3D1.0"

        // [i] Signature for above: oyg55+J+tiWduaXMdMFrCS/PMZQ=

        val actual = new SignatureBaseString(
            "get",
            new URI("http://XXX/"),
            new Query(),
            CredentialSet(forConsumer(new Credential("key", "secret")), andNoToken),
            "ddb61ca14d02e9ef7b55cc5c1f88616f",
            "1252500234",
			Options.DEFAULT
        ) toString

        assertEquals("Actual does not match expected.", expected, actual)
	}

	// See: http://oauth.net/core/1.0/#anchor14, 9.1.2. Construct Request URL
	@Test
	def result_includes_url_path_as_declared_i_e_not_lower_cased {
		val expected = "GET&http%3A%2F%2Fxxx%2FANYPATH&oauth_consumer_key%3Dkey" +
            "%26oauth_nonce%3Dddb61ca14d02e9ef7b55cc5c1f88616f%26" +
            "oauth_signature_method%3DHMAC-SHA1%26" +
            "oauth_timestamp%3D1252500234%26oauth_token%3D%26oauth_version%3D1.0"

        // [i] Signature for above: oyg55+J+tiWduaXMdMFrCS/PMZQ=

        val actual = new SignatureBaseString(
            "get",
            new URI("http://xxx/ANYPATH"),
            new Query(),
            CredentialSet(forConsumer(new Credential("key", "secret")), andNoToken),
            "ddb61ca14d02e9ef7b55cc5c1f88616f",
            "1252500234",
			Options.DEFAULT
        ) toString

        assertEquals("Actual does not match expected.", expected, actual)
	}

    // See: http://term.ie/oauth/example/client.php
    // See also: http://oauth.net/core/1.0#sig_base_example
    @Test
    def examples {
        val expected = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey" +
            "%26oauth_nonce%3Dddb61ca14d02e9ef7b55cc5c1f88616f%26" +
            "oauth_signature_method%3DHMAC-SHA1%26" +
            "oauth_timestamp%3D1252500234%26oauth_token%3D%26oauth_version%3D1.0"
        
        // [i] Signature for above: oyg55+J+tiWduaXMdMFrCS/PMZQ=

        val actual = new SignatureBaseString(
            "get",
            new URI("http://xxx/"),
            new Query(),
            CredentialSet(forConsumer(new Credential("key", "secret")), andNoToken),
            "ddb61ca14d02e9ef7b55cc5c1f88616f",
            "1252500234",
			Options.DEFAULT
        ) toString

        assertEquals("Actual does not match expected.", expected, actual)
    }

	// See: http://oauth.net/core/1.0a#RFC2617, Appendix A.5.1.  Generating Signature Base String
	@Test
	def example_with_token_from_the_oauth_spec_document {
		val expected = "GET&http%3A%2F%2Fphotos.example.net%2Fphotos&file%3Dvacation.jpg" +
			"%26oauth_consumer_key%3Ddpf43f3p2l4k3l03%26oauth_nonce%3Dkllo9940pd9333jh%26" +
			"oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1191242096%26" +
			"oauth_token%3Dnnch734d00sl2jdk%26oauth_version%3D1.0%26size%3Doriginal"
				
		val timestamp = "1191242096"
		val nonce = "kllo9940pd9333jh"
		val version = "1.0"

		val consumer 	= new Credential("dpf43f3p2l4k3l03", "kd94hf93k423kf44")
		val token 		= new Credential("nnch734d00sl2jdk", "pfkkdhi9sl3r4s00")
        val credentials = CredentialSet(forConsumer(consumer), andToken(token))

		val uri : URI = new URI("http://photos.example.net/photos")
		val query = new QueryParser().
				parse("file=vacation.jpg&size=original").
				filter(nvp => false == nvp.name.startsWith("oauth_"))

		val signatureBaseString = new SignatureBaseString(uri, query, credentials, nonce, timestamp)

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

		val credentials = new CredentialSet(
			new Credential("key", "secret"),
			new Credential("HZvFeX5T7XlRIcJme/EWTg==", "Ao61gCJXIM20aqLDw7+Cow==")
		)
        
		val uri : URI = new URI("http://xxx")
		val query = Query()

		val signatureBaseString = new SignatureBaseString(uri, query, credentials, nonce, timestamp)

		assertThat(
			signatureBaseString toString,
			is(equalTo(expected))
		)
	}


	@Test
	def token_is_always_included_even_when_it_has_no_value {
		val expected = "GET&http%3A%2F%2Fxxx&" +
			"oauth_consumer_key%3Dkey%26oauth_nonce%3D1108721620a4c6093f92b24d5844e61b%26" +
			"oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1259051683%26" +
			"oauth_token%3D%26oauth_version%3D1.0"

		val timestamp = "1259051683"
		val nonce = "1108721620a4c6093f92b24d5844e61b"

		val credentials = new CredentialSet(
			new Credential("key", "secret"),
			null
		)

		val uri : URI = new URI("http://xxx")
		val query = Query()

		val signatureBaseString = new SignatureBaseString(uri, query, credentials, nonce, timestamp)

		assertThat(
			signatureBaseString toString,
			is(equalTo(expected))
		)
	}

    // TEST: When URL contains ending slash, then it is included in the result
    // TEST: When URL contains query string, then it is excluded in the result
    // TEST: This class only requires oauth_key, not an entire Credential

    private def assertResultExcludes(port : Port) {
        val expectedUriString = port.scheme + "://xxx/"
        val suppliedUriString = port.scheme + "://xxx:" + port.number.toString  + "/"

        given_a_uri(new java.net.URI(suppliedUriString))

        given_a_list_of_parameters

        when_signature_base_string_is_created

        val plainTextValue = urlDecode(signatureBaseString.toString)

        assertStartsWith("^GET&" + expectedUriString + "&", plainTextValue)
    }

     private def assertResultIncludes(port : Port) {
        val expectedUriString = port.scheme + "://xxx:" + port.number.toString + "/"
        val suppliedUriString = expectedUriString

        given_a_uri(new java.net.URI(suppliedUriString))

        given_a_list_of_parameters

        when_signature_base_string_is_created

        val plainTextValue = urlDecode(signatureBaseString.toString)

        assertStartsWith("^GET&" + expectedUriString + "&", plainTextValue)
    }

    private def given_a_uri(uri: java.net.URI) = this.uri = uri;

    private def given_a_list_of_parameters {
        query = Query.from(
            "a" -> "a_value",
            "b" -> "b_value",
            "c" -> "c_value"
        )
    }

    private def given_an_unsorted_list_of_parameters {
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
        signatureBaseString = createDefault(method)
    }

    private def createDefault(method : String) : SignatureBaseString = {
         new SignatureBaseString(
            method,
            uri,
            query,
            CredentialSet(forConsumer(consumerCredential), andNoToken),
            aValidNonce,
            aValidTimestamp,
		 	Options.DEFAULT
        );
    }

    private def newSignatureBaseString(query : Query) :
        SignatureBaseString = {
        new SignatureBaseString(
            uri,
            query,
            CredentialSet(forConsumer(consumerCredential), andNoToken),
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

        val method : String			= parts(0)
        val url : String 			= parts(1)
        val encodedParams : String	= parts(2)

        val query = parseQuery(urlDecode(encodedParams))
        
        (method, url, query)
    }
}