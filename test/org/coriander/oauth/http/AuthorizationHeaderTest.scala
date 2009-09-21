package org.coriander.oauth.http.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.coriander.oauth._
import org.coriander.oauth.uri._
import scala.util.matching._

import org.mockito.Mockito._

import org.coriander.oauth.http.AuthorizationHeader

// See: http://oauth.net/core/1.0, S 5.4.1
class AuthorizationHeaderTest extends org.coriander.oauth.tests.TestBase {
    val realm                   = "http://sp.example.com/"
    val oauth_consumer_key      = "0685bd9184jfhq22"
    val oauth_token             = "ad180jjd733klru7"
    val oauth_signature_method  = "HMAC-SHA1"
    val oauth_signature         = "wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D"
    val oauth_timestamp         = "137131200"
    val oauth_nonce             = "4572616e48616d6d65724c61686176"
    val oauth_version           = "1.0"

    val linearWhitespace        = "[\t| ]"

    // [!] linear whitespace is either <space> or <horizontal tab>
    // [linear_whitespace]name="[value|empty]"[linear_whitespace]
    val nameEqualsAnyQuotedString = "[\\w]+=\"[^\"]+\""

    @Test
    def result_is_an_authorization_header {
        val value = newAuthorizationHeader toString

        assertTrue(value.startsWith("Authorization: OAuth"));
    }

    @Test
    def result_contains_individual_name_value_pairs_formatted_correctly {
        val value = getHeaderValue(newAuthorizationHeader toString)

        val nameValuePairs : Array[String] = value.split(",");

        nameValuePairs foreach(pair => {
            assertMatches(createNameValuePairPattern, pair)
        })
    }

    @Test
    def each_value_is_url_encoded_during_toString {
        val mockURLEncoder = newMockURLEncoder

        val header = newAuthorizationHeader(mockURLEncoder) toString

        // TODO: Consider NameValuePairs
        verify(mockURLEncoder, times(1)).%%(realm)
        verify(mockURLEncoder, times(1)).%%(oauth_consumer_key)
        verify(mockURLEncoder, times(1)).%%(oauth_signature_method)
        verify(mockURLEncoder, times(1)).%%(oauth_signature)
        verify(mockURLEncoder, times(1)).%%(oauth_timestamp)
        verify(mockURLEncoder, times(1)).%%(oauth_nonce)
        verify(mockURLEncoder, times(1)).%%(oauth_version)
    }

    private def newAuthorizationHeader : AuthorizationHeader = {
        newAuthorizationHeader(new org.coriander.oauth.uri.OAuthURLEncoder)
    }

     private def newAuthorizationHeader(urlEncoder : org.coriander.oauth.uri.URLEncoder) :
        AuthorizationHeader = {
        new AuthorizationHeader(
            realm,
            oauth_consumer_key,
            oauth_token,
            oauth_signature_method,
            oauth_signature,
            oauth_timestamp,
            oauth_nonce,
            oauth_version,
            urlEncoder
        )
    }

    private def newMockURLEncoder : org.coriander.oauth.uri.URLEncoder = {
        var mockedURLEncoder = mock(classOf[org.coriander.oauth.uri.URLEncoder])
        when(mockedURLEncoder.%%("any-string")).thenReturn("stubbed-escaped-value")
        
        mockedURLEncoder
    }

    // TEST: Header value contains just oauth parameters separated by commas
    // TEST: Header value contains ALL expected oauth parameters
    // TEST: Parameter values may be empty
    // TEST: Parameters are comma-separated, and whitespace is okay
    // TEST: Realm is optional -- is it always added?
    // TEST: URLEncoder is required, otherwise an exception results.

    private def getHeaderValue(header : String) : String = {
        // TODO: Consider regexp
        val headerName = "Authorization: OAuth"
        val indexOfEndOfHeaderName = header.indexOf(headerName)
        val result = header.substring(indexOfEndOfHeaderName + 1 + headerName.length)

        println(result toString)

        assertTrue("Header value must not be empty", result != "")

        result
    }

    private def createNameValuePairPattern : String = {
        val anyLinearWhitespace = linearWhitespace + "*"
         "^" + anyLinearWhitespace + nameEqualsAnyQuotedString + anyLinearWhitespace + "$"
    }
}