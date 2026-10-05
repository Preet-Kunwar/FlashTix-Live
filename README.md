<div align="center">

  <!-- Animated / Aesthetic Header Badge -->
  <img src="https://img.shields.io/badge/⚡_HIGH--CONCURRENCY-EVENT_TICKETING_ENGINE-FF2E93?style=for-the-badge&labelColor=111827" alt="Header Badge" />

  <h1>🎟️ 𝙵𝚕𝚊𝚜𝚑𝚃𝚒𝚡 𝙻𝚒𝚟𝚎</h1>
  
  <p>
    <b><i>Zero crashes. Zero overselling. Built for the ultimate traffic storm.</i></b>
  </p>


  <p>
    <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/React-20232A?style=flat-square&logo=react&logoColor=61DAFB" alt="React" />
    <img src="https://img.shields.io/badge/Apache_Kafka-231F20?style=flat-square&logo=apachekafka&logoColor=white" alt="Kafka" />
    <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white" alt="Redis" />
    <img src="https://img.shields.io/badge/RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white" alt="RabbitMQ" />
    <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL" />
    <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white" alt="Docker" />
  </p>

  <sub>
    <a href="#-what-does-this-project-do-key-features">✨ Key Features</a> •
    <a href="#️-how-does-it-work">🛠️ Under the Hood</a> •
    <a href="#-architecture--flow">📐 Architecture</a> •
    <a href="#-tech-stack">🚀 Tech Stack</a>
  </sub>

</div>

<br />

> ### 💡 *The Problem We Solve*
> Have you ever tried to buy tickets to your favorite artist's concert, only to see the website crash because millions of people clicked <kbd>Buy Now</kbd> at the exact same second? 
> 
> **FlashTix Live** is a full-stack, high-performance event ticketing platform built specifically to survive extreme **"Flash Sale"** scenarios. Instead of freezing or crashing under massive bursts of web traffic, FlashTix Live gracefully queues orders, updates ticket counts in real-time without refreshing the page, and generates PDF tickets in the background—ensuring a buttery-smooth experience for real fans.

---

## ✨ What does this project do? `(Key Features)`

<table>
  <tr>
    <td width="50%" valign="top">
      <h3>🌪️ 1. High-Concurrency Purchasing</h3>
      <p>Handles thousands of simultaneous ticket purchases with ease. When a highly anticipated event goes live, orders are processed asynchronously. Users aren't left staring at a frozen checkout screen—they are instantly placed into a fast, orderly processing queue.</p>
    </td>
    <td width="50%" valign="top">
      <h3>📡 2. Live, Real-Time Ticket Counts</h3>
      <p>No need to mash <kbd>F5</kbd> to see if a concert is sold out. As soon as another user anywhere in the world grabs a ticket, the <code>Available Tickets</code> counter drops in real-time on everyone's screen via <b>WebSockets</b>.</p>
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>🛡️ 3. Smart Rate Limiting & Bot Shield</h3>
      <p>To keep things fair, the platform is armored against ticket-scalping bots. If a single IP tries to spam the server with hundreds of purchase requests per second, the <b>API Gateway</b> temporarily blocks them—saving server resources for actual humans.</p>
    </td>
    <td width="50%" valign="top">
      <h3>📄 4. Background PDF Generation</h3>
      <p>Buyers don't wait around on the checkout page for the system to draw their digital ticket. Orders are confirmed instantly, while a downloadable <b>PDF ticket</b> is quietly generated in the background and stored in cloud-ready object storage.</p>
    </td>
  </tr>
  <tr>
    <td colspan="2" valign="top">
      <h3>🔐 5. Secure User Accounts & Role Management</h3>
      <p>Powered by a complete <b>JWT-based authentication system</b>. Regular fans can register, log in securely, and view their order history and downloadable tickets, while <b>Administrators</b> get dedicated controls to launch new concert events and manage ticket pools.</p>
    </td>
  </tr>
</table>

---

## 🛠️ How does it work?

Building a platform that survives flash sales takes more than a basic CRUD database. Here is how **FlashTix Live** handles the heat under the hood:

* 🏎️ **Shock Absorption (`Apache Kafka`):** When 10,000 users click "Buy" at once, a traditional database melts trying to process payments and update tables simultaneously. FlashTix uses Kafka as a massive shock absorber—catching every incoming order instantly and forming an orderly, single-file line.
* 🔒 **Preventing Overselling (`Pessimistic Locking`):** How do you stop two people from buying the very last ticket at the exact same millisecond? We place a strict database-level lock on the row, forcing the system to completely finish User A's checkout before User B can even peek at the remaining inventory.
* ⚡ **Lightning-Fast Memory (`Redis`):** Querying the main SQL database every time someone loads the page is way too slow. Instead, we maintain a live ticket counter inside **Redis** (an ultra-fast, in-memory cache) so pages load instantaneously for everyone browsing.
* 📬 **Delegating Heavy Chores (`RabbitMQ` + `SeaweedFS`):** Rendering PDF files is CPU-heavy work. Instead of bogging down the main server, we hand the job off to **RabbitMQ**, which dispatches it to background workers. Once rendered, the PDF is safely tucked away into **SeaweedFS** (an S3-compatible storage engine).

---

## 📐 Architecture & Flow

To see how the components of **FlashTix Live** interact during peak traffic spikes, explore the system blueprints below:

### 🏛️ 1. System Architecture
<div align="center">
  <img src="/Diagram/System-Architecture.png" alt="System Architecture" width="90%" />
</div>

> 📌 **What this shows:** A high-level overview of the entire **FlashTix Live** ecosystem. Incoming traffic hits the **API Gateway** and routes to the **Spring Boot** backend. Notice the dual-queue design: **Apache Kafka** absorbs the massive shockwave of incoming orders, while **RabbitMQ** offloads heavy PDF rendering to background workers.

<br />

### ⏱️ 2. Sequence Diagram: The Ticket Purchase Flow
<div align="center">
  <img src="/Diagram/sequence-diagram.png" alt="Sequence Diagram" width="90%" />
</div>

> 📌 **What this shows:** The exact millisecond-by-millisecond timeline of a flash sale checkout. Follow the asynchronous journey from the user's initial <kbd>Buy</kbd> click, passing through Redis rate-limiters, entering the Kafka stream, and finally pushing a live WebSocket notification back to the React frontend.

<br />

### 🗄️ 3. Entity-Relationship (ER) Diagram
<div align="center">
  <img src="/Diagram/Entity-Relation.png" alt="ER Diagram" width="90%" />
</div>

> 📌 **What this shows:** The core data schema inside our **MySQL 8.0** database. It maps out the exact relationships between `Users`, `Events`, `Orders`, and `Tickets`, highlighting where **pessimistic locking** is enforced to guarantee zero overselling.

---

## 🚀 Tech Stack

<div align="center">

| Layer | Technology | Role in FlashTix |
| :--- | :--- | :--- |
| 🎨 **Frontend** | ![React](https://img.shields.io/badge/React-20232A?style=flat-square&logo=react&logoColor=61DAFB) ![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white) | Ultra-fast UI & real-time client updates |
| ⚙️ **Backend Core** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white) ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white) | Core business logic & concurrency control |
| 🚪 **API Gateway** | ![Spring Cloud](https://img.shields.io/badge/Spring_Cloud_Gateway-6DB33F?style=flat-square&logo=spring&logoColor=white) | Routing, security & IP rate-limiting |
| 🗄️ **Database** | ![MySQL](https://img.shields.io/badge/MySQL_8.0-4479A1?style=flat-square&logo=mysql&logoColor=white) | Persistent relational storage & row locking |
| ⚡ **In-Memory Cache** | ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white) | Live ticket counters & rate-limit state |
| 🌊 **Event Streaming** | ![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=flat-square&logo=apachekafka&logoColor=white) | High-traffic flash-sale order buffering |
| 🐇 **Message Broker** | ![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white) | Asynchronous background PDF job queue |
| ☁️ **Object Storage** | ![SeaweedFS](https://img.shields.io/badge/SeaweedFS_(S3)-009639?style=flat-square&logo=amazon-s3&logoColor=white) | Scalable cloud-like storage for PDF tickets |
| 📡 **Real-Time Comms** | ![WebSockets](https://img.shields.io/badge/WebSockets-010101?style=flat-square&logo=socket.io&logoColor=white) | Instant live inventory broadcasts |
| 🐳 **Infrastructure** | ![Docker](https://img.shields.io/badge/Docker_Compose-2496ED?style=flat-square&logo=docker&logoColor=white) | One-click multi-container deployment |

</div>

---

<div align="center">
  <sub>Built with ❤️ to keep concert ticketing fast, fair, and crash-free.</sub>
</div>