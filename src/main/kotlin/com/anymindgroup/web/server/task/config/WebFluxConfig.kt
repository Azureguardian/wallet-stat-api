package com.anymindgroup.web.server.task.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

//@Configuration
//@EnableWebFlux
//class WebFluxConfig : WebFluxConfigurer {
//
//    @Autowired
//    lateinit var objectMapper: ObjectMapper
//
//    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
//        configurer.defaultCodecs().jackson2JsonEncoder(
//            Jackson2JsonEncoder(objectMapper)
//        )
//
//        configurer.defaultCodecs().jackson2JsonDecoder(
//            Jackson2JsonDecoder(objectMapper)
//        )
//    }
//}