package com.und.web.validation

import javax.validation.Constraint

@Constraint(validatedBy = emptyArray())
annotation class NullifyEmptyString {
}