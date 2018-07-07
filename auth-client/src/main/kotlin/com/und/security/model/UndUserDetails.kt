package com.und.security.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.io.IOException
import java.time.ZoneId


/**
 * Created by shiv on 21/07/17.
 */
@JsonDeserialize(using = UndUserDeserilaizer::class)
class UndUserDetails(
        @get:JsonIgnore
        val id: Long?,

        val clientId: Long?,

        private val username: String,

        val firstname: String? = "",

        val lastname: String? = "",

        private var password: String? = null,

        val email: String? = null,

        @JsonDeserialize(using = StringToAuthorityDeSerializer::class)
        private var authorities: Collection<GrantedAuthority> = arrayListOf(),

        private val enabled: Boolean = false,

        @get:JsonIgnore
        val secret: String,

        @get:JsonIgnore
        val key: String? = null,

        var timeZoneId: String = ZoneId.of("UTC").id
) : User(username, password, authorities) {

    override fun getUsername(): String {
        return username
    }

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean {
        return true
    }

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean {
        return true
    }

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    @JsonIgnore
    override fun getPassword(): String? {
        return password
    }

    //@JsonSerialize(using = StringToAuthoritySerializer::class)
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun isEnabled(): Boolean {
        return enabled
    }
}

class StringToAuthorityDeSerializer : JsonDeserializer<List<GrantedAuthority>>() {


    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): List<GrantedAuthority> {
        val nameList = jsonParser.readValueAs(List::class.java)
        val authority = nameList.map { name -> SimpleGrantedAuthority(name.toString()) }
        return authority
    }

}

class UndUserDeserilaizer : JsonDeserializer<UndUserDetails>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): UndUserDetails {
        val userDetailsNode = jsonParser.codec.readTree<TreeNode>(jsonParser)
        //val nameList = jsonParser.readValueAs(List::class.java)
        val id = userDetailsNode.get("id").toString().toLong()
        val username =  (userDetailsNode.get("username") as TextNode).asText()
        //val name = userDetailsNode.get("name").toString()
        val firstname =  (userDetailsNode.get("firstname") as TextNode).asText()
        val lastname = (userDetailsNode.get("lastname") as TextNode).asText()
        val email = userDetailsNode.get("email").toString()
        val clientId = userDetailsNode.get("clientId").toString().toLong()
        val enable = userDetailsNode.get("enabled").numberType()
        val authoritiesNode = userDetailsNode.get("authorities")
        val timezone = (userDetailsNode.get("timeZoneId") as TextNode).asText()

        val authorities = if (authoritiesNode != null && authoritiesNode is ArrayNode) {
            authoritiesNode.map { t ->

                SimpleGrantedAuthority(t.get("authority").asText())
            }
        } else {
            arrayListOf()
        }

        return  UndUserDetails(
                id = id,
                username = username,
                secret = "secret",
                key = "key",
                password = "password",
                clientId = clientId,
                authorities = authorities,
                timeZoneId = timezone
        )
    }
}


