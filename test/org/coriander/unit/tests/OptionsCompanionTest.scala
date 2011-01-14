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

package org.coriander.unit.tests

import org.junit.Test
import org.junit.Assert._
import org.hamcrest.core.Is._
import org.hamcrest.core.IsEqual._

import org.coriander.oauth.core.Options

class OptionsCompanionTest extends TestBase {
    @Test
    def DEFAULT_has_standard_default_values {
        assertThat(Options.DEFAULT.signatureMethod, is(equalTo("HMAC-SHA1")))
        assertThat(Options.DEFAULT.version, is(equalTo(1.0)))
    }
}