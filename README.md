# banking-transaction-engine
This project is a scalable, event-driven Core Banking API designed to handle high-concurrency financial transactions securely. It implements distributed system patterns to prevent race conditions, double-spending, and database deadlocks.

Key Features Implemented:
Concurrency Control: Implemented Pessimistic Locking (@Lock) with deterministic lock ordering (smaller ID first) to completely avoid database deadlocks during wallet-to-wallet transfers.

Idempotency (Double-Payment Prevention): Integrated Redis (setIfAbsent) to create distributed locks, ensuring duplicate payment requests are blocked instantly before hitting the database.

Event-Driven Architecture: Utilized Spring's @TransactionalEventListener and Apache Kafka to decouple core transaction logic from background tasks (like notifications) strictly after database commits.

ACID Compliance: Leveraged Spring's @Transactional to guarantee "All-or-Nothing" operations for financial data integrity.
