package org.coriander.oauth

import org.coriander.oauth.uri._
import scala.collection.immutable._

class Normalizer(val urlEncoder : URLEncoder) {
    def this() {
        this(new OAuthURLEncoder())
    }

    // TODO: Does this make sense? It's sorting the list and joining the
    // result into an ampersand-delimited string
    def normalize(params : Map[String, String]) : String = {
        sort(params) map {
            case (name, value) => { urlEncoder.%%(name) + "=" + urlEncoder.%%(value) }
        } mkString "&"
    }
    
    def sort(queryParams : Map[String, String]) : SortedMap[String, String] = {
        return new TreeMap[String, String] ++ queryParams
    }
}
