package com.queue.conflatingqueue;


/**
 *
 * A ConflatingQueue is an unbounded, blocking, data structure that holds item of the type @{@link KeyValue <K, V>}.
 * It can be considered FIFO with respect to its items' keys, but not necessarily its items' values. A ConflatingQueue
 * does not support null items.
 *
 * A ConflatingQueue is thread-safe and supports multiple producers and multiple consumers.  Each item on the queue is made
 * available to at most one single consumer.
 *
 * A ConflatingQueue would typically be used as an interface between a high frequency producer and low frequency consumer,
 * where it is still meaningful to process the most recent value for each key. For example:
 *
 * 		The producer might be a market data feed handler connected to a high frequency cryptocurrency exchange that is
 * 		streaming last traded price updates per symbol.  The consumer might be a trading system that needs to react to the
 * 		latest market data updates per symbol.
 *
 * A typical sequence of events might be:
 *
 * 		1. The consumer calls take(), and blocks as the queue is empty.
 * 		2. The producer calls offer(@{@link KeyValue}) with key = BTCUSD, value = 7000.  The consumer unblocks and receives
 * 		   the @{@link KeyValue}.
 * 		3. The producer calls offer(@{@link KeyValue}) with key = BTCUSD, value = 7001
 * 		4. The producer calls offer(@{@link KeyValue}) with key = ETHUSD, value = 250
 * 		5. The producer calls offer(@{@link KeyValue}) with key = BTCUSD, value = 7002, which replaces the queued price 7001
 * 		6. The consumer calls take() three times.  The consumer first receives key = BTCUSD, value = 7002, then receives
 * 		   key = ETHUSD, value = 250, then blocks as the queue is empty.
 *
 * @param <K> the type of the "key" part of the @{@link KeyValue} that this queue holds
 * @param <V> the type of the "value" part of the @{@link KeyValue} that this queue holds
 */
public interface ConflatingQueue<K, V> {

	/**
	 * Adds a key value item to the queue.
	 *
	 * If a @{{@link KeyValue} already exists in the queue with the same key, then the old @{{@link KeyValue} in the queue is updated
	 * in place with the new keyValue. The order of the new keyValue within the queue should be the same as the old @{{@link KeyValue}.
	 *
	 * If no @{{@link KeyValue} item exists in the queue with the same key, the keyValue is added to the end of the queue.
	 *
	 * @param keyValue the key value item to add to the queue
	 * @return true if the key value item was successfully added to the queue, false otherwise
	 * @throws NullPointerException if keyValue is null
	 */
	boolean offer(KeyValue<K, V> keyValue);

	/**
	 * Removes the first key value item in the queue, blocking if the queue is empty.
	 *
	 * @return the first key value item in the queue
	 * @throws InterruptedException if the thread was interrupted while waiting for a key value item to be added to the queue
	 */
	KeyValue<K, V> take() throws InterruptedException;

	/**
	 * Checks whether the queue is currently empty
	 *
	 * @return true if the queue is currently empty, false otherwise
	 */
	boolean isEmpty();

}
