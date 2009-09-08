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
    var signatureBaseString : java.net.URI = null;

    var done = false
    val caller = self
    
    @Test
    def given_a_parameter_containing_reserved_character_then_the_result_contains_url_encoded_parameter() {
        val originalValue = "this value requires encoding"
        val expectedEncodedValue = "this%20value%20requires%20encoding"

        val result : String = newSignatureBaseString(Map("xxx" -> originalValue))

        val actualValue = parseQuery(result).
            find(item => item.getName() == "xxx").
            get.getValue;

        Assert assertEquals(expectedEncodedValue, actualValue)
    }
    
    @Test
    def given_a_parameter_containing_reserved_character_then_result_is_RFC3629_percent_encoded() {
        val originalValue = "http://some-url?param=value"
        val expectedEncodedValue = "http%3A%2F%2Fsome-url%3Fparam%3Dvalue"

        val result : String = newSignatureBaseString(Map("xxx" -> originalValue))

        val actualValue = parseQuery(result).
            find(item => item.getName() == "xxx").
            get.getValue;

        Assert assertThat(
            "The result must conform to RFC3629 for percent-encoding",
            actualValue, is(equalTo(expectedEncodedValue))
        )
    }

    @Test
    def given_an_unsorted_list_of_parameters_then_result_has_them_sorted() {
        given_an_unsorted_list_of_parameters

        val result : java.net.URI = toURI(newSignatureBaseString(parameters))

        var parametersExcludingOAuth : List[NameValuePair] = trimOAuth(
            parseQuery(result.getQuery)
        )

        val expectedList : List[NameValuePair] = List(
            new NameValuePair("a", "a_value"),
            new NameValuePair("b", "b_value"),
            new NameValuePair("c", "c_value")
        )

        for (i <- 0 to expectedList.length - 1) {
            Assert assertThat(
                "Sorting of resultant query parameters should match expected.",
                parametersExcludingOAuth(i),
                is(equalTo(expectedList(i)))
            )
        }
    }

    @Test
    def given_a_list_of_parameters_then_result_contains_them_all() {
        given_a_list_of_parameters

        val result : java.net.URI = toURI(
            new SignatureBaseString(
                aValidUri,
                parameters,
                consumerCredential,
                aValidNonce,
                aValidTimestamp
            )
        )

        val allParameters : List[NameValuePair] = parseQuery(result.getQuery)

        parameters.foreach((nameValueTuple : Tuple2[String, String]) => {
                Assert assertThat(
                    String.format(
                        "Expected the returned parameters to contain " +
                        "parameter called '%1$s', with value '%2$s'",
                        nameValueTuple._1,
                        nameValueTuple._2
                    ),
                    contains(allParameters, nameValueTuple),
                    is(true)
                )
            }
        )
    }

    // See: http://oauth.net/core/1.0/#anchor13
    @Test
    def result_contains_all_expected_oauth_parameters() {
        given_a_list_of_parameters
        when_signature_base_string_is_created
        val allParameters = parseQuery(signatureBaseString.getQuery)

        val requiredParameters = List(
            "oauth_consumer_key",
            "oauth_signature_method",
            "oauth_timestamp",
            "oauth_nonce",
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

        println(example)

        Assert assertTrue(
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
    @Test @Ignore
    def result_contains_method_and_url_separated_by_ampersand() {
        val method = "xxx"
        val expected = method.toUpperCase + "&"

        given_a_list_of_parameters

        when_signature_base_string_is_created(method)

        var pattern = "^" + expected r

        Assert assertTrue(
            String format(
                "Expected that the returned value would begin with <%1$s>, " +
                "but it did not. Actual: <%2$s>",
                expected,
                signatureBaseString.toString
            ),
            pattern.findPrefixOf(signatureBaseString.toString) != None
        )
    }

    @Test
    def given_a_uri_containing_port_number_then_result_excludes_it() {
        given_a_uri(new java.net.URI("http://xxx:1337/"))
        
        given_a_list_of_parameters

        when_signature_base_string_is_created

        Assert assertFalse(
            "Expected the port number to have been stripped, but it wasn't.",
            "^gethttp://xxx?".r.findAllIn(signatureBaseString.toString) == None
        )

        Assert assertFalse(
            "Expected the port number to have been stripped, but it wasn't.",
            "^gethttp://xxx:1337".r.findAllIn(signatureBaseString.toString) hasNext
        )
    }

    @Test
    def given_a_uri_containing_path_then_result_contains_it() {
        given_a_uri(new java.net.URI("http://xxx.com/yyy/zzz/index.html"))

        given_a_list_of_parameters

        when_signature_base_string_is_created

        Assert assertThat(
            signatureBaseString.toString,
            containsString(aValidUri.getPath)
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

        Assert assertTrue(
            String format(
                "Expected that the returned value would begin with <%1$s>, " +
                "but it did not. Actual: <%2$s>",
                expectedMethod,
                signatureBaseString toString
            ),
            ("^" + expectedMethod).r.findPrefixOf(signatureBaseString toString) != None
        )
    }

    // TEST: Result includes absolute URL (scheme, host (excluding port) and absolute path), and is in lower case
    // TEST: When URL contains ending slash, then it is included in the result
    // TEST: When I create 2 instances, then each has a different timestamp value
    // TEST: I can supply timestamp behaviour (or value) to create a SignatureBaseString instance
    // TEST: I can supply nonce behaviour (or value) to create a SignatureBaseString instance
    // TEST: This class only requires oauth_key, not an entire OAuthCredential

    // See: http://term.ie/oauth/example/client.php
    @Test
    def examples() {
        when_signature_base_string_is_created
        val expected = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26oauth_nonce%3Db03c8c22ad58d88d62cc46b345997b28%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1252410722%26oauth_version%3D1.0"
        println("Expected: " + expected)
        println("Actual: " + signatureBaseString)
        
        // For the current settings, the following is expected:
        //GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26oauth_nonce%3Db03c8c22ad58d88d62cc46b345997b28%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1252410722%26oauth_version%3D1.0
    }

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
        signatureBaseString = toURI(createDefault(method))

        //println(String.format("base_string='%1$s'", signatureBaseString))
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

    private def toURI(baseString : SignatureBaseString) : java.net.URI = {

        // [!] Relies on text starting with <method>&

        val index = baseString.toString.indexOf('&') + 1

        val uriPart = baseString.toString().substring(index)

        new java.net.URI(uriPart);
    }
}
