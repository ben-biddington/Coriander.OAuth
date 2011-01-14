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

import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit.matchers.JUnitMatchers._

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

	@Test def can_also_make_one_with_tuple {
		var query = new Query()
		query = query += ("a", "aaa")

		assertThat(query.size, is(equalTo(1)))
		assertThat(query.get("a").first.value, is(equalTo("aaa")))
	}
}