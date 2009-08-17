package org.coriander.tests.oauth

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers._
import org.hamcrest.CoreMatchers._
import org.junit._
import org.apache.commons.httpclient._
import org.apache.commons.httpclient.util._
import org.junit.rules._
import scala.collection.immutable._
import org.coriander.oauth._

class SignatureBaseStringTest extends TestBase {

    val consumerKey = "key"
    val consumerSecret = "secret"
    val aValidUri =  new java.net.URI("http://xxx/")

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

        Assert.assertEquals(expectedEncodedValue, actualValue)
    }

    // Test: The string-to-sign must contain the oauth parameters, as well as all the supplied parameters
    // Test: The result has ALL the params supplied

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

        Assert.assertThat(
            "The result must conform to RFC3629 for percent-encoding",
            actualValue, is(equalTo(expectedEncodedValue))
        )
    }

    @Test
    def given_an_unsorted_list_of_parameters_then_result_has_them_sorted() {
        val unsortedParams = Map(
            "c" -> "c_value",
            "b" -> "b_value",
            "a" -> "a_value"
        )

        val result : java.net.URI = new SignatureBaseString(
            aValidUri,
            unsortedParams,
            consumerKey,
            consumerKey
        )

        var parametersExcludingOAuth : List[NameValuePair] = trimOAuth(
            parseQuery(result.getQuery)
        )
        
        //        parametersExcludingOAuth.foreach(
        //            nvp => {
        //                println("name: " + nvp.getName)
        //            }
        //        )

        val expectedList : List[NameValuePair] = List(
            new NameValuePair("a", "a_value"),
            new NameValuePair("b", "b_value"),
            new NameValuePair("c", "c_value")
        )

        for (i <- 0 to expectedList.length - 1) {
            Assert.assertThat(
                "Sorting of resultant query paramsters should matches expected.",
                parametersExcludingOAuth(i),
                is(equalTo(expectedList(i)))
            )
        }
    }
}
