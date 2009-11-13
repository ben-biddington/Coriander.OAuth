package org.coriander.oauth.uri.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit._

import org.coriander.oauth.tests.TestBase
import org.coriander.oauth.uri.{OAuthURLEncoder, URLEncoder}

class OAuthURLEncoderTest extends TestBase {

    // See: http://oauth.net/core/1.0, S 5.1 Parameter Encoding
    // unreserved characters = ALPHA, DIGIT, '-', '.', '_', '~'

    val urlEncoder = new OAuthURLEncoder

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

	@Test
    def an_example_character_not_listed_in_rfc_3986_unreserved_is_escaped {
		// See: http://oauth.net/core/1.0/, 5.1. Parameter Encoding
		// Characters not in the [following] unreserved character set ([RFC3986] section 2.3):
		//
		//     unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
		//
		// MUST be encoded.
		// Seealso: http://www.ietf.org/rfc/rfc3986.txt
		//
		val anyUnreservedCharacter = "'"
		val expectedResult = "%27"
        val actualResult = urlEncoder %%(anyUnreservedCharacter)

        assertEquals(expectedResult, actualResult)
    }

    @Test
	def hexadecimal_characters_in_encodings_are_be_upper_case {
		val result = urlEncoder %%("%=?/:=")

		assertEquals(result, "%25%3D%3F%2F%3A%3D")
	}
}
