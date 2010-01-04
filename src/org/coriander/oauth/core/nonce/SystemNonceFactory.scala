package org.coriander.oauth.core.nonce

class SystemNonceFactory extends NonceFactory {
    def newNonce() : String = System.nanoTime.toString
}
