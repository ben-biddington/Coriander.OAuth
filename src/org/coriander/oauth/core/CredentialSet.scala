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