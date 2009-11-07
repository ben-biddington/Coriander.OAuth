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
import scala.util.matching._

import scala.collection.immutable._

class TestBase extends Assert {
    val queryParser = new org.coriander.QueryParser
    
    protected def trimOAuth(nameValuePairs : List[NameValuePair]) :
        List[NameValuePair] = {
        nameValuePairs.filter(
            nameValuePair =>
            false == nameValuePair.getName.startsWith("oauth_")
        )
    }

    protected def getQueryParameter(url : String, name : String) : String = {
        parseQuery(url).
            find(item => item.getName() == name).
            get.getValue;
    }
    
    protected def parseQuery(uri : java.net.URI) : List[NameValuePair] = {
        parseQuery(uri.getQuery)
    }

    protected def parseQuery(query : String) : List[NameValuePair] = {
        queryParser parse(query)
    }

    protected def parseNameValuePairs(value : String, delimiter : String) : List[NameValuePair] = {
        var result : List[NameValuePair] = List[NameValuePair]()

        value.split(delimiter).foreach((pair : String) => {
            val parts = pair.split("=");
            result = new NameValuePair(parts(0) trim, parts(1) trim) :: result // TODO: This is prepending!
        });

        result.reverse; // TODO: Pretty naff
    }

    protected def urlDecode(str : String) : String = {
        java.net.URLDecoder.decode(str, "UTF-8")
    }

    protected def assertContainsName(
        list : List[NameValuePair],
        expectedNames : String*
    ) {
        expectedNames foreach(expectedName => {
            assertThat(
                String.format(
                    "Expected the list to contain " +
                    "parameter called <%1$s>, but it does not.",
                    expectedName,
                ),
                containsName(list, expectedName),
                is(true)
            )
        })
    }

    protected def assertContainsAllNames(
        expectedList : List[NameValuePair],
        actualList : List[NameValuePair]
    ) {
        assertContainsAll(expectedList, actualList, (expected, actual) => {
            actual.getName     == expected.getName
        })
    }

    protected def assertContainsAll(
        expectedList : List[NameValuePair],
        actualList : List[NameValuePair]
    ) {
        assertContainsAll(expectedList, actualList, (expected, actual) => {
            actual.getName     == expected.getName &&
            actual.getValue    == expected.getValue
        })
    }
    
    private def assertContainsAll(
        expected : List[NameValuePair],
        actual : List[NameValuePair],
        comparer: (=> NameValuePair, NameValuePair) => Boolean
    ) {

        expected foreach(expectedPair => {
            assertTrue(
                "The actual list did not contain the expected item: " +
                "<" + expectedPair.getName + "=" + expectedPair.getValue +">",
                actual.exists(
                   nvp => {
                       comparer(expectedPair, nvp)
                   }
                )
            )
        })
    }

    // [!] Tries to match target (whole match) and returns the matches.
    protected def assertMatches(pattern : String, value : String) {
        val hasMatch = false == (new Regex(pattern).unapplySeq(value).isEmpty)

        assertTrue(
            "The supplied value <" + value + "> " +
            "does not match pattern <" + pattern +">", 
            hasMatch
        )
    }

     protected def assertStartsWith(pattern : String, value : String) {
        val hasMatch = false == (new Regex(pattern).findPrefixOf(value).isEmpty)

        assertTrue(
            "The supplied value <" + value + "> " +
            "does not start with <" + pattern +">",
            hasMatch
        )
    }

    protected def contains(
        list : List[NameValuePair],
        expected : Tuple2[String, String]
    ) : Boolean = {
        val (expectedName, expectedValue) = expected

        list exists(
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
