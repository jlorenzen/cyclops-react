package com.aol.cyclops2.internal.stream.spliterators.push.flatMap.publisher;

import com.aol.cyclops2.types.stream.reactive.AsyncSubscriber;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import com.aol.cyclops2.types.stream.reactive.SeqSubscriber;
import cyclops.collections.ListX;
import cyclops.stream.Spouts;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

/**
 * Created by johnmcclean on 19/01/2017.
 */
public class FlatMapPublisherTest {

    @Test
    public void flatMapP(){
        assertThat(Spouts.of(1,2,3)
                .flatMapP(i->Spouts.of(i))
                .toList(),equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void flatMapP2(){
        assertThat(Spouts.of(1,2,3)
                .flatMapP(i->Spouts.of(1,i))
                .toList(),equalTo(ListX.of(1,1,1,2,1,3)));
    }
    @Test
    public void flatMapPAsync2(){
        for(int k=0;k<100;k++) {
            List<Integer> res = Spouts.of(1, 2, 3)
                    .flatMapP(i -> nextAsync())
                    .toList();
            assertThat(res.size(), equalTo(ListX.of(1, 2, 1, 2, 1, 2).size()));
            assertThat(res, hasItems(1,2));
            int one = 0;
            int two = 0;
            for(Integer next : res){
                if(next==1){
                    one++;
                }
                if(next==2){
                    two++;
                }
            }
            assertThat(one,equalTo(3));
            assertThat(two,equalTo(3));
        }
    }
    @Test
    public void flatMapPAsync3(){
        for(int k=0;k<10;k++) {
            List<Integer> res = Spouts.of(1, 2, 3)
                    .flatMapP(i -> nextAsyncRS())
                    .toList();
            assertThat(res.size(), equalTo(ListX.of(1, 2, 1, 2, 1, 2).size()));
            assertThat(res, hasItems(1,2));
            int one = 0;
            int two = 0;
            for(Integer next : res){
                if(next==1){
                    one++;
                }
                if(next==2){
                    two++;
                }
            }
            assertThat(one,equalTo(3));
            assertThat(two,equalTo(3));
        }
    }
    Subscription subs;
    AtomicInteger count;
    @Test
    public void flatMapPAsyncRS(){
        count = new AtomicInteger(0);
        ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
        Spouts.of(1, 2, 3).peek(System.out::println)
                .flatMapP(i -> nextAsyncRS())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        subs=s;

                    }

                    @Override
                    public void onNext(Integer integer) {
                       assertThat(integer, Matchers.isOneOf(1,2));
                       count.incrementAndGet();
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        subs.request(Long.MAX_VALUE);
        assertThat(count.get(),equalTo(6));

    }
    @Test
    public void flatMapPAsyncRS2(){
        for(int k=0;k<1;k++) {
            ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
            Spouts.of(1, 2, 3).peek(System.out::println)
                    .flatMapP(i -> nextAsyncRS())
                    .subscribe(sub);

            List<Integer> res = sub.reactiveStream().collect(Collectors.toList());
            System.out.println(res);
            assertThat(res.size(), equalTo(ListX.of(1, 2, 1, 2, 1, 2).size()));
            assertThat(res, hasItems(1, 2));
            int one = 0;
            int two = 0;
            for (Integer next : res) {
                if (next == 1) {
                    one++;
                }
                if (next == 2) {
                    two++;
                }
            }
            assertThat(one, equalTo(3));
            assertThat(two, equalTo(3));
        }

    }
    @Test
    public void flatMapPAsyncRS3(){
        for(int k=0;k<1;k++) {
            SeqSubscriber<Integer> sub = SeqSubscriber.subscriber();
            Spouts.of(1, 2, 3).peek(System.out::println)
                    .flatMapP(i -> nextAsyncRS())
                    .subscribe(sub);

            List<Integer> res = sub.stream().collect(Collectors.toList());
            System.out.println(res);
            assertThat(res.size(), equalTo(ListX.of(1, 2, 1, 2, 1, 2).size()));
            assertThat(res, hasItems(1, 2));
            int one = 0;
            int two = 0;
            for (Integer next : res) {
                if (next == 1) {
                    one++;
                }
                if (next == 2) {
                    two++;
                }
            }
            assertThat(one, equalTo(3));
            assertThat(two, equalTo(3));
        }

    }
    private Publisher<Integer> nextAsyncRS() {
        ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();

        new Thread(()->{

            Flux.just(1,2).subscribe(sub);


        }).start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sub.reactiveStream();
    }
    private Publisher<Integer> nextAsync() {
        AsyncSubscriber<Integer> sub = Spouts.asyncSubscriber();
        new Thread(()->{

            sub.awaitInitialization();
            try {
                //not a reactive-stream so we don't know with certainty when demand signalled
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sub.onNext(1);
            sub.onNext(2);
            sub.onComplete();
        }).start();
        return sub.stream();
    }
}