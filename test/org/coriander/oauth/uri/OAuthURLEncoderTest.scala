package org.coriander.oauth.uri.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit._

import org.coriander.oauth.uri.URLEncoder

class OAuthURLEncoderTest extends TestBase {

    // See: http://oauth.net/core/1.0, S 5.1 Parameter Encoding
    // unreserved characters = ALPHA, DIGIT, '-', '.', '_', '~'

    val urlEncoder = new org.coriander.oauth.uri.OAuthURLEncoder

    @Test
    def null_returns_empty {
        val result = urlEncoder %%(null)

        assertEquals("", result)
    }
    
    @Test
    def space_is_percent_encoded {
        val result = urlEncoder %%(" ")

        assertEquals("%20", result)
    }

    @Test
    def tilda_is_not_encoded {
        val result = urlEncoder %%("~")

        assertEquals("~", result)
    }

    @Test
    def period_is_not_encoded {
        val result = urlEncoder %%(".")

        assertEquals(".", result)
    }

    @Test
    def dash_is_not_encoded {
        val result = urlEncoder %%("-")

        assertEquals("-", result)
    }

    @Test
    def underscore_is_not_encoded {
        val result = urlEncoder %%("_")

        assertEquals("_", result)
    }

    @Test
    def digits_are_not_encoded {
        val allDigits = "0123456789"
        val result = urlEncoder %%(allDigits)

        assertEquals(allDigits, result)
    }

    @Test
    def alphabet_characters_are_not_encoded {
        val someAlphabetCharacters = "abc"
        val result = urlEncoder %%(someAlphabetCharacters)

        assertEquals(someAlphabetCharacters, result)
    }

    // TEST: percent encoding is uppercase
}
