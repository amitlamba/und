package com.und.security

import com.und.common.utils.DateUtils
import com.und.model.jpa.security.AuthorityName
import com.und.model.jpa.security.UndUserDetails
import com.und.model.redis.security.UserCache
import com.und.repository.jpa.security.UserRepository
import com.und.repository.redis.UserCacheRepository
import com.und.security.utils.KEYTYPE
import com.und.security.utils.KeyResolver
import com.und.security.utils.RestTokenUtil
import com.und.service.security.JWTKeyService
import io.jsonwebtoken.impl.TextCodec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.util.DateUtil
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.util.ReflectionTestUtils

/**
 * Created by shiv on 21/07/17.
 */
@RunWith(MockitoJUnitRunner::class)
class RestTokenUtilTest {


    @Mock
    private lateinit var dateUtilsMock: DateUtils

    @InjectMocks
    private lateinit var keyResolverMock: KeyResolver

    @InjectMocks
    private lateinit var restTokenUtil: RestTokenUtil

    @Mock
    private lateinit var jwtKeyService: JWTKeyService


    @Mock
    lateinit var userCacheRepository: UserCacheRepository

    @Mock
    lateinit var userRepository: UserRepository


    private val secret: String = "supremeSecret"

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        ReflectionTestUtils.setField(restTokenUtil, "expiration", 3600L) // one hour
        ReflectionTestUtils.setField(restTokenUtil, "keyResolver", keyResolverMock)
        ReflectionTestUtils.setField(keyResolverMock, "jwtKeyService", jwtKeyService)
        ReflectionTestUtils.setField(restTokenUtil, "jwtKeyService", jwtKeyService)
        ReflectionTestUtils.setField(jwtKeyService, "userRepository", userRepository)
        ReflectionTestUtils.setField(jwtKeyService, "userCacheRepository", userCacheRepository)
/*       whenever(
                keyResolverMock.resolveSigningKeyBytes(ArgumentMatchers.any<DefaultJwsHeader>(), ArgumentMatchers.any<Claims?>())).
                thenReturn(TextCodec.BASE64.decode(secret)
                )*/
    }

    @Test
    @Throws(Exception::class)
    fun testGenerateTokenGeneratesDifferentTokensForDifferentCreationDates() {
        `when`(dateUtilsMock.now())
                .thenReturn(DateUtil.yesterday())
                .thenReturn(DateUtil.now())



        val (token, uc) = createToken()
        val (laterToken, laterUc) = createToken()

        assertThat(token).isNotEqualTo(laterToken)
    }

    @Test
    @Throws(Exception::class)
    fun getUsernameFromToken() {
        `when`(dateUtilsMock.now()).thenReturn(DateUtil.now())
        val (token, uc) = createToken()
        `when`(jwtKeyService.getKeyIfExists(1L))
                .thenReturn(uc)
        val (user, _) = restTokenUtil.validateToken(token)
        assertThat(user?.username).isEqualTo(TEST_USER)
    }


    @Test
    @Throws(Exception::class)
    fun getRolesFromToken() {
        `when`(dateUtilsMock.now()).thenReturn(DateUtil.now())
        val (token, uc) = createToken()
        val (user, _) = restTokenUtil.validateToken(token)

        assertThat(user?.authorities).isEqualTo(
                arrayListOf(
                        SimpleGrantedAuthority(AuthorityName.ROLE_ADMIN.name),
                        SimpleGrantedAuthority(AuthorityName.ROLE_EVENT.name)
                )
        )
    }


    private fun createToken(): Pair<String, UserCache> {
        val user = UndUserDetails(
                id = 1L,
                username = TEST_USER,
                secret = TextCodec.BASE64.encode(secret),
                key = "key",
                password = "password",
                clientId = 1,
                authorities = arrayListOf(SimpleGrantedAuthority(AuthorityName.ROLE_ADMIN.name), SimpleGrantedAuthority(AuthorityName.ROLE_EVENT.name))
        )
        val jwtKey = UserCache(secret = secret, userId = "${user.id}")
        with(jwtKey) {
            username = user.username
            secret = user.secret
            password = user.password ?: ""
            clientId = "${user.clientId}"
            email = user.email ?: "not available"

        }
        val device = DeviceMock()
        device.isNormal = true
        `when`(restTokenUtil.getJwtIfExists(user.id!!))
                .thenReturn(jwtKey)

        val jwtKeys = restTokenUtil.generateJwtByUser(user, KEYTYPE.LOGIN)
        return Pair(jwtKeys.loginKey ?: "", jwtKeys)
    }

    companion object {

        private const val TEST_USER = "testUser"
    }

}