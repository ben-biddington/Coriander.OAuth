package org.coriander.oauth.http.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.coriander.oauth._
import org.coriander.oauth.uri._
import scala.util.matching._

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
    
    val pattern = "^[\\s]*[\\w]+=\"[^\"]+\"[\\s]*$"

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
            assertMatches(pattern, pair)
        })
    }
    
    private def newAuthorizationHeader : AuthorizationHeader = {
        new AuthorizationHeader(
            realm,
            oauth_consumer_key,
            oauth_token,
            oauth_signature_method,
            oauth_signature,
            oauth_timestamp,
            oauth_nonce,
            oauth_version,
            new org.coriander.oauth.uri.OAuthURLEncoder
        )
    }

    // TEST: header value contains just oauth parameters separated by commas
    // TEST: header value contains ALL expected oauth parameters
    // TEST: name and value are url encoded
    // TEST: parameters values may be empty
    // TEST: parameters are comma-separated, and whitespace is okay
    // TEST: Realm is optional -- is it always added?

    private def getHeaderValue(header : String) : String = {
        // TODO: Consider regexp
        val headerName = "Authorization: OAuth"
        val indexOfEndOfHeaderName = header.indexOf(headerName)
        val result = header.substring(indexOfEndOfHeaderName + 1 + headerName.length)
        
        assertTrue("Header value must not be empty", result != "")

        result
    }
}
