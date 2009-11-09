package org.coriander

import scala.collection.mutable.Buffer

class Query(val nameValuePairs : List[NameValuePair]) {
    val urlEncoder = new org.coriander.oauth.uri.OAuthURLEncoder
    val DELIMITER = "&"
    
    def this() {
        this(List())
    }

    def contains(name : String) : Boolean = {
         nameValuePairs.exists(nameValuePair => nameValuePair.name == name )
    }

    def get(name : String) : List[NameValuePair] = {
         nameValuePairs.filter(nameValuePair => nameValuePair.name == name)
    }

    def filter(comparer : NameValuePair => Boolean) : Query = {
        new Query(
             nameValuePairs.filter(comparer)
        )
    }

    def foreach(func : NameValuePair => Unit) {
         nameValuePairs.foreach(func);
    }

    def size : Int = {
        return  nameValuePairs.size
    }

    def sort : Query = {
         new Query(
             nameValuePairs.sort(
                (left, right) => left.name.compareToIgnoreCase(right.name) < 0
            )
        )
    }

    def map(mapper : NameValuePair => NameValuePair) : Query = {
        new Query(nameValuePairs.map(mapper))
    }

    def += (nameValuePair : NameValuePair) : Query = {
        new Query(append(nameValuePair))
    }

    override def toString : String = {
        var result = ""

        nameValuePairs.foreach(pair => {
            result += { if (result != "") DELIMITER else "" }
            result += urlEncode(pair.name) + "=" + urlEncode(pair.value)
        })

        result
    }

    private def append(nameValuePair : NameValuePair) : List[NameValuePair] = {
        val result = nameValuePairs + nameValuePair
        result
    }

    private def urlEncode(value : String) : String = {
        urlEncoder.encode(value)
    }
}

object Query {
    def copy(query : Query) : Query = {
        var result = new Query()

        query.foreach(pair => {
            // TODO: Surely there's a better way
            result = query += pair
        })

        result
    }
}
