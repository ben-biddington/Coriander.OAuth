package org.coriander.oauth

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit._

import org.coriander.oauth.uri.URLEncoder

class OAuthURLEncoderTest {

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
}
