/*
 * Copyright 2021 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.api.data;

import internal.toolkit.base.api.data.InternalSeq;
import internal.toolkit.base.api.data.InternalSeqCursor;
import nbbrd.design.Development;
import nbbrd.design.NonNegative;
import lombok.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Describes a generic sequence of elements.
 *
 * @param <E>
 * @author Philippe Charles
 */
@Development(status = Development.Status.Release)
public interface Seq<E> extends BaseSeq, Iterable<E> {

    /**
     * Returns the value at the specified index. An index ranges from zero to
     * <tt>length() - 1</tt>. The first value of the sequence is at index zero,
     * the next at index one, and so on, as for array indexing.
     *
     * @param index the index of the value to be returned
     * @return the specified value
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is
     *                                   negative or not less than <tt>length()</tt>
     */
    E get(@NonNegative int index) throws IndexOutOfBoundsException;

    @Override
    default @NonNull SeqCursor<E> cursor() {
        return new InternalSeqCursor.DefaultSeqCursor(this);
    }

    @Override
    default Iterator<E> iterator() {
        return new InternalSeq.SequenceIterator(this);
    }

    @Override
    default void forEach(Consumer<? super E> action) {
        InternalSeq.forEach(this, action);
    }

    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), length(), 0);
    }

    /**
     * Returns a sequential Stream with this sequence as its source.
     *
     * @return a sequential Stream over the elements in this sequence
     * @see Collection#stream()
     */
    default @NonNull Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns an array containing all of the elements in this sequence,
     * using the provided generator function to allocate the returned array.
     *
     * @param generator a function which produces a new array of the desired type and the provided length
     * @return an array containing all of the elements in this sequence
     * @see Collection#toArray(IntFunction)
     */
    default @NonNull E[] toArray(@NonNull IntFunction<E[]> generator) {
        return InternalSeq.toArray(this, generator);
    }

    /**
     * Returns a modifiable list containing all the elements of this sequence.
     * This method is equivalent to <code>seq.stream().collect(Collectors.toList())</code>
     *
     * @return a non-null list containing all the elements of this sequence
     */
    default @NonNull List<E> toList() {
        return InternalSeq.toList(this);
    }

    default int indexOf(@NonNull Predicate<? super E> predicate) {
        return InternalSeq.firstIndexOf(this, predicate);
    }
}
