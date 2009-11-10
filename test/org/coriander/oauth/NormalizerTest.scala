package org.coriander.oauth.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers.JUnitMatchers._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.mockito.Mockito._

import org.coriander.oauth.Normalizer

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
            verify(mockURLEncoder, times(1)).%%(pair.name)
            verify(mockURLEncoder, times(1)).%%(pair.value)
        })
    }

    @Test
    def normalize_includes_parameters_with_no_value {
        val singleParameter = new Query(List(
            new NameValuePair("b", null)
        ))
        
        val expected = "b="

        val actual = new Normalizer() normalize(singleParameter)

        assertThat(actual, is(equalTo(expected)));
    }

    // TEST: How do we handle parameters with the same name?

    private def newMockURLEncoder : org.coriander.oauth.uri.URLEncoder = {
        var mockedURLEncoder = mock(classOf[org.coriander.oauth.uri.URLEncoder])
        
        when(mockedURLEncoder.%%("any-string")).
        thenReturn("stubbed-escaped-value")

        mockedURLEncoder
    }
}
