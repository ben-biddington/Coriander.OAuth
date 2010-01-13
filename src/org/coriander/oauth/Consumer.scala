package org.coriander.oauth

import core.nonce.{NonceFactory, SystemNonceFactory}
import core.timestamp.{TimestampFactory, SystemTimestampFactory}
import core.{SignedUri, CredentialSet, Credential}
import java.net.URI

class Consumer(
	credential : Credential,
	timestampFactory : TimestampFactory,
	nonceFactory : NonceFactory
) {
	def this(credential : Credential) = this(
		credential,
		new SystemTimestampFactory(),
		new SystemNonceFactory()
	)

	def sign(uri : URI) = {
		new SignedUri(
			uri,
			new CredentialSet(credential),
			timestampFactory.newTimestamp,
			nonceFactory.newNonce
		).value
	}
}