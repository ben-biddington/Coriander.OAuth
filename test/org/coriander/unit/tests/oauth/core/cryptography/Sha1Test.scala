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

package org.coriander.unit.tests.oauth.core.cryptography

import org.junit._
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.apache.commons.codec.binary.Base64.encodeBase64
import org.coriander.oauth.core.cryptography.HmacSha1

class Sha1Test {
	@Test
	def an_example_using_oauth_consumer_secret_only {
		val key 	= "secret&"
		val message = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26" +
			"oauth_nonce%3Df3df23228e40e2905e305a893895f115%26oauth_signature_method%3DHMAC-SHA1%26" +
			"oauth_timestamp%3D1252657173%26oauth_version%3D1.0"

		val expected 	= "2/MMtvuImh4H+clAdThQWk916lo="
        val actual 		= mac(key, message)

		assertEquals(expected, actual)
	}

	@Test
	def an_example_using_oauth_consumer_secret_and_token_secret {
		val key 	= "secret&token_secret"
		val message = "GET&http%3A%2F%2Fxxx%2F&oauth_consumer_key%3Dkey%26" +
			"oauth_nonce%3D35d48708b951a3adfaa64a9d0632e19a%26oauth_signature_method%3DHMAC-SHA1%26" +
			"oauth_timestamp%3D1252669302%26oauth_token%3Dtoken_key%26oauth_version%3D1.0"


		val expected 	= "a6D1JJVdKIxKe7L/AW+gtSzBT24="
		val actual 		= mac(key, message)

		assertEquals(expected, actual)
	}

	private def mac(key : String, message : String) = {
		val theMac = new HmacSha1().create(key, message)
		new String(encodeBase64(theMac))		
	}
}