package org.coriander

class NameValuePair(val name : String, val value : String) { }

object NameValuePair {
    def apply() : NameValuePairBuilder = new NameValuePairBuilder
    
    def _new : NameValuePairBuilder = {
        new NameValuePairBuilder
    }

    implicit def fromBuilder(builder : NameValuePairBuilder) : NameValuePair = {
        builder.build
    }
}