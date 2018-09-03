package android.support.v4.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.collection.SimpleArrayMap;

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
public class ArrayMap<K, V> implements Map<K, V> {
    private final Map<K, V> map;

    public ArrayMap() {
        this.map = new androidx.collection.ArrayMap<>();
    }

    public ArrayMap(int capacity) {
        this.map = new androidx.collection.ArrayMap<>(capacity);
    }

    public ArrayMap(SimpleArrayMap map) {
        this.map = new androidx.collection.ArrayMap<>(map);
    }

    @RequiresApi(24)
    @Nullable
    @Override
    public V compute(K key, @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.compute(key, remappingFunction);
    }

    @RequiresApi(24)
    @Nullable
    @Override
    public V computeIfAbsent(K key, @NonNull Function<? super K, ? extends V> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @RequiresApi(24)
    @Nullable
    @Override
    public V computeIfPresent(K key, @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.computeIfPresent(key, remappingFunction);
    }

    @RequiresApi(24)
    @Override
    public void forEach(@NonNull BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    @RequiresApi(24)
    @Nullable
    @Override
    public V getOrDefault(@Nullable Object key, @Nullable V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Nullable
    @Override
    public V get(@Nullable Object key) {
        return map.get(key);
    }

    @RequiresApi(24)
    @Nullable
    @Override
    public V merge(K key, @NonNull V value, @NonNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return map.merge(key, value, remappingFunction);
    }

    @RequiresApi(24)
    @Nullable
    @Override
    public V putIfAbsent(@NonNull K key, @NonNull V value) {
        return map.putIfAbsent(key, value);
    }

    @Nullable
    @Override
    public V put(@NonNull K key, @NonNull V value) {
        return map.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Nullable
    @Override
    public V remove(@Nullable Object key) {
        return map.remove(key);
    }

    @RequiresApi(24)
    @Override
    public boolean remove(@Nullable Object key, @Nullable Object value) {
        return map.remove(key, value);
    }

    @RequiresApi(24)
    @Nullable
    @Override
    public V replace(@NonNull K key, @NonNull V value) {
        return map.replace(key, value);
    }

    @RequiresApi(24)
    @Override
    public boolean replace(K key, @Nullable V oldValue, V newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @RequiresApi(24)
    @Override
    public void replaceAll(@NonNull BiFunction<? super K, ? super V, ? extends V> function) {
        map.replaceAll(function);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return map.containsValue(value);
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public int size() {
        return map.size();
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return map.equals(obj);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
