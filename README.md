Test-driven, "clean code" pure scala implementation of the OAuth standard.

#Examples#

##How to sign a URI with Consumer

Sugar class which signs a URI for a consumer. Adds query string parameters.

    val uri         = new URI("http://xxx")
    val consumer    = new Consumer(new Credential("key", "secret"))
    val signedUri   = consumer.sign(uri)


##How to create a signed URI

Adds query string parameters to your URI. (Currently supports only __HmacSha1Signature__)

    val uri         = new URI("http://xxx")
    val consumer    = new Credential("key", "secret")
    val token       = new Credential("token", "token_secret")  
    val timestamp   = "1259067839"
    val nonce       = "73f0f93345d76d6cd1bab30af14a99e3"

    val signedUri = new SignedUri(
        uri,
        new CredentialSet(consumer, token),
        timestamp,
        nonce
    )

##How to create an authorization header

A lower-level abstraction than __SignedUri__. This process requires you to first generate a __SignatureBaseString__,
and then a __Signature__. Here we are using __HmacSha1Signature__, but __RsaSha1Signature__ is also available.

    val uri         = new URI("http://xxx")
    val query       = new Query()
    val credentials = new CredentialSet(
        new Credential("consumer_key", "consumer_secret"),
        new Credential("token_key", "token_secret")
    )
    
    val nonce       = "73f0f93345d76d6cd1bab30af14a99e3"
    val timestamp 	= "1259067839"

    val baseString = new SignatureBaseString(
        "GET"
        uri,
        query,
        credentials,
        nonce,
        timestamp,
        Options.DEFAULT
    )

    val signature = new HmacSha1Signature(urlEncoder, credentials).sign(baseString)

    new AuthorizationHeader(
        "REALM",
        credentials,
        signature,
        timestamp,
        nonce,
        Options.DEFAULT,
        urlEncoder
	)

##Example: generating signature base string
The one from the oauth specification (http://oauth.net/core/1.0a#RFC2617,
Appendix A.5.1.  Generating Signature Base String) is represented as a unit test.

See: org.coriander.unit.tests.oauth.core.SignatureBaseStringTest.
    example_with_token_from_the_oauth_spec_document