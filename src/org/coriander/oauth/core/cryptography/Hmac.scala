package org.coriander.oauth.core.cryptography

abstract class Hmac {
	def create(key : String, message : String) : Array[Byte]
}