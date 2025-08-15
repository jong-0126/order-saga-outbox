# Order Saga Outbox (Spring Boot × Kafka)

주문 → 재고 → 결제를 **이벤트**로 이어서 처리하는 **사가(Choreography) + Outbox 패턴** 예제입니다.
분산 트랜잭션(2PC) 없이 **최종 일관성**을 달성하고, 이벤트 유실을 Outbox로 막습니다.

## 아키텍처

```
Client ──POST /orders────────▶ [order-svc]
  DB Tx: 주문 저장 + Outbox(OrderCreated)
           │
           └─[OutboxPublisher @Scheduled]→ Kafka topic: saga.events
                 │
                 ├─▶ [inventory-svc] : OrderCreated 수신 → reserve()
                 │        └─ Outbox: InventoryReserved/Failed → Kafka
                 │
                 └─▶ [payment-svc]  : InventoryReserved 수신 → authorize()
                          └─ Outbox: PaymentAuthorized/Failed → Kafka

[order-svc]는 PaymentAuthorized/Failed 수신 → 주문 상태 전이(COMPLETED/CANCELLED)
```

## 기술 스택

* Spring Boot 3, Java 17, Maven 멀티모듈
* Spring Web / Data JPA / Validation / Kafka
* H2(in-memory), Lombok(선택)
* Docker Compose(Kafka/Zookeeper)

## 모듈 구조

```
root (packaging: pom)
 ├─ common-events        # 이벤트 봉투/타입/페이로드 DTO (POJO)
 ├─ order-svc            # 주문 API + 결과 수신
 ├─ inventory-svc        # 재고 예약 + 보상
 └─ payment-svc          # 결제 승인/실패
```

---

## 핵심 설계 포인트

* **Outbox 패턴**: 비즈니스 업데이트와 **같은 DB 트랜잭션**으로 이벤트를 `outbox` 테이블에 JSON으로 기록 → 별도 퍼블리셔가 Kafka로 전송 → 유실 방지.
* **사가(Choreography)**: 각 서비스가 **이벤트를 구독**하고 자신의 트랜잭션으로 처리, 실패 시 **보상 이벤트**로 롤백.
* **멱등성**: 주문 생성에 **Idempotency-Key** 헤더 지원(중복 요청 방지).
* **타입드 이벤트**: `EventEnvelope<T>` + payload DTO로 스키마 안정성 확보.

---

## 빠른 실행(스모크 테스트)

### 1) 선행 조건

* JDK 17, Maven 3.9+
* Docker Desktop (또는 docker compose)

### 2) Kafka/Zookeeper 띄우기 (예시 `docker-compose.yml`)

> 이미 갖고 있는 compose가 있으면 그걸 쓰세요. 핵심은 **내부: `kafka:29092`**, \*\*외부: `localhost:9092`\*\*입니다.

```yaml
services:
  zookeeper:
    image: bitnami/zookeeper:3.9
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
    ports: ["2181:2181"]

  kafka:
    image: bitnami/kafka:3.7
    depends_on: [zookeeper]
    ports: ["9092:9092"]
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_BROKER_ID=1
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_CFG_LISTENERS=INTERNAL://:29092,EXTERNAL://:9092
      - KAFKA_CFG_ADVERTISED_LISTENERS=INTERNAL://kafka:29092,EXTERNAL://localhost:9092
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
      - KAFKA_CFG_INTER_BROKER_LISTENER_NAME=INTERNAL
```

```bash
docker compose up -d
```

### 3) 서비스 실행

루트에서:

```bash
# 전체 빌드
mvn -q -DskipTests clean package

# 개별 실행(별 터미널 3개)
mvn -q -pl order-svc spring-boot:run
mvn -q -pl inventory-svc spring-boot:run
mvn -q -pl payment-svc spring-boot:run
```

### 4) 재고 시드 & 주문 생성

```bash
# 재고 시드
curl -X POST "http://localhost:8082/inventory/seed?productId=p-100&qty=10"

# 주문 생성
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: demo-401" \
  -d '{"productId":"p-100","quantity":2,"amount":20000}'
```

### 5) 이벤트 확인(옵션)

```bash
# 컨테이너 안에서 콘솔 소비자 실행
docker compose exec kafka bash -lc \
 "kafka-console-consumer.sh --bootstrap-server kafka:29092 --topic saga.events --from-beginning"
```

`OrderCreated → InventoryReserved → PaymentAuthorized` 순으로 보이면 OK.

---

## 주요 API

### order-svc

* `POST /orders`

  * Header: `Idempotency-Key`(선택)
  * Body: `{"productId":"p-100","quantity":2,"amount":20000}`
  * 결과: `OrderEntity`(NEW) + Outbox에 `OrderCreated`

### inventory-svc (테스트 편의)

* `POST /inventory/seed?productId=&qty=`
* `GET /inventory/{productId}`
* `POST /inventory/reserve?productId=&qty=`
* `POST /inventory/release?productId=&qty=`

---

## 이벤트 스키마

### EventEnvelope (POJO)

```java
class EventEnvelope<T> {
  String type;           // ex) "OrderCreated"
  String correlationId;  // ex) orderId
  T payload;             // ex) OrderCreatedPayload
  Instant createdAt;
}
```

### 예시 JSON

```json
{
  "type": "OrderCreated",
  "correlationId": "ord-123",
  "payload": {
    "orderId": "ord-123",
    "productId": "p-100",
    "quantity": 2,
    "amount": 20000,
    "v": "v1"
  },
  "createdAt": "2025-08-15T04:25:30.123Z"
}
```

### EventTypes

* `OrderCreated`
* `InventoryReserved`, `InventoryFailed`
* `PaymentAuthorized`, `PaymentFailed`
* `OrderCancelled`(옵션)

### Payload DTO (요약)

* `order.OrderCreatedPayload(orderId, productId, quantity, amount, v)`
* `inventory.InventoryReservedPayload(orderId, productId, qty, v)`
* `payment.PaymentAuthorizedPayload(orderId, amount, v)`
* 각 `FailedPayload(orderId, reason, v)`

---

## Outbox 테이블 (JPA)

* 컬럼: `id, aggregateId(orderId), type, payload(JSON), status(PENDING|SENT|FAILED), attempts, lastError, createdAt, sentAt`
* 퍼블리셔: `@Scheduled`가 `PENDING`을 읽어 Kafka로 전송 → 성공 시 `SENT`

---

## 설정 포인트

* 모든 서비스 `application.yml`:

  * `spring.kafka.bootstrap-servers: ${KAFKA_BOOTSTRAP:localhost:9092}`
  * `app.kafka.topic: saga.events`
  * `app.outbox.publish-interval-ms: 1000`
* 로컬 실행: `localhost:9092`
* 컨테이너 내부 통신: `kafka:29092`

---

## 트러블슈팅 메모

* **파라미터 이름 에러**: 컨트롤러에서 `@PathVariable("productId")`, `@RequestHeader(name="Idempotency-Key")`처럼 **이름 명시**.
  (또는 Maven `maven-compiler-plugin`에 `<parameters>true</parameters>` 설정)
* **YAML 파싱 오류**: `${KAFKA_BOOTSTRAP:localhost:9092}`는 **여러 줄 스타일**로 쓰기(한 줄 flow map 금지).
* **멀티모듈 버전 오류**: 루트 POM에서 `dependencyManagement`/`pluginManagement`로 **중앙관리**하고 자식에서 버전 생략.
* **Kafka 접속 불가**: 내부와 외부 리스너/주소(29092/9092) 구분 확인.
