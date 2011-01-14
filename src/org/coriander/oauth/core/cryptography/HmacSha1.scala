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

package org.coriander.oauth.core.cryptography

import javax.crypto

class HmacSha1 extends Sha1 {
	def create(key : String, message : String) = hmac(bytes(key), bytes(message))

	private def hmac(key : Array[Byte], message : Array[Byte]) : Array[Byte] = {
        val secret = new crypto.spec.SecretKeySpec(key, algorithm)
		val mac = newMac

		mac.init(secret)
        mac.doFinal(message)
    }

	private def bytes(what : String) = what.getBytes(encoding)

	private def newMac 		= crypto.Mac.getInstance(algorithm)
	private val algorithm 	= "HMacSha1"
    private val encoding 	= org.apache.http.protocol.HTTP.UTF_8
}