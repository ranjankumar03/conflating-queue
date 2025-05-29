package com.queue.conflatingqueue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of a ConflatingQueue that allows conflating key-value pairs.
 * Based on specification mentioned in the ConflatingQueue interface, shared by XXX.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public class ConflatingQueueImpl<K, V> implements ConflatingQueue<K, V> {
    //Uses ConcurrentHashMap for efficient key-value storage and concurrent access
    private final ConcurrentHashMap<K, KeyValue<K, V>> map;
    //Uses LinkedBlockingQueue for FIFO ordering and blocking behavior
    private final LinkedBlockingQueue<KeyValue<K, V>> queue;
    //Uses AtomicBoolean for safe conflation operations
    private final AtomicBoolean conflating;

    public ConflatingQueueImpl() {
        this.map = new ConcurrentHashMap<>();
        this.queue = new LinkedBlockingQueue<>();
        this.conflating = new AtomicBoolean(false);
    }

    @Override
    public boolean offer(KeyValue<K, V> keyValue) {
        if (keyValue == null) {
            throw new NullPointerException("KeyValue cannot be null");
        }

        K key = keyValue.getKey();
        KeyValue<K, V> previous = map.put(key, keyValue);

        if (previous == null) {
            // New key, add to queue
            return queue.offer(keyValue);
        }

        // If the key already exists, we need to conflate
        while (!conflating.compareAndSet(false, true)) {
            Thread.yield();
        }

        try {
            // Remove old value if it's still in queue
            queue.remove(previous);
            // Add new value at the same position
            return queue.offer(keyValue);
        } finally {
            conflating.set(false);
        }
    }

    @Override
    public KeyValue<K, V> take() throws InterruptedException {
        // Wait for an item to be available, ie blocking behavior
        KeyValue<K, V> item = queue.take();
        map.remove(item.getKey());
        return item;
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}