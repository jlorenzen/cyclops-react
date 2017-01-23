package com.aol.cyclops2.internal.stream.spliterators.push;


import cyclops.async.Queue;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class RangeLongOperator implements Operator<Long> {


    final long start;
    final long end;

    public RangeLongOperator(long start, long end){
        this.start = start;
        this.end = end;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super Long> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        long[] index = {start};
        boolean completed[] = {false};
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{

                if(requested.get()==Long.MAX_VALUE) {
                    pushAll();
                }
                while (isActive() && index[0] < end) {
                    try {

                        ((Consumer) onNext).accept(index[0]++);
                        requested.decrementAndGet();
                    } catch (Throwable t) {
                        onError.accept(t);
                    }

                }
                if (index[0] >= end) {
                    if(!completed[0]) {
                        completed[0]=true;
                        onComplete.run();
                    }

                }

            };

            @Override
            public void request(long n) {
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                singleActiveRequest(n, work );
            }

            private void pushAll() {
                for(;index[0]<end;index[0]++){

                    try {
                        if(isOpen)
                            ((Consumer) onNext).accept(index[0]);
                        else
                            break;
                    }catch(Throwable t){
                        onError.accept(t);
                    }
                }
                if(index[0]==end){
                    if(!completed[0]) {
                        completed[0]=true;
                        onComplete.run();
                    }

                }
                requested.set(0);

            }

            @Override
            public void cancel() {

                super.cancel();

            }
        };
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super Long> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        for(long i=start;i<end;i++){
            try {
                ((Consumer) onNext).accept(i);
            }catch(Throwable t){
                onError.accept(t);
            }
        }
        onCompleteDs.run();
    }
}
