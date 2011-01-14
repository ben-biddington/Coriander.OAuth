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