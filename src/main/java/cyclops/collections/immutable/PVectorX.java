package cyclops.collections.immutable;

import com.aol.cyclops.data.collections.extensions.persistent.PVectorXImpl;
import com.aol.cyclops.data.collections.extensions.persistent.PersistentCollectionX;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.Reducers;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import cyclops.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.FluentSequenceX;
import cyclops.collections.ListX;
import com.aol.cyclops.types.OnEmptySwitch;
import com.aol.cyclops.types.To;
import cyclops.monads.WitnessType;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface PVectorX<T> extends To<PVectorX<T>>,
                                     PVector<T>, 
                                     FluentSequenceX<T>,
        PersistentCollectionX<T>,
                                     OnEmptySwitch<T, 
                                     PVector<T>> {


    default <W extends WitnessType<W>> ListT<W, T> liftM(W witness) {
        return ListT.of(witness.adapter().unit(this));
    }
    /**
     * Narrow a covariant PVectorX
     * 
     * <pre>
     * {@code 
     *  PVectorX<? extends Fruit> set = PVectorX.of(apple,bannana);
     *  PVectorX<Fruit> fruitSet = PVectorX.narrow(set);
     * }
     * </pre>
     * 
     * @param vectorX to narrow generic type
     * @return POrderedSetX with narrowed type
     */
    public static <T> PVectorX<T> narrow(final PVectorX<? extends T> vectorX) {
        return (PVectorX<T>) vectorX;
    }
    
    
    /**
     * Create a PVectorX that contains the Integers between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range PVectorX
     */
    public static PVectorX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end)
                          .toPVectorX();
    }

    /**
     * Create a PVectorX that contains the Longs between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range PVectorX
     */
    public static PVectorX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end)
                          .toPVectorX();
    }

    /**
     * Unfold a function into a PVectorX
     * 
     * <pre>
     * {@code 
     *  PVectorX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</code>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return PVectorX generated by unfolder function
     */
    static <U, T> PVectorX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .toPVectorX();
    }

    /**
     * Generate a PVectorX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate PVectorX elements
     * @return PVectorX generated from the provided Supplier
     */
    public static <T> PVectorX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .toPVectorX();
    }  
    /**
     * Generate a PVectorX from the provided value up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Value for PVectorX elements
     * @return PVectorX generated from the provided Supplier
     */
    public static <T> PVectorX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit)
                          .toPVectorX();
    }

    /**
     * Create a PVectorX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return PVectorX generated by iterative application
     */
    public static <T> PVectorX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .toPVectorX();

    }

    /**
     * Construct a PVector from the provided values 
     * 
     * <pre>
     * {@code 
     *  List<String> list = PVectors.of("a","b","c");
     *  
     *  // or
     *  
     *  PVector<String> list = PVectors.of("a","b","c");
     *  
     *  
     * }
     * </pre>
     * 
     * 
     * @param values To add to PVector
     * @return new PVector
     */
    public static <T> PVectorX<T> of(final T... values) {
        return new PVectorXImpl<>(
                                  TreePVector.from(Arrays.asList(values)));
    }
    /**
     * 
     * Construct a PVectorX from the provided Iterator
     * 
     * @param it Iterator to populate PVectorX
     * @return Newly populated PVectorX
     */
    public static <T> PVectorX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }
    /**
     * <pre>
     * {@code 
     *     List<String> empty = PVectors.empty();
     *    //or
     *    
     *     PVector<String> empty = PVectors.empty();
     * }
     * </pre>
     * @return an empty PVector
     */
    public static <T> PVectorX<T> empty() {
        return new PVectorXImpl<>(
                                  TreePVector.empty());
    }

    /**
     * Construct a PVector containing a single value
     * </pre>
     * {@code 
     *    List<String> single = PVectors.singleton("1");
     *    
     *    //or
     *    
     *    PVector<String> single = PVectors.singleton("1");
     * 
     * }
     * </pre>
     * 
     * @param value Single value for PVector
     * @return PVector with a single value
     */
    public static <T> PVectorX<T> singleton(final T value) {
        return new PVectorXImpl<>(
                                  TreePVector.singleton(value));
    }

    /**
     * Construct a PVectorX from an Publisher
     * 
     * @param publisher
     *            to construct PVectorX from
     * @return PVectorX
     */
    public static <T> PVectorX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>) publisher)
                          .toPVectorX();
    }

    public static <T> PVectorX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof PVectorX)
            return (PVectorX) iterable;
        if (iterable instanceof PVector)
            return new PVectorXImpl<>(
                                      (PVector) iterable);
        PVector<T> res = TreePVector.<T> empty();
        final Iterator<T> it = iterable.iterator();
        while (it.hasNext())
            res = res.plus(it.next());

        return new PVectorXImpl<>(
                                  res);
    }

    /**
     * Create a PVector from the supplied Colleciton
     * <pre>
     * {@code 
     *   PVector<Integer> list = PVectors.fromCollection(Arrays.asList(1,2,3));
     *   
     * }
     * </pre>
     * 
     * @param values to add to new PVector
     * @return PVector containing values
     */
    public static <T> PVectorX<T> fromCollection(final Collection<T> values) {
        if (values instanceof PVectorX)
            return (PVectorX) values;
        if (values instanceof PVector)
            return new PVectorXImpl<>(
                                      (PVector) values);
        return new PVectorXImpl<>(
                                  TreePVector.from(values));
    }

    /**
     * Reduce (immutable Collection) a Stream to a PVector
     * 
     * <pre>
     * {@code 
     *    PVector<Integer> list = PVectors.fromStream(Stream.of(1,2,3));
     * 
     *  //list = [1,2,3]
     * }</pre>
     * 
     * 
     * @param stream to convert to a PVector
     * @return
     */
    public static <T> PVectorX<T> fromStream(final Stream<T> stream) {
        return Reducers.<T> toPVectorX()
                       .mapReduce(stream);
    }

    /**
    * Combine two adjacent elements in a PVectorX using the supplied BinaryOperator
    * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
    * with it's neighbor
    * <pre>
    * {@code 
    *  PVectorX.of(1,1,2,3)
                 .combine((a, b)->a.equals(b),Semigroups.intSum)
                 .toListX()
                 
    *  //ListX(3,4) 
    * }</pre>
    * 
    * @param predicate Test to see if two neighbors should be joined
    * @param op Reducer to combine neighbors
    * @return Combined / Partially Reduced PVectorX
    */
    @Override
    default PVectorX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (PVectorX<T>) PersistentCollectionX.super.combine(predicate, op);
    }

 

    @Override
    default PVectorX<T> take(final long num) {

        return limit(num);
    }
    @Override
    default PVectorX<T> drop(final long num) {

        return skip(num);
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> PVectorX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (PVectorX)PersistentCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> PVectorX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (PVectorX)PersistentCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> PVectorX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (PVectorX)PersistentCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> PVectorX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (PVectorX)PersistentCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> PVectorX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (PVectorX)PersistentCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> PVectorX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (PVectorX)PersistentCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }
    

    /**
     * coflatMap pattern, can be used to perform lazy reductions / collections / folds and other terminal operations
     * 
     * <pre>
     * {@code 
     *   
     *     PVectorX.of(1,2,3)
     *          .map(i->i*2)
     *          .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *      
     *     //PVectorX[12]
     * }
     * </pre>
     * 
     * 
     * @param fn mapping function
     * @return Transformed PVectorX
     */
    default <R> PVectorX<R> coflatMap(Function<? super PVectorX<T>, ? extends R> fn){
       return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }

    default PVector<T> toPVector() {
        return this;
    }

    @Override
    default <X> PVectorX<X> from(final Collection<X> col) {
        return fromCollection(col);
    }

    @Override
    default <T> Reducer<PVector<T>> monoid() {
        return Reducers.toPVector();
    }

    @Override
    default PVectorX<T> toPVectorX() {
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#reverse()
     */
    @Override
    default PVectorX<T> reverse() {
        return (PVectorX<T>) PersistentCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> filter(final Predicate<? super T> pred) {
        return (PVectorX<T>) PersistentCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
     */
    @Override
    default <R> PVectorX<R> map(final Function<? super T, ? extends R> mapper) {

        return (PVectorX<R>) PersistentCollectionX.super.map(mapper);
    }

    @Override
    default <R> PVectorX<R> unit(final Collection<R> col) {
        return fromCollection(col);
    }

    @Override
    default <R> PVectorX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> PVectorX<R> emptyUnit() {
        return empty();
    }

    @Override
    default <R> PVectorX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> PVectorX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (PVectorX<R>) PersistentCollectionX.super.flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limit(long)
     */
    @Override
    default PVectorX<T> limit(final long num) {
        return (PVectorX<T>) PersistentCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skip(long)
     */
    @Override
    default PVectorX<T> skip(final long num) {
        return (PVectorX<T>) PersistentCollectionX.super.skip(num);
    }

    @Override
    default PVectorX<T> takeRight(final int num) {
        return (PVectorX<T>) PersistentCollectionX.super.takeRight(num);
    }

    @Override
    default PVectorX<T> dropRight(final int num) {
        return (PVectorX<T>) PersistentCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> takeWhile(final Predicate<? super T> p) {
        return (PVectorX<T>) PersistentCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> dropWhile(final Predicate<? super T> p) {
        return (PVectorX<T>) PersistentCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> takeUntil(final Predicate<? super T> p) {
        return (PVectorX<T>) PersistentCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> dropUntil(final Predicate<? super T> p) {
        return (PVectorX<T>) PersistentCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> PVectorX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (PVectorX<R>) PersistentCollectionX.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
     */
    @Override
    default PVectorX<T> slice(final long from, final long to) {
        return (PVectorX<T>) PersistentCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> PVectorX<T> sorted(final Function<? super T, ? extends U> function) {
        return (PVectorX<T>) PersistentCollectionX.super.sorted(function);
    }

    @Override
    public PVectorX<T> plus(T e);

    @Override
    public PVectorX<T> plusAll(Collection<? extends T> list);

    @Override
    public PVectorX<T> with(int i, T e);

    @Override
    public PVectorX<T> plus(int i, T e);

    @Override
    public PVectorX<T> plusAll(int i, Collection<? extends T> list);

    @Override
    public PVectorX<T> minus(Object e);

    @Override
    public PVectorX<T> minusAll(Collection<?> list);

    @Override
    public PVectorX<T> minus(int i);

    @Override
    public PVectorX<T> subList(int start, int end);

    @Override
    default PVectorX<ListX<T>> grouped(final int groupSize) {
        return (PVectorX<ListX<T>>) PersistentCollectionX.super.grouped(groupSize);
    }

    @Override
    default <K, A, D> PVectorX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {
        return (PVectorX) PersistentCollectionX.super.grouped(classifier, downstream);
    }

    @Override
    default <K> PVectorX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return (PVectorX) PersistentCollectionX.super.grouped(classifier);
    }

    @Override
    default <U> PVectorX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (PVectorX) PersistentCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> PVectorX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (PVectorX<R>) PersistentCollectionX.super.zip(other, zipper);
    }



    @Override
    default <U, R> PVectorX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (PVectorX<R>) PersistentCollectionX.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#permutations()
     */
    @Override
    default PVectorX<ReactiveSeq<T>> permutations() {

        return (PVectorX<ReactiveSeq<T>>) PersistentCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#combinations(int)
     */
    @Override
    default PVectorX<ReactiveSeq<T>> combinations(final int size) {

        return (PVectorX<ReactiveSeq<T>>) PersistentCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#combinations()
     */
    @Override
    default PVectorX<ReactiveSeq<T>> combinations() {

        return (PVectorX<ReactiveSeq<T>>) PersistentCollectionX.super.combinations();
    }

    @Override
    default PVectorX<ListX<T>> sliding(final int windowSize) {
        return (PVectorX<ListX<T>>) PersistentCollectionX.super.sliding(windowSize);
    }

    @Override
    default PVectorX<ListX<T>> sliding(final int windowSize, final int increment) {
        return (PVectorX<ListX<T>>) PersistentCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default PVectorX<T> scanLeft(final Monoid<T> monoid) {
        return (PVectorX<T>) PersistentCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> PVectorX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (PVectorX<U>) PersistentCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default PVectorX<T> scanRight(final Monoid<T> monoid) {
        return (PVectorX<T>) PersistentCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> PVectorX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (PVectorX<U>) PersistentCollectionX.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#plusInOrder(java.lang.Object)
     */
    @Override
    default PVectorX<T> plusInOrder(final T e) {

        return (PVectorX<T>) PersistentCollectionX.super.plusInOrder(e);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(int)
     */
    @Override
    default PVectorX<T> cycle(final int times) {

        return (PVectorX<T>) PersistentCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    default PVectorX<T> cycle(final Monoid<T> m, final int times) {

        return (PVectorX<T>) PersistentCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (PVectorX<T>) PersistentCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (PVectorX<T>) PersistentCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipStream(java.util.stream.Stream)
     */
    @Override
    default <U> PVectorX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (PVectorX) PersistentCollectionX.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> PVectorX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (PVectorX) PersistentCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> PVectorX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (PVectorX) PersistentCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipWithIndex()
     */
    @Override
    default PVectorX<Tuple2<T, Long>> zipWithIndex() {

        return (PVectorX<Tuple2<T, Long>>) PersistentCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#distinct()
     */
    @Override
    default PVectorX<T> distinct() {

        return (PVectorX<T>) PersistentCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted()
     */
    @Override
    default PVectorX<T> sorted() {

        return (PVectorX<T>) PersistentCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default PVectorX<T> sorted(final Comparator<? super T> c) {

        return (PVectorX<T>) PersistentCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> skipWhile(final Predicate<? super T> p) {

        return (PVectorX<T>) PersistentCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> skipUntil(final Predicate<? super T> p) {

        return (PVectorX<T>) PersistentCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> limitWhile(final Predicate<? super T> p) {

        return (PVectorX<T>) PersistentCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> limitUntil(final Predicate<? super T> p) {

        return (PVectorX<T>) PersistentCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default PVectorX<T> intersperse(final T value) {

        return (PVectorX<T>) PersistentCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle()
     */
    @Override
    default PVectorX<T> shuffle() {

        return (PVectorX<T>) PersistentCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipLast(int)
     */
    @Override
    default PVectorX<T> skipLast(final int num) {

        return (PVectorX<T>) PersistentCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitLast(int)
     */
    @Override
    default PVectorX<T> limitLast(final int num) {

        return (PVectorX<T>) PersistentCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default PVectorX<T> onEmptySwitch(final Supplier<? extends PVector<T>> supplier) {
        if (this.isEmpty())
            return PVectorX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default PVectorX<T> onEmpty(final T value) {

        return (PVectorX<T>) PersistentCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default PVectorX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (PVectorX<T>) PersistentCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> PVectorX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (PVectorX<T>) PersistentCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle(java.util.Random)
     */
    @Override
    default PVectorX<T> shuffle(final Random random) {

        return (PVectorX<T>) PersistentCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> PVectorX<U> ofType(final Class<? extends U> type) {

        return (PVectorX<U>) PersistentCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default PVectorX<T> filterNot(final Predicate<? super T> fn) {

        return (PVectorX<T>) PersistentCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#notNull()
     */
    @Override
    default PVectorX<T> notNull() {

        return (PVectorX<T>) PersistentCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default PVectorX<T> removeAll(final Stream<? extends T> stream) {

        return (PVectorX<T>) PersistentCollectionX.super.removeAll(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default PVectorX<T> removeAll(final Iterable<? extends T> it) {

        return (PVectorX<T>) PersistentCollectionX.super.removeAll(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default PVectorX<T> removeAll(final T... values) {

        return (PVectorX<T>) PersistentCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Iterable)
     */
    @Override
    default PVectorX<T> retainAll(final Iterable<? extends T> it) {

        return (PVectorX<T>) PersistentCollectionX.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.util.stream.Stream)
     */
    @Override
    default PVectorX<T> retainAll(final Stream<? extends T> seq) {

        return (PVectorX<T>) PersistentCollectionX.super.retainAll(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Object[])
     */
    @Override
    default PVectorX<T> retainAll(final T... values) {

        return (PVectorX<T>) PersistentCollectionX.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cast(java.lang.Class)
     */
    @Override
    default <U> PVectorX<U> cast(final Class<? extends U> type) {

        return (PVectorX<U>) PersistentCollectionX.super.cast(type);
    }


    @Override
    default <C extends Collection<? super T>> PVectorX<C> grouped(final int size, final Supplier<C> supplier) {

        return (PVectorX<C>) PersistentCollectionX.super.grouped(size, supplier);
    }

    @Override
    default PVectorX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (PVectorX<ListX<T>>) PersistentCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default PVectorX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (PVectorX<ListX<T>>) PersistentCollectionX.super.groupedStatefullyUntil(predicate);
    }

    @Override
    default PVectorX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (PVectorX<ListX<T>>) PersistentCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> PVectorX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (PVectorX<C>) PersistentCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends Collection<? super T>> PVectorX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (PVectorX<C>) PersistentCollectionX.super.groupedUntil(predicate, factory);
    }



}