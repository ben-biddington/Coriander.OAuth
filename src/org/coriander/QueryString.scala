package org.coriander

class QueryString(val nameValuePairs : List[NameValuePair]) {
    def this() {
        this(List())
    }

    def contains(name : String) : Boolean = {
        nameValuePairs.
            exists(nameValuePair => nameValuePair.name == name )
    }

    def get(name : String) : List[NameValuePair] = {
        nameValuePairs.
            filter(nameValuePair => nameValuePair.name == name)
    }
}
