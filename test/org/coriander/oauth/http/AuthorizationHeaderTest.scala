package org.coriander.oauth.http.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.coriander.oauth._
import org.coriander.oauth.uri._
import org.coriander.oauth.http.AuthorizationHeader

// See: http://oauth.net/core/1.0/#auth_header, S 5.4.1
class AuthorizationHeaderTest extends org.coriander.oauth.tests.TestBase {
    val realm                   = "http://sp.example.com/"
    val oauth_consumer_key      = "0685bd9184jfhq22"
    val oauth_token             = "ad180jjd733klru7"
    val oauth_signature_method  = "HMAC-SHA1"
    val oauth_signature         = "wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D"
    val oauth_timestamp         = "137131200"
    val oauth_nonce             = "4572616e48616d6d65724c61686176"
    val oauth_version           = "1.0"

    @Test
    def result_is_an_authorization_header {
        val value = newAuthorizationHeader.toString

        assertTrue(value.startsWith("Authorization: "));
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
    // TEST: name and value are url encoded
    // TEST: parameters values may be empty
    // TEST: parameters are comma-separated, and whitespace is okay
    // TEST: Realm is optional -- is it always added?
}
