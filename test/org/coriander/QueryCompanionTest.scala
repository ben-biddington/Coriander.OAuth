package org.coriander.oauth

import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.matchers.JUnitMatchers._

import org.coriander.oauth.tests.TestBase

import org.coriander.Query
import org.coriander.NameValuePair

class QueryCompanionTest extends TestBase {

	@Test
    def apply_splat_returns_as_expected {
        val actual = Query(new NameValuePair("x", "y"))

        assertThat(
            "Empty splat should be converted to empty query.",
            actual.size, is(equalTo(1))
        )
    }

    @Test
    def apply_splat_returns_empty_when_splat_is_empty {
        val actual = Query()

        assertThat(
            "Splat should be converted to query with correct number of entries.",
            actual.size, is(equalTo(0))
        )
    }

    @Test
    def from_tuple_splat_returns_empty_when_map_argument_is_empty {
        val actual = Query.from()

        assertThat(
            "Empty map should be converted to empty query.",
            actual.size, is(equalTo(0))
        )
    }

    @Test
    def from_tuple_splat_returns_as_expected {
        val actual = Query.from("a" -> "aaa", "b" -> "bbb")

        assertThat(
            "Map should be converted to query with correct number of entries.",
            actual.size, is(equalTo(2))
        )
    }

    @Test { val expected=classOf[Exception] }
    def given_a_null_reference_then_copy_throws_argument_exception {
        Query.copy(null)
    }
}