package org.optaplanner.core.impl.domain.solution.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Thread-safe basic cache implementation.
 *
 * @param <K> the type of keys for the cache
 * @param <V> the type of values stored in the cache
 * */
public final class ConcurrentCache<K, V> implements Cache<K, V> {

    private final ConcurrentMap<K, V> cache = new ConcurrentHashMap<>();

    /**
     * {@link ConcurrentHashMap#computeIfAbsent(Object, Function)} should not be used to directly retrieve cached values
     * as it incorporates locking even when the value is present and doesn't have to be computed. Thus, the value is
     * retrieved by {@link ConcurrentHashMap#get(Object)} and iff the value is null,
     * {@link ConcurrentHashMap#computeIfAbsent(Object, Function)} is used to ensure thread-safety.
     *
     * {@inheritDoc}
     * */
    @Override
    public V retrieveOrComputeAndStore(K key, Function<? super K, ? extends V> mappingFunction) {
        V value = cache.get(key);
        if (value == null) {
            value = cache.computeIfAbsent(key, mappingFunction);
        }
        return value;
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public V put(final K key, final V value) {
        return cache.put(key, value);
    }
}
