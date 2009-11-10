package org.coriander

class NameValuePairBuilder() {
    var name : String = ""
    var value : String = ""

    def called(name : String) : NameValuePairBuilder = {
        this.name = name
        this
    }

    def withValue(value : String) : NameValuePairBuilder = {
        this.value = value
        this
    }

    def build : NameValuePair = {
         new NameValuePair(name, value)
    }
}
