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

package org.coriander.unit.tests.oauth.core

import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._

import org.coriander.oauth.core.Normalizer
import org.coriander.unit.tests.TestBase
import org.coriander.{QueryParser, Query}

class NormalizerTest extends TestBase {

    @Test
    def normalize_returns_string_containing_all_supplied_parameters_sorted_by_name {
        val anyQuery = Query.from(
            "b" -> "any-value",
            "c" -> "any-value",
            "a" -> "any-value"
        )

        val expected = "a=any-value&b=any-value&c=any-value"

        val actual = new Normalizer() normalize(anyQuery)

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    def normalize_does_not_url_encode_names_or_values {
        val anyQuery = Query.from("!" -> "/")
		val actual = normalize(anyQuery)

		assertThat(actual, is(equalTo("!=/")))
    }

    @Test
    def normalize_includes_parameters_with_no_value {
        val singleParameter = Query.from("b" -> null)

		assertEquals("Test requires a single parameter.", singleParameter.size, 1)

		val expected = "b="
        val actual = normalize(singleParameter)

        assertThat(actual, is(equalTo(expected)));
    }

	@Test
    def normalize_sorts_parameters_with_the_same_name_by_their_value {
		val twoParametersWithTheSameName = Query.from("a" -> "2", "a" -> "1")
		val expected = "a=1&a=2"
		val actual = normalize(twoParametersWithTheSameName)

		assertThat(actual, is(equalTo(expected)))
	}

	@Test
	def normalize_sorts_parameters_using_lexicographical_byte_value_ordering {
		val parameters = Query.from("z" -> "1", "!" -> "1", "a" -> "1")
		val expected = "!=1&a=1&z=1"
		val actual = normalize(parameters)

		assertThat(actual, is(equalTo(expected)))
	}

	@Test
	def normalize_includes_parameters_with_no_name {
		val parameters = Query.from("" -> "xxx")
		val expected = "=xxx"
		val actual = normalize(parameters)

		assertThat(actual, is(equalTo(expected)))	
	}

	@Test // @see: http://oauth.net/core/1.0/#anchor14
	def example_from_spec {
		val example = Query.from(
			"f" -> "50",
			"z" -> "p",
			"a" -> "1",
			"f" -> "25",
			"f" -> "a",
			"c" -> "hi%20there",
			"z" -> "t"
		)

		val expected = "a=1&c=hi%20there&f=25&f=50&f=a&z=p&z=t"
		val actual = normalize(example)

		assertThat(actual, is(equalTo(expected)))
	}

	private def normalize(query : Query) : String = new Normalizer().normalize(query)
}
