package org.coriander.oauth.nonce

class SystemNonceFactory extends NonceFactory {
    def createNonce() : Long = {
        System.nanoTime;
    }
}
