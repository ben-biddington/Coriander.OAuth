package org.coriander.oauth.core.nonce

class SystemNonceFactory extends NonceFactory {
    def createNonce() : String = System.nanoTime.toString
}
