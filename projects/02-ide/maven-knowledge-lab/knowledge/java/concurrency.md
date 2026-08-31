# Java Concurrency Essentials

Java provides built-in multithreading capabilities via the `java.lang.Thread` class and the `java.util.concurrent` package.

## Synchronization and Locks
The `synchronized` keyword enforces mutual exclusion for methods or blocks.
ReentrantLock provides explicit locking mechanisms with fairness options and interruptible lock acquisition.

## Thread Safety Constructs
- **Atomic Variables**: AtomicInteger and AtomicReference use hardware-level Compare-And-Swap (CAS) instructions.
- **Concurrent Collections**: ConcurrentHashMap uses fine-grained bucket locking for high throughput without blocking read operations.
- **Executors Framework**: ExecutorService manages thread pools efficiently to decouple task submission from thread management.
