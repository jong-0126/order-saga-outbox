# Order Saga Outbox (Spring Boot + Kafka)

**주문 → 재고 → 결제**를 이벤트로 이어붙인 **사가(Choreography) + Outbox 패턴** 예제입니다.
각 서비스는 자신 DB 트랜잭션 안에서 **비즈니스 변경 + 이벤트 기록(Outbox)** 를 원자적으로 커밋하고,
별도의 **퍼블리셔(@Scheduled)** 가 Outbox의 `PENDING` 레코드를 **Kafka** 로 발행합니다.
최종적으로 주문 서비스가 결과 이벤트를 수신해 **COMPLETED / CANCELLED** 로 상태를 전이합니다.

---

## 모듈 구조

```
order-saga-outbox
├── pom.xml                # parent (dependencyManagement, pluginManagement)
├── common-events          # 이벤트 봉투/타입/페이로드 DTO (Lombok POJO)
├── order-svc              # 주문 API, 결과 리스너, Outbox 퍼블리셔
├── inventory-svc          # OrderCreated 리스너 → 재고 예약 → Outbox
└── payment-svc            # InventoryReserved 리스너 → 결제 승인/실패 → Outbox
```

---

## 기술 스택

* Spring Boot 3 (Web, Data JPA, Validation, Scheduling)
* Kafka (spring-kafka)
* H2 (in-memory DB) / JPA(Hibernate)
* Jackson, Lombok
* Maven 멀티모듈

---

## 아키텍처 한눈에 보기 (ASCII)

```
Client
  │  POST /orders (Idempotency-Key?)
  ▼
order-svc ──[DB 트랜잭션]──> orders: NEW
    │                         outbox: PENDING (OrderCreated)
    ├── @Scheduled Publisher ────────► Kafka topic: saga.events
    │
    └── @KafkaListener (PaymentAuthorized/Failed, InventoryFailed)
          └─ transition(): NEW → COMPLETED / CANCELLED
             + outbox 최종 이벤트 (OrderCompleted/Cancelled)
                (퍼블리셔가 Kafka로 발행)

inventory-svc (@KafkaListener: OrderCreated)
  ├─ reserve() 성공 → outbox: InventoryReserved (PENDING)
  ├─ 실패          → outbox: InventoryFailed  (PENDING)
  └─ @Scheduled Publisher → Kafka

payment-svc (@KafkaListener: InventoryReserved)
  ├─ authorize() 성공 → outbox: PaymentAuthorized (PENDING)
  ├─ 실패             → outbox: PaymentFailed     (PENDING)
  └─ @Scheduled Publisher → Kafka
```

> 핵심: **비즈니스 변경 + Outbox INSERT** 는 같은 DB 트랜잭션,
> Kafka 발행은 **분리된 스케줄러**가 수행(유실/중복 방지).

---

## 이벤트 계약 (Envelope & Payload)

```json
{
  "type": "OrderCreated",              // 이벤트 타입 문자열(상수로 관리)
  "correlationId": "ORDER-12345",      // 흐름 추적 키(주로 orderId)
  "createdAt": "2025-08-20T06:00:00Z",
  "payload": { /* 타입별 DTO 스키마 */ }
}
```

### 주요 타입

* `OrderCreated` → (inventory 처리) → `InventoryReserved` | `InventoryFailed`
* `InventoryReserved` → (payment 처리) → `PaymentAuthorized` | `PaymentFailed`
* 결과 수신 후 주문 서비스가 최종 전이:

  * `OrderCompleted` | `OrderCancelled`  *(들어온 타입을 그대로 재발행하지 않음)*

---

## 실행 방법

### 0) 필수 도구

* JDK 17
* Docker & Docker Compose
* postman

### Kafka 기동

```bash
docker compose up -d
# 또는 docker-compose up -d
```

> `KAFKA_BOOTSTRAP` 환경 값은 컨테이너 내부면 `kafka:29092`, 로컬 실행이면 `localhost:9092` 로 맞춰주세요.

기본 포트(예시):

* order-svc: `8080`
* inventory-svc: `8081`
* payment-svc: `8082`

---

## 빠른 시나리오 테스트

1. **재고 시드** (인벤토리 수량 가산)

```bash
curl -XPOST "http://localhost:8081/inventory/seed?productId=P1&qty=10"
```

2. **주문 생성**

```bash
curl -i -XPOST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: idem-001" \
  -d '{ "productId":"P1", "quantity":2, "amount":20000 }'
# 202 Accepted + Location: /orders/{id}
```

3. **상태 조회**

```bash
curl -s "http://localhost:8080/orders/{id}"
# NEW → (이벤트가 흐르면) COMPLETED 또는 CANCELLED
```

4. **H2 콘솔** (서비스별)

* `http://localhost:8080/h2-console` (order)
* `http://localhost:8081/h2-console` (inventory)
* `http://localhost:8082/h2-console` (payment)

  * JDBC URL 예: `jdbc:h2:mem:testdb`  (설정에 따름)
  * 사용자/비번: 기본은 빈 값 또는 `sa`

5. **Outbox 상태 확인 (order 예시)**

```sql
SELECT type, status, attempts, created_at, sent_at
FROM OUTBOX ORDER BY id DESC;
-- PENDING → SENT 로 바뀌면 Kafka 발행 성공
```

---

## API 요약

### 주문

* `POST /orders`

  * Request: `{"productId": "...", "quantity": 2, "amount": 20000}`
  * Headers: `Idempotency-Key` (중복 생성 방지, 선택)
  * Response: `202 Accepted`, `Location: /orders/{id}`, `X-Correlation-Id: {id}`
* `GET /orders/{id}`

  * Response: `200 OK` (DTO) | `404 Not Found`

### 인벤토리 (데모용)

* `POST /inventory/seed?productId=P1&qty=10` → 수량 가산(없으면 생성)

---

## 설정 예시 (`application.yml` 공통 패턴)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
    driverClassName: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
  h2:
    console:
      enabled: true
      path: /h2-console

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP:localhost:9092}

app:
  kafka:
    topic: saga.events
  outbox:
    publish-interval-ms: 1000
```
---

## 멱등성 / 상태 전이

* **Idempotency-Key** 로 동일 주문 요청 **중복 생성 방지** (DB `idempotency_key` Unique 권장)
* **상태 전이 가드**:

  * 이미 해당 상태면 `transition()` 조기 반환(루프/중복 방지)
  * `InventoryFailed`/`PaymentFailed` 수신 시 **항상 CANCELLED**

---

## 트러블슈팅 메모

1. **주문이 NEW에서 안 바뀜**

   * 각 서비스 메인 클래스에 `@EnableScheduling` 있는지
   * `spring.kafka.bootstrap-servers` / `app.kafka.topic` **세 서비스가 동일**한지
   * order 결과 리스너가 **최종 타입(OrderCompleted/Cancelled)** 으로 발행하는지

2. **Outbox가 계속 PENDING**

   * 퍼블리셔가 안 돎 → `@EnableScheduling` 확인
   * Kafka 연결 실패 → `KAFKA_BOOTSTRAP` 환경변수/포트 확인

3. **이벤트 루프/폭주**

   * 들어온 타입을 그대로 재발행하지 말 것
   * 결과 알림은 **최종 타입**으로만 발행 + 상태 가드

4. **컨트롤러 파라미터 바인딩 에러**

   * 컴파일러 옵션 `-parameters` (Maven `maven-compiler-plugin`에 `<release>17</release>`)

5. **POM 에러**

   * 멀티모듈: parent `dependencyManagement/pluginManagement` 설정, 하위 POM에서 버전 생략
   * 내부 모듈 의존 시 `<version>${project.version}</version>` 또는 생략(부모 관리)

6. **record 사용 시 컴파일 실패**

   * JDK 17 미설치/설정 문제 → POJO(Lombok)로 대체

7. **.gitignore**

   * `target/`, `**/target/` 커밋 금지 (Wrapper는 커밋)

---

## 내가 배운 것 

* **사가(Choreography) + Outbox + Kafka** 로 **최종 일관성** 구현
* DB 트랜잭션 안에서 **비즈니스 변경 + 이벤트 기록** 원자적 커밋
* **Idempotency-Key** 로 중복 주문 방지
* **타입드 이벤트**(Envelope + DTO)로 스키마 안정성
* 퍼블리셔/리스너 분리, **관측성**(Correlation-Id, SUB/PUB 로그)
* 멀티모듈 POM, JDK17, Spring 3, H2 콘솔, ResponseEntity/ProblemDetail 등 실전 운영 팁 습득
