# AGENTS

## Project overview
- Kotlin + Spring Boot 4 (Gradle Kotlin DSL) with Spring Batch and Spring Data JPA.
- Main batch job manages `order_item` records (setup, hold lock, delete, cleanup).
- MySQL is expected locally on `localhost:13306` (root/root).

## Key paths
- `src/main/kotlin/io/github/batch/BatchApplication.kt`: Spring Boot entrypoint.
- `src/main/kotlin/io/github/batch/orderitem/batch/OrderItemJobConfig.kt`: Batch job wiring.
- `src/main/kotlin/io/github/batch/orderitem/batch/support/`: Tasklets and writer.
- `src/main/kotlin/io/github/batch/orderitem/domain/OrderItem.kt`: JPA entity.
- `src/main/resources/application.yml`: datasource config.
- `src/test/kotlin/io/github/batch/orderitem/OrderItemRepositoryTest.kt`: JPA + JDBC test.

## Local setup
- Start MySQL via Docker:
  - `docker compose -f docker/dockerFile.yml up -d`
- App config uses `jdbc:mysql://localhost:13306` with `root/root`.

## Common commands
- Run the app:
  - `./gradlew bootRun`
- Run tests:
  - `./gradlew test`

## Batch job notes
- Job name: `orderItemLockWaitJob`.
- Steps: setup -> hold lock -> partitioned delete -> cleanup.
- Job parameters (with defaults):
  - `rowCount` (default 200000): seed size for `order_item`.
  - `lockMinOrderId` (default 1) / `lockMaxOrderId` (default 100): range to lock.
  - `holdMillis` (default 15000): how long to sleep during lock step.
- Example launch args:
  - `--spring.batch.job.name=orderItemLockWaitJob --rowCount=50000 --lockMinOrderId=1 --lockMaxOrderId=100 --holdMillis=5000`

## Testing notes
- Tests expect a running MySQL instance matching `application.yml`.
- `OrderItemRepositoryTest` creates and drops `order_item` table each run.
