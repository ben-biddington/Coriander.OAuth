package org.coriander.oauth.core.signing

abstract class Signature {
	def sign(baseString : String) : String
}