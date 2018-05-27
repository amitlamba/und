package com.und.eventapi.validation

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

    @MustBeDocumented
    @Constraint(validatedBy = arrayOf(IDValidator::class))
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ValidateID(

            val message: String = "{event.id.invalid}",
            val groups: Array<KClass<*>> = arrayOf(),
            val payload: Array<KClass<out Payload>> = arrayOf()
    )