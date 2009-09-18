package org.coriander.oauth.nonce

class SystemNonceFactory extends NonceFactory {
    def createNonce() : String = {
        System.nanoTime.toString;
    }
}
