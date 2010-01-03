package org.coriander.oauth.core
class Options(val signatureMethod : String, val version : Double)
object Options {
    val DEFAULT = new Options("HMAC-SHA1", 1.0)
}