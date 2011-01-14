/*
Copyright 2011 Ben Biddington

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.coriander

import collection.mutable.ListBuffer
import oauth.core.uri.OAuthUrlEncoder

class Query(val nameValuePairs : List[NameValuePair]) {
    val urlEncoder 	= new OAuthUrlEncoder
    val DELIMITER 	= "&"
    
    def this() { this(List()) }

    def contains(name : String) : Boolean =
         nameValuePairs.exists(nameValuePair => nameValuePair.name == name)

	def get(name : String) : List[NameValuePair] =
         nameValuePairs.filter(nameValuePair => nameValuePair.name == name)
	
    def filter(comparer : NameValuePair => Boolean) : Query =
        new Query(nameValuePairs.filter(comparer))

    def foreach(func : NameValuePair => Unit) = nameValuePairs.foreach(func);

    def size : Int = nameValuePairs.size

    def map(mapper : NameValuePair => NameValuePair) : Query =
		new Query(nameValuePairs.map(mapper))

	def single(name : String) : NameValuePair = {
		val allMatching = get(name)

		if (allMatching.size > 1)
			throw new RuntimeException(
				"Too many items found, expected <1>, actual <" + allMatching.size + ">"
			)

		allMatching.head
    }

    def sort : Query = {
		new Query(
			nameValuePairs.sort((left, right) => {
				val nameCompared = left.name.compareToIgnoreCase(right.name)

				if (nameCompared != 0)
					nameCompared < 0
				else
					left.value.compareToIgnoreCase(right.value) < 0
			})
		)
	}

	def += (nameValuePair : Tuple2[String,String]) : Query = {
		val pair = new NameValuePair(nameValuePair._1, nameValuePair._2)
		new Query(append(pair))
	}
	
    def += (nameValuePair : NameValuePair) : Query = new Query(append(nameValuePair))

    override def toString : String = {
        var result : StringBuffer = new StringBuffer()

        nameValuePairs.foreach(pair => {
            result.append(if (result.length > 0) DELIMITER else "")
            result.append(urlEncode(pair.name) + "=" + urlEncode(pair.value))
        })

        result toString
    }

    private def append(nameValuePair : NameValuePair) : List[NameValuePair] =
		nameValuePairs ::: List(nameValuePair)

    private def urlEncode(value : String) : String = urlEncoder.encode(value)
}

object Query {
    def apply[NameValuePair](pairs : org.coriander.NameValuePair*) : Query = {
        new Query(pairs.toList)
    }

    def from(tupleSplat : Tuple2[String, String]*) : Query = {

        var temp : ListBuffer[NameValuePair] = new ListBuffer[NameValuePair]()
        
        tupleSplat.foreach(tuple => {
            val (name, value) = tuple

            temp += new NameValuePair(name, value)
        })

        new Query(temp toList)
    }
    
    def copy(query : Query) : Query = {
        var temp : ListBuffer[NameValuePair] = new ListBuffer[NameValuePair]()

        query foreach(pair => temp += new NameValuePair(pair.name, pair.value))

        new Query(temp toList)
    }
}
