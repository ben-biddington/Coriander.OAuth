package org.coriander.oauth

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit._

import org.coriander.oauth.URLEncoder

class URLEncoderTest {

    // TEST: Null argument returns empty
    // TEST: The sequence of characters '%7E' is replaced with '~'

    @Test
    def space_is_percent_encoded() {
        val result = URLEncoder %%(" ")

        Assert assertEquals("%20", result)
    }
}
