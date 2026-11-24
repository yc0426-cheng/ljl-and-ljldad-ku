package com.yc.common.core.anotation

import org.springframework.core.annotation.AliasFor
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@RequestMapping(method = [RequestMethod.GET])
annotation class GetResource(
    val name: String,
    @get:AliasFor(annotation = RequestMapping::class)
    val path: String
)
