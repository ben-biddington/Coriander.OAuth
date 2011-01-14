/*
Copyright 2011 Ben Biddington

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.coriander.unit.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._

import org.coriander.{Query,NameValuePair}
import java.lang.String

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

        assertThat(actualList, is(equalTo(expectedList)))
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

    @Test
    def size_returns_zero_for_empty_query() {
        assertThat(new Query().size, is(equalTo(0)))
    }

    @Test
    def increment_and_assign_returns_a_new_instance {
        val query = Query.from("a" -> "b")

        val incremented = query += new NameValuePair("c", "d")

        assertTrue(
            "Query should be immutable. The += operation should return a new instance.",
            incremented != query
        )

        assertThat(
            "Expected that an item shoud have been added.",
            incremented.size, is(equalTo(2))
        )
    }

    @Test
    def toString_returns_ampersand_delimited_list {
        val expected = "a=aaa%3F&b=bbb%3A%2F%2F"
        
        val actual = Query(
            NameValuePair().called("a").withValue("aaa?"),
            NameValuePair().called("b").withValue("bbb://")
        ) toString

        assertThat(actual, is(equalTo(expected)))
    }

	@Test { val expected=classOf[RuntimeException] }
	def single_throws_exception_when_name_contains_two_items {
        var name : String = "a"
		val query = Query.from(
			name -> "item",
			name -> "item_1"
		)

		val item = query.single(name);
	}

	@Test
	def given_item_with_name_exists_exactly_once_then_single_returns_it {
		var expectedItem = new NameValuePair("a", "item")

		val query = Query.from(
			"b" -> "item_1",
			expectedItem.name -> expectedItem.value,
			"c" -> "item_2"
		)

		val actualItem = query.single(expectedItem.name);

		assertThat("Mismatched name", actualItem.name, is(equalTo(expectedItem.name)))
		assertThat("Mismatched value", actualItem.value, is(equalTo(expectedItem.value)))
	}
}