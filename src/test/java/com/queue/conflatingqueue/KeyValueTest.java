package com.queue.conflatingqueue;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KeyValueTest {

    @Test
    public void testSimpleKeyValue() {
        // Create test instance with String key and Integer value
        KeyValue<String, Integer> kv = new SimpleKeyValue<>("test", 123);

        // Test key retrieval
        assertEquals("test", kv.getKey());

        // Test value retrieval
        assertEquals(Integer.valueOf(123), kv.getValue());
    }

    @Test
    public void testNullValues() {
        // Test with null key and value
        KeyValue<String, Integer> kv = new SimpleKeyValue<>(null, null);

        assertNull(kv.getKey());
        assertNull(kv.getValue());
    }

    @Test
    public void testDifferentTypes() {
        // Test with different generic types
        KeyValue<Integer, String> kv = new SimpleKeyValue<>(42, "hello");

        assertEquals(Integer.valueOf(42), kv.getKey());
        assertEquals("hello", kv.getValue());
    }

    // Simple implementation for testing
    private static class SimpleKeyValue<K, V> implements KeyValue<K, V> {
        private final K key;
        private final V value;

        public SimpleKeyValue(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }
    }
}