/*
Copyright 2011 Ben Biddington

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.coriander.unit.tests.oauth.core.http

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.Assert._
import scala.util.matching._
import org.mockito.Mockito._

import org.coriander.oauth._
import core.{Options, CredentialSet, Credential}
import org.coriander.oauth.core.uri._
import org.coriander.oauth.core.http.AuthorizationHeader
import java.net.URI
import org.coriander.QueryParser
import org.coriander.unit.tests.TestBase

// See: http://oauth.net/core/1.0, S 5.4.1
class AuthorizationHeaderTest extends TestBase {
    val realm                   = "http://sp.example.com/"
    val oauth_consumer_key      = "0685bd9184jfhq22"
    val oauth_token             = "ad180jjd733klru7"
    val oauth_signature_method  = "HMAC-SHA1"
    val oauth_signature         = "wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D"
    val oauth_timestamp         = "137131200"
    val oauth_nonce             = "4572616e48616d6d65724c61686176"
    val oauth_version           = "1.0"

    val credentials =  new CredentialSet(
        new Credential(oauth_consumer_key, ""),
        new Credential(oauth_token, "")
    )

    val options = Options.DEFAULT

    val linearWhitespace        = "[\t| ]"
    val anyLinearWhitespace     = linearWhitespace + "*"

    val requiredParameters = List(
        "realm", "oauth_consumer_key",
        "oauth_signature_method", "oauth_signature",
        "oauth_timestamp", "oauth_version"
    )
                          
    // [!] linear whitespace is either <space> or <horizontal tab>
    // [linear_whitespace]name="[value|empty]"[linear_whitespace]
    val nameEqualsAnyQuotedString = "[\\w]+=\"[^\"]+\""

	val urlEncoder = new OAuthUrlEncoder

    @Test
    def result_is_an_authorization_header {
        val value = newAuthorizationHeader toString

        assertEquals("Authorization", newAuthorizationHeader.name)
    }

    @Test
    def value_has_oauth_specifier {
        val value = newAuthorizationHeader toString
        val expected = "OAuth"
        val headerValue = newAuthorizationHeader value

        assertTrue(
            "The value part <" + headerValue + "> should start with <" + expected + ">",
            headerValue.startsWith(expected)
        )
    }

    @Test
    def value_contains_individual_name_value_pairs_formatted_correctly {
        val value = getHeaderValue(newAuthorizationHeader)

        val nameValuePairs : Array[String] = value.split(",");

        nameValuePairs foreach(pair =>
            assertMatches(createNameValuePairPattern, pair)
        )
    }

     @Test
    def value_contains_all_expected_oauth_parameters {
        val all = parseHeaderValue(
            getHeaderValue(newAuthorizationHeader)
        );
        
        assertContainsName(
            all,
            "realm", "oauth_consumer_key", "oauth_signature_method",
            "oauth_signature", "oauth_timestamp", "oauth_version"
        )
    }

    @Test
    def each_value_excluding_realm_is_url_encoded_during_toString {
        val mockURLEncoder = newMockURLEncoder

        val header = newAuthorizationHeader(mockURLEncoder) toString
		
        verify(mockURLEncoder, org.mockito.Mockito.times(0)).%%(realm)
        verify(mockURLEncoder, org.mockito.Mockito.times(1)).%%(oauth_consumer_key)
        verify(mockURLEncoder, org.mockito.Mockito.times(1)).%%(oauth_signature_method)
        verify(mockURLEncoder, org.mockito.Mockito.times(1)).%%(oauth_signature)
        verify(mockURLEncoder, org.mockito.Mockito.times(1)).%%(oauth_timestamp)
        verify(mockURLEncoder, org.mockito.Mockito.times(1)).%%(oauth_nonce)
        verify(mockURLEncoder, org.mockito.Mockito.times(1)).%%(oauth_version)
    }

    @Test { val expected = classOf[Exception] }
    def url_encoder_must_be_supplied_otherwise_toString_throws_exception {
        newAuthorizationHeader(null) toString
    }

	// See: http://oauth.net/core/1.0a#RFC2617, Appendix A.5.1. Generating Signature Base String
	@Test
	def example {
		val signature = "tR3+Ty81lMeYAr/Fid0kMTYa/WM="
        val expectedHeaderValue = 
			"Authorization: OAuth realm=\"http://photos.example.net/\"," +
			"oauth_consumer_key=\"dpf43f3p2l4k3l03\"," +
			"oauth_token=\"nnch734d00sl2jdk\"," +
			"oauth_signature_method=\"HMAC-SHA1\"," +
			"oauth_signature=\"tR3%2BTy81lMeYAr%2FFid0kMTYa%2FWM%3D\"," +
			"oauth_timestamp=\"1191242096\"," +
			"oauth_nonce=\"kllo9940pd9333jh\"," +
			"oauth_version=\"1.0\""

		val timestamp = "1191242096"
		val nonce = "kllo9940pd9333jh"
		val version = "1.0"
		val realm = "http://photos.example.net/"
		val consumer = new Credential("dpf43f3p2l4k3l03", "kd94hf93k423kf44")
		val token = new Credential("nnch734d00sl2jdk", "pfkkdhi9sl3r4s00")

		val header = new AuthorizationHeader(
			realm,
			new CredentialSet(consumer, token),
			signature,
			timestamp,
			nonce,
			options,
			urlEncoder
		)

		assertThat(
			header toString,
			is(equalTo(expectedHeaderValue))
		)
	}

    private def newAuthorizationHeader : AuthorizationHeader = {
        newAuthorizationHeader(new OAuthUrlEncoder)
    }

    private def newAuthorizationHeader(urlEncoder : UrlEncoder) :
        AuthorizationHeader = {
        new AuthorizationHeader(
            realm,
            credentials,
            oauth_signature,
            oauth_timestamp,
            oauth_nonce,
            options,
            urlEncoder
        )
    }

    private def newMockURLEncoder : UrlEncoder = {
        var mockedURLEncoder = mock(classOf[UrlEncoder])
        when(mockedURLEncoder.%%("any-string")).thenReturn("stubbed-escaped-value")
        
        mockedURLEncoder
    }

    // TEST: Header value contains just oauth parameters separated by commas
    // TEST: Parameter values may be empty
    // TEST: Parameters are comma-separated, and whitespace is okay
    // TEST: Realm is optional -- is it always added?

    private def createNameValuePairPattern : String = {
        "^" + anyLinearWhitespace + nameEqualsAnyQuotedString + anyLinearWhitespace + "$"
    }

    protected def parseHeaderValue(headerValue : String) : Map[String, String] = {
        parseNameValuePairs(headerValue, ",")
    }

    private def getHeaderValue(header : AuthorizationHeader) : String = {
        val headerName = "OAuth"
        val indexOfEndOfHeaderName = header.value.indexOf(headerName)
        val result = header.value.substring(indexOfEndOfHeaderName + 1 + headerName.length)

        assertTrue("Header value must not be empty", result != "")

        result
    }
}