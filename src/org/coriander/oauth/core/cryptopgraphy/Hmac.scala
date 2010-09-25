package org.coriander.oauth.core.cryptopgraphy

abstract class Hmac {
	def create(key : String, message : String) : Array[Byte]
}