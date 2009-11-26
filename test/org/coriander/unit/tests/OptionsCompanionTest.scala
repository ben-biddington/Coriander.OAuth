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