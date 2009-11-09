package org.coriander.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.matchers.JUnitMatchers._

import org.coriander.oauth.tests.TestBase

import org.coriander.QueryParser
import org.coriander.Query
import org.coriander.NameValuePair

class QueryParserTest extends TestBase {

    var result : Query = null

    @Before
    def setUp { }

    @After
    def tearDown { }

    @Test
    def parse_returns_empty_when_query_argument_is_empty {
        when_string_parsed("")

        then_result_equals(new Query())
    }

    @Test
    def parse_single_parameter_returns_as_expected {
        val value = "a=any-value"

        when_string_parsed(value)

        then_result_equals(Query.from("a" -> "any-value"))
    }

    @Test
    def parse_multiple_parameters_returns_as_expected {
        val value = "a=any-value&b=any-value-1"

        val expectedQuery = Query.from(
            "a" -> "any-value",
            "b" -> "any-value-1"
        )

        when_string_parsed(value)

        then_result_equals(expectedQuery)
    }

    @Test
    def parse_ignores_parameters_with_no_name() {
        val value = "=a-value-with-no-name"

        when_string_parsed(value)

        then_result_equals(new Query())
    }

    @Test
    def parse_preserves_parameters_with_the_same_name() {
        val value = "a=value&a=value-1&a=value-2"

        when_string_parsed(value)

        val expected = Query.from(
            "a" -> "value",
            "a" -> "value-1",
            "a" -> "value-2"
        )

        then_result_equals(expected)
    }

    private def when_string_parsed(query : String) {
        result = new QueryParser().parse(query)
    }

    private def then_result_equals(expected : Query) {
        then_result_has_length(expected.size)

        expected.foreach(nameValuePair => {
            val name = nameValuePair.name

            assertTrue(
                "The result does not contain parameter with name " +
                "<" + name + ">",
                result.contains(name)
            )

            val expectedValues = expected.get(name)
            val actualValues = result.get(name)
            
            assertAreEqual_rename_me(expectedValues, actualValues)
        })
    }
    
    private def then_result_has_length(expectedCount : Int) {
        assertThat(
            "Expected " + expectedCount + " results.",
            result.size,
            is(equalTo(expectedCount))
        )
    }
}
