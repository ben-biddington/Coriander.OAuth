package org.coriander.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.matchers.JUnitMatchers._

import org.coriander.QueryParser

class QueryParserTest {

    var result : Map[String, String] = null

    @Before
    def setUp { }

    @After
    def tearDown { }

    @Test
    def parse_returns_empty_when_query_argument_is_empty {
        when_string_parsed("")

        then_result_equals(Map())
    }

    @Test
    def parse_single_parameter_returns_as_expected {
        val value = "a=any-value"

        when_string_parsed(value)

        then_result_equals(Map("a" -> "any-value"))
    }

    @Test
    def parse_multiple_parameters_returns_as_expected {
        val value = "a=any-value&b=any-value-1"
       
        when_string_parsed(value)

        then_result_equals(Map(
            "a" -> "any-value",
            "b" -> "any-value-1"
        ))
    }

    @Test
    def parse_ignores_parameters_with_no_name() {
        val value = "=a-value-with-no-name"

        when_string_parsed(value)

        then_result_equals(Map())
    }

    @Test
    def parse_collects_parameters_with_the_same_name() {
        val value = "a=value&a=value-1"

        when_string_parsed(value)

        then_result_equals(Map(
            "a" -> "value,value-1"
            ))
    }

    private def when_string_parsed(query : String) {
        result = new QueryParser().parse(query)
    }

    private def then_result_equals(expected : Map[String, String]) {
        then_result_has_length(expected.size)
        assertEquals(expected, result)
    }
    
    private def then_result_has_length(expectedCount : Int) {
        assertThat(
            "Expected " + expectedCount + " results.",
            result.size,
            is(equalTo(expectedCount))
        )
    }
}
