package com.tdev.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
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

    @Test
    public void monoSubscriberConsumerComplete() {
        String name = "Thiago";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(s -> s.toUpperCase());

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"));

        StepVerifier.create(mono)
                .expectNext("Thiago".toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        String name = "Thiago";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(s -> s.toUpperCase());

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                Subscription::cancel);

        /*StepVerifier.create(mono)
                .expectNext("Thiago".toUpperCase())
                .verifyComplete();*/
    }

    @Test
    public void monoDoOnMethods() {
        String name = "Thiago";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(s -> s.toUpperCase())
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request received, starting doing something"))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .flatMap(s -> Mono.empty())
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s)) // will not be executed
                .doOnSuccess(s -> log.info("doOnSuccess executed {}", s));

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"));

       /* StepVerifier.create(mono)
                .expectNext("Thiago".toUpperCase())
                .verifyComplete();*/
    }

    @Test
    public void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("Ilegal Argument Exception"))
                .doOnError(e -> log.error("Error message: {}", e.getMessage()))
                .doOnNext(s -> log.info("Executing this doOnNext"))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume() {
        String name = "Thiago";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Ilegal Argument Exception"))
                .doOnError(e -> log.error("Error message: {}", e.getMessage()))
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn() {
        String name = "Thiago";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Ilegal Argument Exception"))
                .doOnError(e -> log.error("Error message: {}", e.getMessage()))
                .onErrorReturn("EMPTY")
                .log();

        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();
    }
}
