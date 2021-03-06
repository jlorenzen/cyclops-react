package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;

public interface ReversableSpliterator<T> extends CopyableSpliterator<T>{

    boolean isReverse();

    void setReverse(boolean reverse);

    default ReversableSpliterator<T> invert() {
        setReverse(!isReverse());
        return this;
    }

    ReversableSpliterator<T> copy();


}
