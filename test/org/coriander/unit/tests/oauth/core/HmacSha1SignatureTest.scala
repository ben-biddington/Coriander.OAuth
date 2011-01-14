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

package org.coriander.unit.tests.oauth.core

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.matchers._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._
import org.junit._
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.apache.commons.httpclient._
import org.apache.commons.httpclient.util._
import org.junit.rules._
import scala.collection.immutable._
import org.coriander.oauth._
import core.cryptography.Sha1
import core.cryptography.signing.HmacSha1Signature
import core.uri.{UrlEncoder, OAuthUrlEncoder}
import core.{CredentialSet, Credential}
import CredentialSet._
import org.coriander.unit.tests.TestBase

// See: http://www.infoq.com/news/2009/07/junit-4.7-rules#
// For signature examples, see: http://term.ie/oauth/example/client.php
class HmacSha1SignatureTest extends TestBase {
    val validConsumerCredential 	= new Credential("key", "secret")
    val validToken 					= new Credential("token_key", "token_secret")
    var urlEncoder 	: UrlEncoder 	= null
    var hmac 		: Sha1 			= null

	@Before
	def before {
		hmac = newMockHmac
		urlEncoder = new OAuthUrlEncoder
	}

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerCredential_is_null_then_sign_throws_exception {
        newSignature(null) sign("anything");
    }

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_consumerSecret_is_null_then_sign_throws_exception {
        newSignature(new Credential("key", null)) sign("anything")
    }

    // TODO: Better exception name
    @Test { val expected=classOf[Exception] }
    def when_token_is_supplied_with_null_secret_then_sign_throws_exception {
        val tokenWithNullSecret = new Credential("key", null)

        newSignature(validConsumerCredential, tokenWithNullSecret) sign("anything");
    }

	@Test
	def when_just_consumer_secret_supplied_then_token_secret_is_just_left_empty {
		val baseString = "anything"

		val credentials = CredentialSet(forConsumer(validConsumerCredential), andNoToken)
		val expectedKey = "secret&"

		val signature = new HmacSha1Signature(urlEncoder, credentials, hmac).sign("anything")

		verify(hmac).create(expectedKey, "anything")
	}
	
	@Test
	def when_consumer_secret_and_token_secret_supplied_then_both_are_used_for_hmac {
		val baseString = "anything"

		val credentials = CredentialSet(forConsumer(validConsumerCredential), andToken(validToken))
		val expectedKey = "secret&token_secret"

		val signature = new HmacSha1Signature(urlEncoder, credentials, hmac).sign(baseString)

		verify(hmac).create(expectedKey, baseString)
	}

    @Test
    def each_part_of_the_key_is_url_encoded {
		val credentials = CredentialSet(
			forConsumer(new Credential("key", "secret with spaces")),
			andToken(new Credential("key", "token secret with spaces"))
		)

		urlEncoder = mock(classOf[UrlEncoder])

		new HmacSha1Signature(urlEncoder, credentials, hmac).sign("anything")

		verify(urlEncoder).encode("secret with spaces")
		verify(urlEncoder).encode("token secret with spaces")
    }

	@Test
	def example {
		val baseString = "GET&http%3A%2F%2Fxxx&" +
			"oauth_consumer_key%3Dkey%26oauth_nonce%3D1108721620a4c6093f92b24d5844e61b%26" +
			"oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1259051683%26" +
			"oauth_token%3DHZvFeX5T7XlRIcJme%252FEWTg%253D%253D%26oauth_version%3D1.0"

		val consumer = new Credential("key", "secret")
		val token = new Credential("HZvFeX5T7XlRIcJme/EWTg==", "Ao61gCJXIM20aqLDw7+Cow==")
        val credentials = CredentialSet(consumer, token)

		val expected = "ZZ3oRaIMA4dg4HrS63qokpKwGbY="
		val actual = new HmacSha1Signature(credentials) sign(baseString)

		assertThat(actual, is(equalTo(expected)))
	}

	private def newMockHmac = {
		val hmac = mock(classOf[Sha1])
		when(hmac.create(anyString, anyString)).thenReturn(new Array[Byte](0))
		hmac
	}

    private def newSignature(consumerCredential : Credential) : HmacSha1Signature = {
        newSignature(consumerCredential, null)
    }

    private def newSignature(consumer  : Credential, token : Credential) : HmacSha1Signature = {
        val credentials = CredentialSet(
            forConsumer(consumer),
            andToken(token)
        )
        new HmacSha1Signature(urlEncoder, credentials, hmac)
    }
}