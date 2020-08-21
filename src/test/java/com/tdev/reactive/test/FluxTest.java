package com.tdev.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

@Slf4j
public class FluxTest {

    @Test
    public void fluxSubscriber() {
        Flux<String> fluxString = Flux.just("Thiago", "Henrique", "Reactor")
                .log();

        StepVerifier.create(fluxString)
                .expectNext("Thiago", "Henrique", "Reactor")
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers() {
        Flux<Integer> fluxNumber = Flux.range(1, 5)
                .log();

        fluxNumber.subscribe(integer -> log.info("Number: {}", integer));

        log.info("---------------- TEST -------------------");
        StepVerifier.create(fluxNumber)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberFromList() {
        Flux<Integer> fluxNumber = Flux.fromIterable(List.of(1, 2, 3, 4, 5))
                .log();

        fluxNumber.subscribe(integer -> log.info("Number: {}", integer));

        log.info("---------------- TEST -------------------");
        StepVerifier.create(fluxNumber)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersError() {
        Flux<Integer> fluxNumber = Flux.range(1, 5)
                .log()
                .map(integer -> {
                    if (integer == 4) {
                        throw new IndexOutOfBoundsException(("index error"));
                    }
                    return integer;
                });

        fluxNumber.subscribe(integer -> log.info("Number: {}", integer), Throwable::printStackTrace,
            () -> log.info("DONE"), subscription -> subscription.request(3));

        log.info("---------------- TEST -------------------");
        StepVerifier.create(fluxNumber)
                .expectNext(1, 2, 3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void fluxSubscriberNumbersUglyBackpressure() {
        Flux<Integer> fluxNumber = Flux.range(1, 10)
                .log();

        fluxNumber.subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private Subscription subscription;
            private final int requestCount = 2;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    subscription.request(requestCount);

                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        log.info("---------------- TEST -------------------");
        StepVerifier.create(fluxNumber)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersNotSoUglyBackpressure() {
        Flux<Integer> fluxNumber = Flux.range(1, 10)
                .log();

        fluxNumber.subscribe(new BaseSubscriber<Integer>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }
        });

        log.info("---------------- TEST -------------------");
        StepVerifier.create(fluxNumber)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersPrettyBackpressure() {
        Flux<Integer> fluxNumber = Flux.range(1, 10)
                .log()
                .limitRate(3);

        fluxNumber.subscribe(integer -> log.info("Number: {}", integer));

        log.info("---------------- TEST -------------------");
        StepVerifier.create(fluxNumber)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberIntervalOne() throws InterruptedException {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .take(10)
                .log();

        interval.subscribe(i -> log.info("Number {}", i));

        Thread.sleep(3000);
    }

    @Test
    public void fluxSubscriberIntervalTwo() throws InterruptedException {
        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .expectNoEvent(Duration.ofHours(24))
                .thenAwait(Duration.ofDays(1))
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1))
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    private Flux<Long> createInterval() {
        return Flux.interval(Duration.ofDays(1))
                .take(10)
                .log();
    }

    @Test
    public void connectableFlux() throws InterruptedException {
        ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
               // .log()
                .delayElements(Duration.ofMillis(100))
                .publish();

        /*connectableFlux.connect();

        log.info("Thread sleeping for 300 ms");
        Thread.sleep(300);

        connectableFlux.subscribe(i -> log.info("Sub1 number: {}", i));

        log.info("Thread sleeping for 200 ms");

        Thread.sleep(2000);

        connectableFlux.subscribe(i -> log.info("Sub2 number: {}", i));
*/
        StepVerifier
                .create(connectableFlux)
                .then(connectableFlux::connect)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .expectComplete()
                .verify();
    }

    @Test
    public void connectableFluxAutoConnect() throws InterruptedException {
        Flux<Integer> fluxAutoConnect = Flux.range(1, 5)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish()
                .autoConnect(2);
        
        StepVerifier
                .create(fluxAutoConnect)
                .then(fluxAutoConnect::subscribe)
                .expectNext(1, 2, 3, 4, 5)
                .expectComplete()
                .verify();
    }

}
