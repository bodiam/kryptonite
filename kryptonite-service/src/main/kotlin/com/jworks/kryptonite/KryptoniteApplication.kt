package com.jworks.kryptonite

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream


@SpringBootApplication
class KryptoniteApplication {

    @Bean
    fun coins(kryptoRepository: KryptoRepository) = CommandLineRunner {

        kryptoRepository.deleteAll().subscribe(null, null, {
            Stream.of(
                    "BitCoin" to BigDecimal("2200"),
                    "LiteCoin" to BigDecimal("30"),
                    "Ether" to BigDecimal("150"),
                    "Monero" to BigDecimal("30"),
                    "Ripple" to BigDecimal("0.14"),
                    "Golem" to BigDecimal("0.2")
            ).forEach { (name, price) ->
                kryptoRepository
                        .save(KryptoKurrency(name = name, dateCreated = LocalDateTime.now(), value = price))
                        .subscribe { currency -> println(currency) }
            }
        })

        kryptoRepository.findAll().subscribe { println(it) }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(KryptoniteApplication::class.java, *args)
}


@RestController
class KryptoRestController(val kryptoService: KryptoService) {

    @GetMapping("/currencies")
    fun all(): Flux<KryptoKurrency> {
        return kryptoService.all()
    }

    @GetMapping("/currencies/{currencyId}")
    fun byId(@PathVariable("currencyId") currencyId: String): Mono<KryptoKurrency> {
        return kryptoService.byId(currencyId)
    }

    @CrossOrigin
    @GetMapping(produces = arrayOf(TEXT_EVENT_STREAM_VALUE), value = "/currencies/{currencyId}/events")
    fun stream(@PathVariable("currencyId") currencyId: String): Flux<KryptoPriceEvent> {
        return kryptoService.streamPrices(currencyId)
    }
}


@Service
class KryptoService(val kryptoRepository: KryptoRepository) {

    fun streamPrices(currencyId: String): Flux<KryptoPriceEvent> {

        return byId(currencyId).flatMapMany { kurrency ->
            val interval = Flux.interval(Duration.ofSeconds(1))
            val priceEvents = Flux.fromStream(Stream.generate { KryptoPriceEvent(kurrency.copy(value = randomPrice(kurrency.value)), LocalDateTime.now()) })
            Flux.zip(priceEvents, interval).map { it.t1 }
        }
    }

    private fun randomPrice(value: BigDecimal) =value.times(BigDecimal(1.5 * Math.random()))


    fun byId(currencyId: String): Mono<KryptoKurrency> {
        return kryptoRepository.findById(currencyId)
    }

    fun all(): Flux<KryptoKurrency> {
        return kryptoRepository.findAll()
    }
}


interface KryptoRepository : ReactiveMongoRepository<KryptoKurrency, String>

@Document
data class KryptoKurrency(
        @Id val id: String = UUID.randomUUID().toString(),
        val name: String,
        val value: BigDecimal = ZERO,
        val dateCreated: LocalDateTime
)

data class KryptoPriceEvent(val kurrency: KryptoKurrency,
                            val mutationDate: LocalDateTime)