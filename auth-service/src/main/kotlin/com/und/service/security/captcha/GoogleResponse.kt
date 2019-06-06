package com.und.service.security.captcha

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder("success", "challenge_ts", "hostname", "error-codes")
class GoogleResponse {

    @JsonProperty("success")
    @get:JsonProperty("success")
    @set:JsonProperty("success")
    internal var isSuccess: Boolean = false

    @JsonProperty("challenge_ts")
    @get:JsonProperty("challenge_ts")
    @set:JsonProperty("challenge_ts")
    internal var challengeTs: String? = null

    @JsonProperty("hostname")
    @get:JsonProperty("hostname")
    @set:JsonProperty("hostname")
    internal var hostname: String? = null

    @JsonProperty("error-codes")
    @get:JsonProperty("error-codes")
    @set:JsonProperty("error-codes")
    internal var errorCodes: Array<ErrorCode>? = null

    internal enum class ErrorCode {
        MissingSecret, InvalidSecret, MissingResponse, InvalidResponse,TimeOutOrDuplicate,BadRequest;


        companion object {

            private val errorsMap = HashMap<String, ErrorCode>(4)

            init {
                errorsMap["missing-input-secret"] = MissingSecret
                errorsMap["invalid-input-secret"] = InvalidSecret
                errorsMap["missing-input-response"] = MissingResponse
                errorsMap["invalid-input-response"] = InvalidResponse
                errorsMap["timeout-or-duplicate"] = TimeOutOrDuplicate
                errorsMap["bad-request"] = BadRequest
            }

            @JsonCreator
            @JvmStatic
            fun forValue(value: String): ErrorCode? {
                return errorsMap[value.toLowerCase()]
            }
        }
    }

    @JsonIgnore
    fun hasClientError(): Boolean {
        val errors = errorCodes ?: return false
        for (error in errors) {
            when (error) {
                GoogleResponse.ErrorCode.InvalidResponse, GoogleResponse.ErrorCode.MissingResponse -> return true
                else -> {
                }
            }
        }
        return false
    }

    override fun toString(): String {
        return "GoogleResponse{success=$isSuccess , challengeTs=${challengeTs.toString()} , hostname=$hostname.toString(), errorCodes= Arrays.toString(errorCodes)}"
    }
}
