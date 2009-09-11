package org.coriander.oauth.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers._
import org.junit.matchers.JUnitMatchers._
import org.hamcrest._
import org.hamcrest.Matcher._
import org.hamcrest.CoreMatchers._
import org.junit._
import org.apache.commons.httpclient._
import org.apache.commons.httpclient.util._
import org.junit.rules._
import scala.collection.immutable._
import scala.util.matching.Regex._
import java.util.regex._
import scala.actors.Actor._
import org.jfugue._

import org.coriander.oauth._
import org.coriander.oauth.timestamp._
import org.coriander.oauth.nonce._

class SignatureBaseStringTest extends TestBase {
    val consumerCredential = new OAuthCredential("key", "secret")

    var aValidUri =  new java.net.URI("http://xxx/")
    val aValidNonce = new SystemNonceFactory().createNonce
    val aValidTimestamp = new SystemTimestampFactory().createTimestamp
    var parameters : Map[String, String] = Map()
    var signatureBaseString : SignatureBaseString = null;

    var done = false
    val caller = self
    
    @Test
    def given_a_parameter_containing_reserved_character_then_the_result_contains_url_encoded_parameter() {
        val originalValue = "this value requires encoding"
        val expectedEncodedValue = "this%20value%20requires%20encoding"

        val result = newSignatureBaseString(Map("xxx" -> originalValue))

        val actualValue = parseParameters(result).
            find(item => item.getName() == "xxx").
            get.getValue;

        assertEquals(expectedEncodedValue, actualValue)
    }
    
    @Test
    def given_a_parameter_containing_reserved_character_then_result_is_RFC3629_percent_encoded() {
        val originalValue = "http://some-url?param=value"
        val expectedEncodedValue = "http%3A%2F%2Fsome-url%3Fparam%3Dvalue"

        val result = newSignatureBaseString(Map("xxx" -> originalValue))

        val actualValue = parseParameters(result).
            find(item => item.getName() == "xxx").
            get.getValue;

        assertThat(
            "The result must conform to RFC3629 for percent-encoding",
            actualValue, is(equalTo(expectedEncodedValue))
        )
    }

    @Test
    def given_an_unsorted_list_of_parameters_then_result_contains_them_all_and_they_are_sorted() {
        given_an_unsorted_list_of_parameters
        when_signature_base_string_is_created

        var parametersExcludingOAuth : List[NameValuePair] = trimOAuth(
            parseParameters(signatureBaseString)
        )

        val expectedList : List[NameValuePair] = List(
            new NameValuePair("a", "a_value"),
            new NameValuePair("b", "b_value"),
            new NameValuePair("c", "c_value")
        )

        for (i <- 0 to expectedList.length - 1) {
            assertThat(
                "Sorting of resultant query parameters should match expected.",
                parametersExcludingOAuth(i),
                is(equalTo(expectedList(i)))
            )
        }
    }

    @Test
    def given_a_list_of_parameters_then_result_contains_them_all() {
        given_a_list_of_parameters

        val result = new SignatureBaseString(
            aValidUri,
            parameters,
            consumerCredential,
            aValidNonce,
            aValidTimestamp
        )

        val allParameters : List[NameValuePair] = parseParameters(result)

        parameters.foreach((nameValueTuple : Tuple2[String, String]) => {
            assertThat(
                String.format(
                    "Expected the returned parameters to contain " +
                    "parameter called '%1$s', with value '%2$s'",
                    nameValueTuple._1,
                    nameValueTuple._2
                ),
                contains(allParameters, nameValueTuple),
                is(true)
            )
        })
    }

    // See: http://oauth.net/core/1.0/#anchor13
    @Test
    def result_contains_all_expected_oauth_parameters() {
        given_a_list_of_parameters
        when_signature_base_string_is_created
        
        val allParameters = parseParameters(signatureBaseString)

        val requiredParameters = List(
            "oauth_consumer_key",
            "oauth_signature_method",
            "oauth_timestamp",
            "oauth_nonce",
            "oauth_version"
        )

        requiredParameters foreach(
            requiredName => assertContainsName(allParameters, requiredName)
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
    def given_a_uri_containing_port_number_then_result_excludes_it() {
        given_a_uri(new java.net.URI("http://xxx:1337/"))
        
        given_a_list_of_parameters

        when_signature_base_string_is_created

        assertFalse(
            "Expected the port number to have been stripped, but it wasn't.",
            "^gethttp://xxx?".r.findAllIn(signatureBaseString.toString) == None
        )

        assertFalse(
            "Expected the port number to have been stripped, but it wasn't.",
            "^gethttp://xxx:1337".r.findAllIn(signatureBaseString.toString) hasNext
        )
    }

    @Test
    def given_a_uri_containing_path_then_result_contains_it() {
        given_a_uri(new java.net.URI("http://xxx.com/yyy/zzz/index.html"))

        given_a_list_of_parameters

        when_signature_base_string_is_created

        assertThat(
            signatureBaseString.toString,
            containsString(URLEncoder.%%(aValidUri.getPath))
        )
    }

    @Test
    def when_I_create_an_instance_without_supplying_a_method_then_method_defaults_to_get() {
        val signatureBaseString = new SignatureBaseString(
            aValidUri,
            parameters,
            consumerCredential,
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
            new java.net.URI("http://xxx/"),
            Map(),
            new OAuthCredential("key", "secret"),
            "ddb61ca14d02e9ef7b55cc5c1f88616f",
            "1252500234"
        ) toString;

        assertEquals("Actual does not match expected.", expected, actual)
    }

    // TEST: Result includes absolute URL (scheme, host (excluding port) and absolute path), and is in lower case
    // TEST: When URL contains ending slash, then it is included in the result
    // TEST: When URL contains query string, then it is excluded in the result
    // TEST: When I create 2 instances, then each has a different timestamp value
    // TEST: I can supply timestamp behaviour (or value) to create a SignatureBaseString instance
    // TEST: I can supply nonce behaviour (or value) to create a SignatureBaseString instance
    // TEST: This class only requires oauth_key, not an entire OAuthCredential

    private def given_a_uri(uri: java.net.URI) {
        aValidUri = uri;
    }

    private def given_a_list_of_parameters() {
        parameters = Map(
            "a" -> "a_value",
            "b" -> "b_value",
            "c" -> "c_value"
        )
    }

    private def given_an_unsorted_list_of_parameters() {
        parameters = Map(
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
            aValidUri,
            parameters,
            consumerCredential,
            aValidNonce,
            aValidTimestamp
        );
    }

    private def newSignatureBaseString(parameters : Map[String, String]) :
        SignatureBaseString = {
        new SignatureBaseString(
            aValidUri,
            parameters,
            consumerCredential,
            aValidNonce,
            aValidTimestamp
        );
    }

    private def getQueryParameter(url : String, name : String) : String = {
        parseQuery(url).
            find(item => item.getName() == name).
            get.getValue;
    }

    // TODO: Consider moving elsewhere
    private def parseParameters(signatureBaseString : SignatureBaseString) : List[NameValuePair] = {
        val (method, url, parameters) = parse(signatureBaseString)
        parameters
    }

    // TODO: Consider moving elsewhere
    private def parse(signatureBaseString : SignatureBaseString) :
        Tuple3[String, String, List[NameValuePair]] = {
        val parts = signatureBaseString.toString().split("&")

        val method = parts(0)
        val url = parts(1)
        val encodedParams = parts(2)
        
        (method, url, toNameValuePairs(urlDecode(encodedParams)))
    }    

    // TODO: Consider moving elsewhere
    private def toNameValuePairs(parameterString : String) : List[NameValuePair] = {
        var result : List[NameValuePair] = List[NameValuePair]()
        
        parameterString.split("&").foreach((pair : String) => {
           val parts = pair.split("=");
           result = new NameValuePair(parts(0), parts(1)) :: result // TODO: This is prepending!
        });

        result.reverse;
    }

    private def urlDecode(str : String) : String = {
        java.net.URLDecoder.decode(str, "UTF-8")
    }
}
