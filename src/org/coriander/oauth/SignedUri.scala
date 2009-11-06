package org.coriander.oauth

import java.net.URI

class SignedUri(val uri : URI, val consumer : OAuthCredential) {
    def value() : URI = {
        return uri
    }
}
