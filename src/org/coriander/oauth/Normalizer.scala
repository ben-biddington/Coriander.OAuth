package org.coriander.oauth

import org.coriander.oauth.uri._
import org.coriander.Query

class Normalizer(val urlEncoder : URLEncoder) {
    def this() {
        this(new OAuthURLEncoder())
    }

    def normalize(query : Query) : String = {
        var pairs : List[String] = List()

        query.sort.foreach(nameValuePair =>
            pairs += urlEncoder.%%(nameValuePair.name) + "=" + urlEncoder.%%(nameValuePair.value)
        )

        pairs.mkString("&")
    }
}
