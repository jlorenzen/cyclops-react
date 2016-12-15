package com.aol.cyclops.internal.stream.spliterators;



import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public class OnEmptySpliterator<T> extends Spliterators.AbstractSpliterator<T> {
    private final Spliterator<T> source;
    private final T value;
    private boolean found=false;
    private boolean sent=false;


    public OnEmptySpliterator(Spliterator<T> source, T value) {
        super(source.estimateSize(), source.characteristics() & Spliterator.ORDERED);
        this.source = source;
        this.value = value;
    }


    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(sent)
            return false;
        if(found)
            return source.tryAdvance(action);
        else{
            if(!source.tryAdvance(e->{
                found =true;
                action.accept(e);
            })){
                sent =true;
                action.accept(value);
            }
        }

        return found;
    }
}