package org.coriander.oauth.core

import collection.mutable.ListBuffer
import org.coriander.{NameValuePair, Query}

class Normalizer {
    def normalize(query : Query) : String = {
        var pairs : ListBuffer[String] = new ListBuffer[String]() 

        query.sort.foreach(nameValuePair => pairs += toString(nameValuePair))

        pairs.toList.mkString("&")
    }

	private def toString(nameValuePair : NameValuePair) : String = 
		nameValuePair.name + "=" + (if (nameValuePair.value != null) nameValuePair.value else "")
}