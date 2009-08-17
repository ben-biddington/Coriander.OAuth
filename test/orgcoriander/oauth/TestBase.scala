package org.coriander.tests.oauth

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

class TestBase {
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

        val nameValuePairs : Array[Object] = result.toArray();

        val listResult : Array[NameValuePair] = nameValuePairs.map(
            (obj : Object) => obj.asInstanceOf[NameValuePair]
        )

        listResult.toList
    }
}
