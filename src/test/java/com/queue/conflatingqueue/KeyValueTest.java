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

    @Test
    public void testImmutability() {
        KeyValue<String, String> kv = new SimpleKeyValue<>("immutable", "value");
        // There are no setters, so values should not change
        assertEquals("immutable", kv.getKey());
        assertEquals("value", kv.getValue());
    }

    @Test
    public void testSameKeyDifferentValues() {
        KeyValue<String, Integer> kv1 = new SimpleKeyValue<>("key", 1);
        KeyValue<String, Integer> kv2 = new SimpleKeyValue<>("key", 2);
        assertEquals("key", kv1.getKey());
        assertEquals("key", kv2.getKey());
        assertNotEquals(kv1.getValue(), kv2.getValue());
    }

    @Test
    public void testEmptyStringKeyAndValue() {
        KeyValue<String, String> kv = new SimpleKeyValue<>("", "");
        assertEquals("", kv.getKey());
        assertEquals("", kv.getValue());
    }

    @Test
    public void testLargeObjects() {
        String largeKey = "k".repeat(10000);
        String largeValue = "v".repeat(10000);
        KeyValue<String, String> kv = new SimpleKeyValue<>(largeKey, largeValue);
        assertEquals(largeKey, kv.getKey());
        assertEquals(largeValue, kv.getValue());
    }

    @Test
    public void testKeyValueWithCustomObject() {
        class Custom {
            int id;
            Custom(int id) { this.id = id; }
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Custom custom = (Custom) o;
                return id == custom.id;
            }
        }
        Custom key = new Custom(5);
        Custom value = new Custom(10);
        KeyValue<Custom, Custom> kv = new SimpleKeyValue<>(key, value);
        assertEquals(key, kv.getKey());
        assertEquals(value, kv.getValue());
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