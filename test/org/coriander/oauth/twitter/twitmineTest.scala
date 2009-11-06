package org.coriander.oauth.twitter

import org.junit._
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import java.io._;

import org.apache.commons.httpclient._;
import org.apache.commons.httpclient.methods._;

import org.coriander.oauth.nonce._
import org.coriander.oauth.timestamp._
import org.coriander.oauth.uri._
import org.coriander.oauth.http._

class twitmineTest {

    // See: http://twitter.com/oauth_clients/details/42032

    val REQUEST_TOKEN_URL   = "http://twitter.com/oauth/request_token"
    val ACCESS_TOKEN_URL    = "http://twitter.com/oauth/access_token"
    val AUTHORIZE_URL       = "http://twitter.com/oauth/authorize"
    var nonceFactory        = new SystemNonceFactory
    var timestampFactory    = new SystemTimestampFactory
    val urlEncoder          = new OAuthURLEncoder

    val consumerCredential = new OAuthCredential(
        "key",
        "secret"
    )

    @Ignore("Needs work") @Test
    def twitmine_get_request_token_works() {
        val uri = new java.net.URI(REQUEST_TOKEN_URL)
        val authHeader = getAuthHeader(uri)

        println(execute(uri, new Header("Authorization", authHeader.value)))

        // TODO: It appears twitter is using query strings, not headers.
        // currently we have nothing to support this. We need to assemble a
        // URL instead of header -- contains the same stuff.
    }

    private def getAuthHeader(uri : java.net.URI) : AuthorizationHeader = {
        val nonce = nonceFactory createNonce
        val timestamp = timestampFactory createTimestamp

        val baseString : SignatureBaseString = new SignatureBaseString(
            "GET",
            uri,
            Map(),
            consumerCredential,
            nonce,
            timestamp
        )

        val signature : String = new Signature(new OAuthURLEncoder(), consumerCredential).
            sign(baseString) toString
        
        new AuthorizationHeader(
            "http://twitter.com",
            consumerCredential key,
            null,
            "HMAC-SHA1",
            signature,
            timestamp,
            nonce,
            "1.0",
            urlEncoder
        )
    }

    private def execute(uri : java.net.URI, authHeader : Header) : String = {
        val client : HttpClient = new HttpClient()

        val method : GetMethod = new GetMethod(uri toString)

        method.addRequestHeader(authHeader)

        println(authHeader toString)

        val statusCode : int = client.executeMethod(method)

        if (statusCode != HttpStatus.SC_OK)
            return ("[execute] Request failed: " + method.getStatusLine)

        getResponse(method)
    }

    private def getResponse(methodBase : HttpMethodBase) : String = {
        val reader : BufferedReader = new BufferedReader(
            new InputStreamReader(methodBase.getResponseBodyAsStream())
        )

        try {
            return readToEnd(reader)
        } finally {
            if (reader != null) {
                reader.close
            }
        }
    }

    private def readToEnd(reader : BufferedReader) : String = {
        var line : String = null

        val result = new StringBuffer

        while ((line = reader.readLine()) != null) {
            result.append(line)
        }

        result toString
    }
}