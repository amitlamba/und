package com.und.security.model

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UndUserDetailsDeSerializationTest {


    @Test
            //@Throws(Exception::class)
    fun deSerializeCompleteJson() {

        val json = """
 {
        "id":1234,
      "username": "shiv6@und.com",
      "authorities": [
        {
          "authority": "ROLE_ADMIN"
        }
      ],
      "enabled": false,
      "clientId": 5,
      "firstname": "",
      "lastname": "",
      "email": null,
      "name": "shiv6@und.com",
      "timeZoneId":"UTC"
    }
    """.trimIndent()

        val  mapper = ObjectMapper()
        val user = mapper.readValue(json, UndUserDetails::class.java)

        Assertions.assertThat(user.authorities).isEqualTo(arrayListOf(SimpleGrantedAuthority("ROLE_ADMIN")))
    }




}