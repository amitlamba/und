package com.und.eventapi

import com.und.web.model.eventapi.EventUser
import com.und.model.mongo.eventapi.EventUser as MongoEventUser
import com.und.eventapi.utils.copyNonNull
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test

class EventUserTest {

    @Test
    fun now() {
        val eventUserDb = MongoEventUser()
        with(eventUserDb.standardInfo) {

            firstname = "hello"
            lastname = "mugabe"
            country = "Zimbabwe"
        }
        with(eventUserDb.identity) {
            uid = "100"
            fbId = "boringboooker"
            googleId = "mugabe@zimbabwe.com"
            email = "mugabe@zimbabwe.com"
        }

        val eventUserNew = EventUser()
        eventUserNew.uid = "200"
        with(eventUserNew) {
            firstName = "namaste"
            lastName = null
            countryCode = "Zim"

        }
        with(eventUserNew) {
            fbId = "coolBooker"
            googleId = null
            email = "newmugambe@zimbabwe.com"
        }

        val copiedUser = eventUserDb.copyNonNull(eventUserNew)
        Assert.assertThat(copiedUser.standardInfo.lastname , IsEqual.equalTo("mugabe"))
        Assert.assertThat(copiedUser.standardInfo.firstname , IsEqual.equalTo("namaste"))
        Assert.assertThat(copiedUser.standardInfo.country , IsEqual.equalTo("Zimbabwe"))
        //Assert.assertThat(copiedUser.standardInfo.countryCode , IsEqual.equalTo("Zim"))


        Assert.assertThat(copiedUser.identity.uid , IsEqual.equalTo("200"))
        Assert.assertThat(copiedUser.identity.fbId , IsEqual.equalTo("coolBooker"))
        Assert.assertThat(copiedUser.identity.googleId , IsEqual.equalTo("mugabe@zimbabwe.com"))
        Assert.assertThat(copiedUser.identity.email , IsEqual.equalTo("newmugambe@zimbabwe.com"))
    }
}