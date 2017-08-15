package com.jworks.kryptonite.client

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class KryptoniteClientApplication {

//    @Bean
//    fun webClient(): WebClient {
//        return WebClient.create("http://localhost:7070")
//    }
//
//    @Bean
//    fun demo(webClient: WebClient) = CommandLineRunner {
//        webClient.get()
//                .uri()
//    }

}

fun main(args: Array<String>) {
    SpringApplication.run(KryptoniteClientApplication::class.java, *args)
}
