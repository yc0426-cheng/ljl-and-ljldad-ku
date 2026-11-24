package com.yc.common.core.anotation

import org.springframework.core.annotation.AliasFor
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@RestController
@RequestMapping
annotation class RestResource(
    val name: String,
    @get:AliasFor(annotation = RequestMapping::class)
    val path: String
)
