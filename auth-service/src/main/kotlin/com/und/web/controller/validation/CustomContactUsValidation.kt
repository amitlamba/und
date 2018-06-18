package com.und.web.controller.validation

import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.MustBeDocumented
import javax.validation.Payload
import kotlin.reflect.KClass
import javax.validation.Constraint


@MustBeDocumented
@Constraint(validatedBy = [(CustomContactUsValidator::class)])
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS,
        AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidateContactUs(
        val message: String = "Hi",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)