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
import org.coriander.oauth._
import scala.util.matching.Regex._
import java.util.regex._

class SignatureBaseStringTest extends TestBase {

    val consumerKey = "key"
    val consumerSecret = "secret"
    var aValidUri =  new java.net.URI("http://xxx/")

    var parameters : Map[String, String] = Map()
    var signatureBaseString : java.net.URI = null;
    
    @Test
    def given_a_parameter_containing_reserved_character_then_the_result_contains_url_encoded_parameter() {
        val originalValue = "this value requires encoding"
        val expectedEncodedValue = "this%20value%20requires%20encoding"

        val result : String = new SignatureBaseString(
            aValidUri,
            Map("xxx" -> originalValue),
            consumerKey,
            consumerKey
        )

        val actualValue = parseQuery(result).
            find(item => item.getName() == "xxx").
            get.getValue;

        Assert assertEquals(expectedEncodedValue, actualValue)
    }
    
    @Test
    def given_a_parameter_containing_reserved_character_then_result_is_RFC3629_percent_encoded() {
        val originalValue = "http://some-url?param=value"
        val expectedEncodedValue = "http%3A%2F%2Fsome-url%3Fparam%3Dvalue"

        val result : String = new SignatureBaseString(
            aValidUri,
            Map("xxx" -> originalValue),
            consumerKey,
            consumerKey
        )

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

        val result : java.net.URI = new SignatureBaseString(
            aValidUri,
            parameters,
            consumerKey,
            consumerKey
        )

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

        val result : java.net.URI = new SignatureBaseString(
            aValidUri,
            parameters,
            consumerKey,
            consumerKey
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
    def given_a_list_of_parameters_then_result_contains_all_expected_oauth_parameters() {
        given_a_list_of_parameters
        when_signature_base_string_is_created
        val allParameters : List[NameValuePair] = parseQuery(signatureBaseString.getQuery)

        val requiredParameters = List(
            "oauth_consumer_key",
            "oauth_signature_method",
            "oauth_timestamp",
            "oauth_nonce",
        )

        requiredParameters.foreach(
            requiredName => assertContainsName(allParameters, requiredName)
        )
    }

    @Test
    def given_an_http_verb_in_uppercase_then_result_starts_with_the_lowercase_version() {
        val expectedMethod = "get"

        given_a_list_of_parameters
       
        when_signature_base_string_is_created(expectedMethod toUpperCase)

        var pattern = "^" + expectedMethod r

        Assert assertFalse(
            String format(
                "Expected that the returned value would begin with <%1$s>, " +
                "but it did not. Actual: <%2$s>",
                expectedMethod,
                signatureBaseString.toString
            ),
            pattern.findPrefixOf(signatureBaseString.toString) == None
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
    def when_I_create_two_instances_then_each_has_a_different_nonce_value() {
        val aSignatureBaseString : String = new SignatureBaseString(
            "head",
            aValidUri,
            parameters,
            consumerKey,
            consumerKey
        )

        val anotherSignatureBaseString : String = new SignatureBaseString(
            "head",
            aValidUri,
            parameters,
            consumerKey,
            consumerKey
        )
        
        val theFirstNonce = getQueryParameter(aSignatureBaseString, "oauth_nonce")
        val theSecondNonce = getQueryParameter(anotherSignatureBaseString, "oauth_nonce")

        Assert assertTrue(
            String.format(
                "Expected two instances to have different nonces, " +
                "they both have value <%1$s>",
                theFirstNonce
            ),
            theSecondNonce != theFirstNonce
        )
    }

    // Test: Result includes absolute URL (scheme, host (excluding port) and absolute path), and is in lower case
    // Test: When URL contains ending slash, then it is included in the result
    // Test: When I create 2 instances, then each has a different timestamp value
    // Test: When I create an instance without an HTTP verb (method), then an error results

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
        signatureBaseString = new SignatureBaseString(
            method,
            aValidUri,
            parameters,
            consumerKey,
            consumerKey
        )

        //println(String.format("base_string='%1$s'", signatureBaseString))
    }

    private def getQueryParameter(url: String, name : String) : String = {
        parseQuery(url).
            find(item => item.getName() == name).
            get.getValue;
    }
}
