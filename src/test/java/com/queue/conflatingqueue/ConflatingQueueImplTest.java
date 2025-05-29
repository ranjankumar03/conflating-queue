package com.queue.conflatingqueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConflatingQueueImplTest {

    private ConflatingQueue<String, Integer> queue;

    @BeforeEach
    public void setUp() {
        queue = new ConflatingQueueImpl<>();
    }

    // Basic KeyValue implementation for testing
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

        @Override
        public String toString() {
            return "{" + key + "=" + value + '}';
        }
    }

    @Test
    public void testOfferAndTake() throws InterruptedException {
        assertTrue(queue.isEmpty());
        queue.offer(new SimpleKeyValue<>("A", 1));
        queue.offer(new SimpleKeyValue<>("B", 2));
        assertFalse(queue.isEmpty());

        KeyValue<String, Integer> kv1 = queue.take();
        assertEquals("A", kv1.getKey());
        assertEquals(Integer.valueOf(1), kv1.getValue());

        KeyValue<String, Integer> kv2 = queue.take();
        assertEquals("B", kv2.getKey());
        assertEquals(Integer.valueOf(2), kv2.getValue());

        assertTrue(queue.isEmpty());
    }

    @Test
    public void testConflation() throws InterruptedException {
        queue.offer(new SimpleKeyValue<>("A", 1));
        queue.offer(new SimpleKeyValue<>("A", 2)); // should replace 1

        KeyValue<String, Integer> kv = queue.take();
        assertEquals("A", kv.getKey());
        assertEquals(Integer.valueOf(2), kv.getValue());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testMultipleKeysOrderPreserved() throws InterruptedException {
        queue.offer(new SimpleKeyValue<>("A", 1));
        queue.offer(new SimpleKeyValue<>("B", 2));
        queue.offer(new SimpleKeyValue<>("A", 3)); // replace A:1 with A:3

        assertEquals("B", queue.take().getKey());
        assertEquals("A", queue.take().getKey());
    }

    @Test
    public void testMarketDataScenarioWithExactSequence() throws InterruptedException {
        CountDownLatch consumerStartedLatch = new CountDownLatch(1);
        CountDownLatch firstValueReceivedLatch = new CountDownLatch(1);
        CountDownLatch allOffersCompleteLatch = new CountDownLatch(1);
        CountDownLatch finalBlockLatch = new CountDownLatch(1);

        List<KeyValue<String, Integer>> consumedValues = new CopyOnWriteArrayList<>();

        // Consumer Thread
        Thread consumer = new Thread(() -> {
            try {
                // Step 1: Consumer signals it's about to block on take()
                consumerStartedLatch.countDown();

                // First take() - should receive BTCUSD:7000
                KeyValue<String, Integer> value1 = queue.take();
                consumedValues.add(value1);
                firstValueReceivedLatch.countDown();

                // Wait for producer to complete all offers
                allOffersCompleteLatch.await();

                // Take remaining values
                consumedValues.add(queue.take()); // BTCUSD:7002
                consumedValues.add(queue.take()); // ETHUSD:250

                // Signal that we're about to block on the final take()
                finalBlockLatch.countDown();

                // This should block as queue is empty
                queue.take();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Producer Thread
        Thread producer = new Thread(() -> {
            try {
                // Wait for consumer to start
                consumerStartedLatch.await();
                Thread.sleep(100); // Ensure consumer is blocked

                // Step 2: Offer BTCUSD:7000
                queue.offer(new SimpleKeyValue<>("BTCUSD", 7000));

                // Wait for consumer to receive first value
                firstValueReceivedLatch.await();

                // Steps 3-5: Offer remaining values
                queue.offer(new SimpleKeyValue<>("BTCUSD", 7001));
                queue.offer(new SimpleKeyValue<>("ETHUSD", 250));
                queue.offer(new SimpleKeyValue<>("BTCUSD", 7002));

                allOffersCompleteLatch.countDown();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Start threads
        consumer.start();
        producer.start();

        // Wait for the final blocking state
        assertTrue(finalBlockLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for final state");

        // Verify results
        assertEquals(3, consumedValues.size(), "Should have consumed exactly 3 values");

        // Verify first value (BTCUSD:7000)
        assertEquals("BTCUSD", consumedValues.get(0).getKey());
        assertEquals(Integer.valueOf(7000), consumedValues.get(0).getValue());

        // Verify second value (ETHUSD:250 - conflated)
        assertEquals("ETHUSD", consumedValues.get(1).getKey());
        assertEquals(Integer.valueOf(250), consumedValues.get(1).getValue());

        // Verify third value (BTCUSD:7002)
        assertEquals("BTCUSD", consumedValues.get(2).getKey());
        assertEquals(Integer.valueOf(7002), consumedValues.get(2).getValue());

        // Verify queue is empty
        assertTrue(queue.isEmpty());
    }
}
