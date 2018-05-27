/*
package com.und.eventapi

import com.und.common.utils.DateUtils
import com.und.security.model.Authority
import com.und.security.model.AuthorityName
import com.und.security.model.User
import com.und.security.repository.UserRepository
import com.und.security.service.UNDUserDetailsService
import com.und.security.utils.RestUserFactory
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
@Ignore
class AuthenticationRestControllerTest {

    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext


    @Mock
    private lateinit var userDetailsService: UNDUserDetailsService

    @Mock
    lateinit var userRepository: UserRepository

    @Value("\${security.header.token}")
    private lateinit var tokenHeader: String


    @Before
    fun setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
    }

    @Test
    //@Ignore
    @WithMockUser(roles = arrayOf("USER"))
    @Throws(Exception::class)
    fun successfulToken() {
        //FIXME failing tests

        val user = buildMockUser(AuthorityName.ROLE_ADMIN)

        val eventUser = RestUserFactory.create(user)
        `when`<UserDetails>(this.userDetailsService.loadUserByUsername(anyString())).thenReturn(eventUser)
        this.mvc.perform(post("/auth").content("""{"username":"username","password":"password"}""").contentType("application/json"))
                .andExpect(status().is2xxSuccessful)
    }

    fun buildMockUser(authorityNames: AuthorityName): User {

        val user = User()
        user.username = "username"
        user.firstname = "firstname"
        user.authorities = buildAuthorities(authorityNames)
        user.enabled = java.lang.Boolean.TRUE
        user.password = "password"
        user.lastname = ""
        user.clientSecret = "secret"
        user.email = ""
        user.key = ""
        user.lastPasswordResetDate = DateUtils().now()
        user.lastPasswordResetDate = Date(System.currentTimeMillis() + 1000 * 1000)
        return user
    }

    fun buildAuthorities(authorityNames: AuthorityName): ArrayList<Authority> {

        val authority = Authority()
        authority.id = 0L
        authority.name = authorityNames
        val authorities = arrayListOf(authority)
        return authorities
    }

    @Test
    @WithMockUser(roles = arrayOf("ADMIN"))
    @Throws(Exception::class)
    fun successfulRefreshTokenWithAdminRole() {

        val user = buildMockUser(AuthorityName.ROLE_ADMIN)
        val eventUser = RestUserFactory.create(user)
        `when`<UserDetails>(this.userDetailsService.loadUserByUsername(anyString())).thenReturn(eventUser)
        this.mvc.perform(get("/refresh/true")
                .header(tokenHeader, user.key)
        )
                .andExpect(status().is2xxSuccessful)
    }

    @Test
    @WithAnonymousUser
    @Throws(Exception::class)
    fun shouldGetUnauthorizedWithAnonymousUser() {

        this.mvc.perform(get("/refresh/true"))
                .andExpect(status().is4xxClientError)

    }

}*/
