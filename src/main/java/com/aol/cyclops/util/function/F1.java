package com.aol.cyclops.util.function;

import java.util.Optional;
import java.util.concurrent.Executor;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.control.Try;

@FunctionalInterface
public interface F1<T1,  R> extends Function1<T1,R>, Reader<T1,R> {

    public static <T1,  T3,R> F1<T1, R> λ(final F1<T1, R> triFunc){
        return triFunc;
    }
    public static <T1,  T3,R> F1<? super T1,? extends R> λv(final F1<? super T1,? extends R> triFunc){
        return triFunc;
    }
    
    public R apply(T1 a);


    default F1<T1,Maybe<R>> lift(){
       return (T1)-> Maybe.fromEval(Eval.later(()->apply(T1)));
    }
    default F1<T1,   FutureW<R>> lift(Executor ex){
       return (T1)-> FutureW.ofSupplier(()->apply(T1),ex);
    }
    default F1<T1,   Try<R,Throwable>> liftTry(){
       return (T1)->  Try.withCatch(()->apply(T1),Throwable.class);
    }
    default F1<T1,   Optional<R>> liftOpt(){
       return (T1)-> Optional.ofNullable(apply(T1));
    }
    
    default F1<T1,R> memoize(){
        return Memoize.memoizeFunction(this);
    }
    default F1<T1,R> memoize(Cacheable<R> c){
        return Memoize.memoizeFunction(this,c);
    }
    
    
    default F0<R> bind(final T1 s) {
        return Curry.curry(this)
                    .apply(s);
    }

    
}