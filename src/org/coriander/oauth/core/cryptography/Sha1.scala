package org.coriander.oauth.core.cryptography

abstract class Sha1 {
	def create(key : String, message : String) : Array[Byte]
}