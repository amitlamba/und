package com.und.service

import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

class SampleSpec : Spek({
    given("some context") {
        on("executing some action") {
            it("should pass") {
                MatcherAssert.assertThat(2, CoreMatchers.`is`(2))
            }
        }
    }

})