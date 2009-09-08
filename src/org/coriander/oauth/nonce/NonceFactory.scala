package org.coriander.oauth.nonce

abstract class NonceFactory {
    def createNonce() : Long
}
