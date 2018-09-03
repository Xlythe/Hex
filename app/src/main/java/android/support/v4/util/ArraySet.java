package android.support.v4.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Due to Google Play Services still depending on the old support libraries, and the old libraries
 * not being compile-time compatible with the new libraries, we have 'ported' the necessary classes
 * so that everything runs the latest and greatest code.
 *
 * If a runtime crash occurs, we may have missed a class that we had to port.
 *
 * Note that if/when Google Play Services updates to the new libraries, this compat files simply
 * won't be called any longer.
 */
public class ArraySet<E> implements Collection<E>, Set<E> {
    private final Set<E> set;

    public ArraySet() {
        this.set = new androidx.collection.ArraySet<>();
    }

    public ArraySet(int capacity) {
        this.set = new androidx.collection.ArraySet<>(capacity);
    }

    public ArraySet(@Nullable ArraySet<E> set) {
        this();
        if (set != null) {
            addAll(set);
        }
    }

    public ArraySet(@Nullable Collection<E> set) {
        this();
        if (set != null) {
            addAll(set);
        }
    }

    @Override
    public boolean add(E e) {
        return set.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return set.addAll(c);
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    @RequiresApi(24)
    @Override
    public Stream<E> parallelStream() {
        return set.parallelStream();
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    @RequiresApi(24)
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return set.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    @Override
    public int size() {
        return set.size();
    }

    @RequiresApi(24)
    @NonNull
    @Override
    public Spliterator<E> spliterator() {
        return set.spliterator();
    }

    @RequiresApi(24)
    @Override
    public Stream<E> stream() {
        return set.stream();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

    @RequiresApi(24)
    @Override
    public void forEach(Consumer<? super E> action) {
        set.forEach(action);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return set.equals(obj);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return set.toString();
    }
}
