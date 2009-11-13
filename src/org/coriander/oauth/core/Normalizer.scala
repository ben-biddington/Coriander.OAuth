package org.coriander.oauth.core


import org.coriander.oauth.uri._
import collection.mutable.ListBuffer

import org.coriander.{NameValuePair, Query}

class Normalizer(val urlEncoder : URLEncoder) {
    def this() {
        this(new OAuthURLEncoder())
    }

    def normalize(query : Query) : String = {
        var pairs : ListBuffer[String] = new ListBuffer[String]() 

        query.sort.foreach(nameValuePair => pairs += toString(nameValuePair))

        pairs.toList.mkString("&")
    }

	private def toString(nameValuePair : NameValuePair) : String = {
		urlEncoder.%%(nameValuePair.name) + "=" + urlEncoder.%%(nameValuePair.value)
	}
}