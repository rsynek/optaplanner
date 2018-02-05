package org.optaplanner.core.impl.domain.solution.util;

import java.util.function.Function;

/**
 * Simple cache interface.
 *
 * @param <K> the type of keys for the cache
 * @param <V> the type of values stored in the cache
 * */
public interface Cache<K, V> {

    /**
     * Retrieves a value associated with the key. In case no such value exists, it is computed by the mappingFunction,
     * stored in the cache and retrieved.
     *
     * @param key key associated with the value
     * @param mappingFunction function to compute the value
     * @return retrieved (preferably) or computed value associated with the key
     * */
    V retrieveOrComputeAndStore(K key, Function<? super K, ? extends V> mappingFunction);

    /**
     * Stores a value associated with a key in the cache.
     *
     * @param key key to be associated with the value
     * @param value value to be stored
     * */
    V put(K key, V value);
}
