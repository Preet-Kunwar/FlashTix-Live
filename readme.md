# 🎟️ FlashTix Live

### ⚡ Real-Time Ticketing Platform with API Gateway & Rate Limiting

<p align="center">
  <em>High-performance distributed system designed for extreme flash-sale traffic</em>
</p>

---

## 🚀 Overview

FlashTix is a **scalable, event-driven ticketing platform** built to handle **massive concurrent users during flash sales**.

It ensures:

- ⚡ Instant response (non-blocking APIs)
- 🔒 Zero data loss
- 🌐 Real-time updates across all clients
- 🛡️ Stability under heavy traffic

---

## ✨ Core Features

- 🚪 API Gateway with Redis-based Rate Limiting
- 🧠 Event-Driven Architecture (Apache Kafka)
- ⚡ Real-Time Updates using WebSockets (STOMP + SockJS)
- 🔄 Asynchronous Order Processing
- 🛡️ Fault Tolerance with Circuit Breaker (Resilience4j)
- 📄 Background PDF Ticket Generation (RabbitMQ)
- 📦 Object Storage with MinIO

---

## 🏗️ System Architecture

<p align="center">
  <img src="./assets/System-Architecture.png" width="900"/>
  <br/>
  <em>End-to-end distributed flow of FlashTix system</em>
</p>

---

## 🔄 End-to-End Request Flow

1. User clicks **Buy Ticket** on frontend
2. Request goes to **API Gateway**
3. Gateway applies **rate limiting (Redis)**
4. Request forwarded to backend service
5. Order published to **Kafka topic**
6. Consumer processes order asynchronously
7. Inventory updated in **MySQL (transaction-safe)**
8. Redis cache updated
9. WebSocket broadcasts live ticket count
10. RabbitMQ triggers PDF generation
11. PDF stored in **MinIO**
12. User downloads ticket from dashboard

---

## 🧰 Tech Stack

### ⚙️ Backend

- Java 17+
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA / Hibernate
- Resilience4j

### 🚪 API Gateway

- Spring Cloud Gateway
- Reactive Redis (Rate Limiting)

### 🧱 Infrastructure

- MySQL (Relational DB)
- Redis (Cache + Rate Limiting)
- Apache Kafka (Event Streaming)
- RabbitMQ (Background Jobs)
- MinIO (Object Storage)

### 🎨 Frontend

- React + Vite
- Axios (JWT interceptors)
- STOMP.js + SockJS

---

## ⚙️ Local Setup

### 1️⃣ Clone Repository

```bash
git clone https://github.com/YOUR_USERNAME/FlashTix-Live.git
cd FlashTix-Live
```
