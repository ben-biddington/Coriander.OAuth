package org.coriander.unit.tests


import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers._
import org.hamcrest.CoreMatchers._
import org.junit._

import org.junit.rules._
import scala.util.matching._

import scala.collection.immutable._

import org.coriander.oauth.core.uri._
import org.coriander.Query
import org.coriander.NameValuePair

class TestBase extends Assert {
    val queryParser = new org.coriander.QueryParser

    protected def trimOAuth(query : Query) : Query = {
        query.filter(item => {
            false == item.name.startsWith("oauth_")
        })
    }

    // TODO: Should return list of values (maybe)
    protected def getQueryParameter(url : String, name : String) : String = {
        parseQuery(url).get(name)(0).value
    }
    
    protected def parseQuery(uri : java.net.URI) : Query = {
        parseQuery(uri.getQuery)
    }

    protected def parseQuery(query : String) : Query = {
        queryParser parse(query)
    }

    protected def parseNameValuePairs(value : String, delimiter : String) : Map[String, String] = {
        var result : Map[String, String] = Map()

        value.split(delimiter).foreach((pair : String) => {
            val parts = pair.split("=");
            result += urlDecode(parts(0).trim) -> {
                if (parts.length ==1) null else urlDecode(parts(1).trim)
            }
        });

        result
    }

    protected def urlEncode(str : String) : String = {
        new OAuthURLEncoder().encode(str)
    }

    protected def urlDecode(str : String) : String = {
        java.net.URLDecoder.decode(str, "UTF-8")
    }

    protected def assertContainsName(
        list : Map[String, String],
        expectedNames : String*
    ) {
        expectedNames foreach(expectedName => {
            assertThat(
                String.format(
                    "Expected the list to contain " +
                    "parameter called <%1$s>, but it does not.",
                    expectedName
                ),
                list.keys.contains(expectedName),
                is(true)
            )
        })
    }

    protected def assertContainsAllNames(
        expectedList : Map[String, String],
        actualList : Map[String, String]
    ) {
        assertContainsAll(expectedList, actualList, (expected, actual) => {
            actual._1 == expected._1
        })
    }

    // DEPRECATED
    protected def assertContainsAll(
        expectedList : Map[String, String],
        actualList : Map[String, String]
    ) {
        assertContainsAll(expectedList, actualList, (expected, actual) => {
            actual._1   == expected._1 &&
            actual._2   == expected._2
        })
    }

    protected def assertContainsAll(expected: Query, actual : Query) {
        expected.foreach(pair => {
            assertTrue(
                "The query <" + actual.toString + "> should " +
                "contain the parameter with name <" + pair.name + ">", 
                actual.contains(pair.name)
            )
        })
    }
    
    private def assertContainsAll(
        expected : Map[String, String],
        actual : Map[String, String],
        comparer: (=> Tuple2[String, String], Tuple2[String, String]) => Boolean
    ) {        
        expected foreach(pair => {
            assertTrue(
                "The actual list did not contain the expected item: " +
                "<" + pair._1 + "=" + pair._2 +">",
                actual.exists(
                   actualPair => {
                       comparer(pair, actualPair)
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

    protected def assertAreEqual(expected : Query, actual : Query) {
        assertThat("The supplied query has unexpected size.", actual.size, is(equalTo(actual.size)))

        expected.foreach(pair => {
            assertTrue("The pair with name <" + pair.name + "> is missing.", actual.contains(pair.name))
            assertAreEqual_rename_me(expected.get(pair.name), actual.get(pair.name))
        })
    }

    protected def assertAreEqual_rename_me(expected : List[NameValuePair], actual : List[NameValuePair]) {
        for (i <- 0 until expected.length - 1) {
            assertThat(expected(i).name, is(equalTo(actual(i).name)))
            assertThat(expected(i).value, is(equalTo(actual(i).value)))
        }
    }

	// Example:
	// times(2) { println("Block me.")}
	protected def times(count : Int)(block : => Unit) = {
		1.to(count).foreach((_) => block)
	}
}
