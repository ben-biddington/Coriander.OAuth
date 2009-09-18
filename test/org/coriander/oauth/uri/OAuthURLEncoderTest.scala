package org.coriander.oauth.uri.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit._

import org.coriander.oauth.uri.URLEncoder

class OAuthURLEncoderTest {

    // See: http://oauth.net/core/1.0, S 5.1 Parameter Encoding
    // unreserved characters = ALPHA, DIGIT, '-', '.', '_', '~'

    // TEST: non-reserved characters are left alone
    val urlEncoder = new org.coriander.oauth.uri.OAuthURLEncoder

    @Test
    def null_returns_empty() {
        val result = urlEncoder %%(null)

        Assert assertEquals("", result)
    }
    
    @Test
    def space_is_percent_encoded() {
        val result = urlEncoder %%(" ")

        Assert assertEquals("%20", result)
    }

    @Test
    def tilda_is_not_encoded() {
        val result = urlEncoder %%("~")

        Assert assertEquals("~", result)
    }

    @Test
    def period_is_not_encoded() {
        val result = urlEncoder %%(".")

        Assert assertEquals(".", result)
    }

    @Test
    def dash_is_not_encoded() {
        val result = urlEncoder %%("-")

        Assert assertEquals("-", result)
    }

    @Test
    def underscore_is_not_encoded() {
        val result = urlEncoder %%("_")

        Assert assertEquals("_", result)
    }

    // TEST: alphabet characters not encoded
    // TEST: numeric characters not encoded
}
