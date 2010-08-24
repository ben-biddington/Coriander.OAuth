package org.coriander.unit.tests.oauth

import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsNot._
import org.hamcrest.core.IsEqual._
import org.coriander.oauth.Consumer
import org.coriander.oauth.core.Credential
import java.net.URI
import org.coriander.unit.tests.TestBase
import org.coriander.oauth.core.timestamp.TimestampFactory
import org.coriander.oauth.core.nonce.NonceFactory
import org.mockito.Mockito._

class ConsumerTests extends TestBase {
	@Test
	def sign_produces_correct_result {
		given_the_next_timestamp_will_be("1257608197")
		given_the_next_nonce_will_be("ea757706c42e2b14a7a8999acdc71089")

		 val expectedSignedUrl = "http://xxx/?" +
			"oauth_consumer_key=key&" +
			"oauth_nonce=ea757706c42e2b14a7a8999acdc71089&" +
			"oauth_signature=araCdxKcPVOtjuqNZhV3No5hlV4%3D&" +
			"oauth_signature_method=HMAC-SHA1&" +
			"oauth_timestamp=1257608197&" +
			"oauth_token=&" +
			"oauth_version=1.0"

		val uri = new URI("http://xxx/")
		
    	val consumer = new Consumer(
			new Credential("key", "secret"),
			mockTimestampFactory,
			mockNonceFactory
		)

		val signedUri = consumer.sign(uri)

		assertThat(signedUri.toString, is(equalTo(expectedSignedUrl)))
    }

	private def given_the_next_timestamp_will_be(timestamp : String) {
		mockTimestampFactory = mock(classOf[TimestampFactory])
		when(mockTimestampFactory.newTimestamp).
		thenReturn(timestamp);
	}

	private def given_the_next_nonce_will_be(nonce : String) {
		mockNonceFactory = mock(classOf[NonceFactory])
		when(mockNonceFactory.newNonce).
		thenReturn(nonce);
	}

	private var mockTimestampFactory : TimestampFactory = null
	private var mockNonceFactory : NonceFactory = null
}
