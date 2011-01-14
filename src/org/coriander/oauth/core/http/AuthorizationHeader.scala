/*
Copyright 2011 Ben Biddington

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.coriander.oauth.core.http

import org.coriander.oauth.core.{CredentialSet, Options}
import org.coriander.oauth.core.uri.UrlEncoder

class AuthorizationHeader(
    realm       : String,
    credentials : CredentialSet,
    signature   : String,
    timestamp   : String,
    nonce       : String,
    options     : Options,
    urlEncoder  : UrlEncoder
) {
    val name = "Authorization"
    val value = formatValue(createValue)
    
    override def toString = name + ": " + value

	private def formatValue(value : String) = "OAuth " + value

    private def createValue : String = {
        requireUrlEncoder
        
        "realm=\""                  + realm + "\"," +
        "oauth_consumer_key=\""     + urlEncoder.%%(credentials.consumer.key) + "\"," +
        "oauth_token=\""            + (if (credentials.hasToken) urlEncoder.%%(credentials.token.key) else "") + "\"," +
        "oauth_signature_method=\"" + urlEncoder.%%(options.signatureMethod) + "\"," +
        "oauth_signature=\""        + urlEncoder.%%(signature) + "\"," +
        "oauth_timestamp=\""        + urlEncoder.%%(timestamp) + "\"," +
        "oauth_nonce=\""            + urlEncoder.%%(nonce) + "\"," +
        "oauth_version=\""          + urlEncoder.%%(options.version.toString) + "\""
    }

    private def requireUrlEncoder {
        require (urlEncoder != null, "No urlEncoder has been supplied.")
    }
}
