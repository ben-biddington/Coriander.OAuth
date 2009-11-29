package org.coriander.oauth.core

final class OAuthCredential (val key : String, val secret : String)

final class OAuthCredentialSet (val consumer : OAuthCredential, val token : OAuthCredential) {
    def this(consumer : OAuthCredential) {
        this(consumer, null)
    }
    def hasConsumer : Boolean = return consumer != null
    def hasToken : Boolean = return token != null
}

object OAuthCredentialSet {
    def apply(consumer : OAuthCredential) : OAuthCredentialSet  = {
        return apply(consumer, null)    
    }

    def apply(consumer : OAuthCredential, token : OAuthCredential) : OAuthCredentialSet  = {
        return new OAuthCredentialSet(consumer, token)    
    }

    def forConsumer(consumer : OAuthCredential) : OAuthCredential = consumer
    def andToken(token : OAuthCredential) : OAuthCredential = token
    def andNoToken : OAuthCredential = null
}
