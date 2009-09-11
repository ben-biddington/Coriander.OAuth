package org.coriander.oauth

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit._

import org.coriander.oauth.URLEncoder

class URLEncoderTest {

    // TEST: non-reserved characters are left alone

    @Test
    def null_returns_empty() {
        val result = URLEncoder %%(null)

        Assert assertEquals("", result)
    }
    
    @Test
    def space_is_percent_encoded() {
        val result = URLEncoder %%(" ")

        Assert assertEquals("%20", result)
    }

    @Test
    def tilda_is_not_encoded() {
        val result = URLEncoder %%("~")

        Assert assertEquals("~", result)
    }
}
