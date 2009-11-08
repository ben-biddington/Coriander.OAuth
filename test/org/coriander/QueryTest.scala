package org.coriander.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.matchers.JUnitMatchers._

import org.coriander.Query
import org.coriander.NameValuePair
import org.coriander.oauth.tests.TestBase

class QueryTest extends TestBase {

    val AN_EMPTY_LIST : List[NameValuePair] = List()


    @Test
    def given_duplicates_then_get_returns_them_all {
        val key = "a"

        val expectedList = List(
            new NameValuePair(key, "zzz"),
            new NameValuePair(key, "yyy")
        )

        val actualList = new Query(expectedList) get(key)

        assertThat(actualList, is(equalTo(actualList)))
    }

    @Test
    def given_default_instance_then_get_returns_empty_list() {
        assertThat(
            new Query().get("xxx"), is(equalTo(AN_EMPTY_LIST))
        )
    }

    @Test
    def contains_returns_false_when_parameter_does_not_exist_with_supplied_name() {
        assertThat(
            new Query().contains("xxx"), is(false)
        )
    }

    @Test
    def contains_returns_true_when_parameter_exists_with_supplied_name() {
        val queryStringContainingOneItem = new Query(
            List(new NameValuePair("xxx", null))
        )
        
        assertThat(
            queryStringContainingOneItem.contains("xxx"), is(true)
        )
    }
}