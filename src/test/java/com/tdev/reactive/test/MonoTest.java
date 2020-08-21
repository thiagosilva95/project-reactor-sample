package com.tdev.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
/**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-blocking
 * 3. Backpressure
 * Publisher <- (subscribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with the subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * Publisher -> (onNext) Subscriber
 * unil:
 * 1. Publisher sends all the objects requested.
 * 2. Publisher sends all the objects it has. (onComplete) subscriber and subscription will be canceled
 * 3. There is an error. (onError) -> Subscriber and subscription will be canceled
 */
public class MonoTest {

    @Test
    public void monoSubscriber() {
        String name = "Thiago";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();

        StepVerifier.create(mono)
                .expectNext("Thiago")
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "Thiago";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> log.info("Value {}", s));

        StepVerifier.create(mono)
                .expectNext("Thiago")
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "Thiago";
        Mono<String> mono = Mono.just(name)
                .map(s -> {throw new RuntimeException("Testeing mono with error");});

        mono.subscribe(s -> log.info("Name {}", s), s -> log.error("Somethings bad happened"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }
}
