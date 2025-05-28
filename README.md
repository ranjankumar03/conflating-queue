# ConflatingQueue

A Java implementation of a thread-safe, unbounded, blocking queue for key-value pairs, supporting conflation (replacement) of values for the same key. This is useful for scenarios like market data feeds, where only the latest value per key is relevant.

## Features

- **Conflation:** If a new value is offered for an existing key, the old value is replaced in the queue.
- **Thread-safe:** Supports multiple producers and consumers.
- **Blocking:** Consumers block on `take()` if the queue is empty.
- **FIFO (per key):** Maintains order for unique keys.

---

## Prerequisites

- **Java 21** (used for this project) or higher installed on your system.
- **Gradle 8.14** (used for this project).
- A terminal or IDE to run the application.

---

## Setup Instructions

1. Clone the project repository.
2. Navigate to the project directory:
   ```bash
   cd ..\conflating-queue\
   ```
3. Compile the project:
   ```bash
   ./gradlew build --refresh-dependencies
   ```
4. Running the test:
   ```bash
   ./gradlew clean test
   ```
---

## Project Structure

```
src/
 └── main/
     └── java/
         └── com/
             └── queue/
                 └── conflatingqueue/
                     ├── ConflatingQueue.java
                     ├── ConflatingQueueImpl.java
                     └── KeyValue.java
 └── test/
     └── java/
         └── com/
             └── queue/
                 └── conflatingqueue/
                     └── [Test Classes]
build.gradle
README.md
```

## Key Classes

- **ConflatingQueue&lt;K, V&gt;:** Interface defining the queue contract.
- **ConflatingQueueImpl&lt;K, V&gt;:** Implementation using `ConcurrentHashMap` and `LinkedBlockingQueue`.
- **KeyValue&lt;K, V&gt;:** Interface for key-value pairs.

## Usage Example

```java
ConflatingQueue<String, Integer> queue = new ConflatingQueueImpl<>();
queue.offer(new SimpleKeyValue<>("BTCUSD", 7000));
KeyValue<String, Integer> value = queue.take();
```

## Running Tests

This project uses JUnit 5. To run tests:

```
./gradlew test
```

## License

This project is licensed under the **conflating-queue License**.

---
