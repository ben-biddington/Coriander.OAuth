package org.coriander.unit.tests.oauth.core


import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers.JUnitMatchers._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.mockito.Mockito._

import org.coriander.oauth.core.Normalizer
import org.coriander.{NameValuePair, Query}
import org.coriander.oauth.core.Normalizer
import org.coriander.unit.tests.TestBase

class NormalizerTest extends TestBase {

    @Before
    def setUp { }

    @After
    def tearDown { }

    @Test
    def normalize_returns_string_containing_all_supplied_parameters_sorted_by_name {
        val anyQuery = new Query(List(
            new NameValuePair("b", "any-value"),
            new NameValuePair("c", "any-value"),
            new NameValuePair("a", "any-value")
        ))

        val expected = "a=any-value&b=any-value&c=any-value"

        val actual = new Normalizer() normalize(anyQuery)

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    def normalize_url_encodes_each_name_and_value {
        val anyQuery = new Query(List(
            new NameValuePair("b", "any-value"),
            new NameValuePair("c", "any-value-1"),
            new NameValuePair("a", "any-value-2")
        ))

        val mockURLEncoder = newMockURLEncoder

        val actual = new Normalizer(mockURLEncoder) normalize(anyQuery)

        anyQuery foreach(pair => {
            verify(mockURLEncoder, org.mockito.Mockito.times(1)).%%(pair.name)
            verify(mockURLEncoder, org.mockito.Mockito.times(1)).%%(pair.value)
        })
    }

    @Test
    def normalize_includes_parameters_with_no_value {
        val singleParameter = Query.from("b" -> null)

		assertEquals(singleParameter.size, 1)
        
        val expected = "b="

        val actual = new Normalizer() normalize(singleParameter)

        assertThat(actual, is(equalTo(expected)));
    }

    // TEST: Parameters with the same name are sorted by their value

    private def newMockURLEncoder : org.coriander.oauth.core.uri.URLEncoder = {
        var mockedURLEncoder = mock(classOf[org.coriander.oauth.core.uri.URLEncoder])
        
        when(mockedURLEncoder.%%("any-string")).
        thenReturn("stubbed-escaped-value")

        mockedURLEncoder
    }
}