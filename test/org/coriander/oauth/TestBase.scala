package org.coriander.oauth.tests

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers._
import org.hamcrest.CoreMatchers._
import org.junit._
import org.apache.commons.httpclient._
import org.apache.commons.httpclient.util._
import org.junit.rules._

import scala.collection.immutable._

class TestBase extends Assert {
    protected def trimOAuth(nameValuePairs : List[NameValuePair]) :
        List[NameValuePair] = {
        nameValuePairs.filter(
            nameValuePair =>
            false == nameValuePair.getName.startsWith("oauth_")
        )
    }

    protected def parseQuery(url : java.net.URI) : List[NameValuePair] = {
        parseQuery(url.getQuery)
    }

    protected def parseQuery(url : String) : List[NameValuePair] = {
        val result : java.util.List[_] = new ParameterParser().parse(url, '&')

        result toArray() map((obj) => obj.asInstanceOf[NameValuePair]) toList
    }

    protected def assertContainsName(
        list : List[NameValuePair],
        expected : String
    ) {
        Assert.assertThat(
            String.format(
                "Expected the list to contain " +
                "parameter called '%1$s', but it does not.",
                expected,
            ),
            containsName(list, expected),
            is(true)
        )
    }

    protected def contains(
        list : List[NameValuePair],
        expected : Tuple2[String, String]
    ) : Boolean = {
        val (expectedName, expectedValue) = expected

        list.exists(
            actualnameValuePair => {
                actualnameValuePair.getName == expectedName &&
                actualnameValuePair.getValue == expectedValue
            }
        )
    }

    protected def containsName(
        list : List[NameValuePair],
        expectedName : String
    ) : Boolean = {
        list.exists(
            actualNameValuePair =>
            actualNameValuePair.getName == expectedName
        )
    }
}
