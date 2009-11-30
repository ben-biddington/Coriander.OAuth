package org.coriander.oauth.core

final class CredentialSet (val consumer : Credential, val token : Credential) {
    def this(consumer : Credential) {
        this(consumer, null)
    }
    
    def hasConsumer : Boolean = return consumer != null
    def hasToken : Boolean = return token != null
}

object CredentialSet {
    def apply(consumer : Credential) : CredentialSet  = {
        return apply(consumer, null)
    }

    def apply(consumer : Credential, token : Credential) : CredentialSet  = {
        return new CredentialSet(consumer, token)
    }

    def forConsumer(consumer : Credential) : Credential = consumer
    def andToken(token : Credential) : Credential = token
    def andNoToken : Credential = null
}