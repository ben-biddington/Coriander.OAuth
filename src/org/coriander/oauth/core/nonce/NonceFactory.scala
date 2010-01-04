package org.coriander.oauth.core.nonce

trait NonceFactory {
    def newNonce : String
}
