package org.coriander.oauth.core.nonce


abstract class NonceFactory {
    def createNonce() : String
}
