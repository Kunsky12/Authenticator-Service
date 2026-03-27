# Authenticator-Service

A Spring Boot microservice for **com.mekheainteractive** that authenticates game clients via PlayFab session tickets and issues signed JWTs for use across the platform's backend services.

---

## How It Works

```
Game Client
    │
    │  POST /api/auth/login  { sessionTicket }
    ▼
authenticator-service
    │
    │  POST /Server/AuthenticateSessionTicket  (PlayFab API)
    ▼
PlayFab
    │
    │  returns PlayFabId
    ▼
authenticator-service
    │
    │  signs JWT (HS256, 12h expiry, sub = PlayFabId)
    ▼
Game Client  ←  { JWT }
```

1. The client sends a PlayFab `SessionTicket` obtained after login on the client SDK
2. The service verifies it against the PlayFab Server API using your Title's secret key
3. On success, a signed JWT is returned — this token is then used to authenticate against other services (e.g. ChatSystem)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.3 |
| Language | Java 17 |
| Security | Spring Security |
| HTTP Client | Spring WebFlux (`WebClient`) |
| JWT | JJWT 0.13.0 (HS256) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Utilities | Lombok |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL instance
- PlayFab title with Server API access

### Installation

```bash
git clone https://github.com/your-org/authenticator-service.git
cd authenticator-service
mvn install
```

### Environment Variables

The service reads secrets from environment variables. Set these before running:

| Variable | Description |
|---|---|
| `PLAYFAB_TITLE_ID` | Your PlayFab title ID |
| `PLAYFAB_SECRET_KEY` | Your PlayFab server secret key |
| `JWT_SECRET_KEY` | Secret used to sign JWTs (min. 32 chars for HS256) |

Example `.env` or shell export:

```bash
export PLAYFAB_TITLE_ID=ABCD1
export PLAYFAB_SECRET_KEY=your-playfab-secret
export JWT_SECRET_KEY=your-super-secret-jwt-key-32chars+
```

### Running

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn package
java -jar target/authenticator-service-0.0.1-SNAPSHOT.jar
```

The service starts on **port 8080**.

---

## API Reference

### `POST /api/auth/login`

Verifies a PlayFab session ticket and returns a signed JWT.

**Access:** Public (no auth required)

**Request body:**
```json
{
  "sessionTicket": "<playfab-session-ticket>"
}
```

**Response:**
```
eyJhbGciOiJIUzI1NiJ9...
```
A raw JWT string. The token's `sub` claim contains the player's `PlayFabId` and expires in **12 hours**.

**Error cases:**
- PlayFab returns an error or null → `500` / null body
- Invalid session ticket → PlayFab rejects it upstream

---

## JWT Token Structure

| Claim | Value |
|---|---|
| `sub` | Player's `PlayFabId` |
| `iat` | Issued-at timestamp |
| `exp` | Issued-at + 12 hours |
| Algorithm | HS256 |

Tokens issued by this service are consumed by other backend services (e.g. ChatSystem's WebSocket `auth` handshake).

---

## Security

- `/api/auth/login` is the only publicly accessible endpoint
- All other routes require a valid JWT (Spring Security)
- CSRF is disabled (stateless API)
- PlayFab API calls use the server-side `X-SecretKey` header — the secret never leaves the server

---

## Configuration Reference (`application.properties`)

```properties
spring.application.name=authenticator-service

# PlayFab
playfab.title-id=${PLAYFAB_TITLE_ID}
playfab.secret-key=${PLAYFAB_SECRET_KEY}

# JWT
JwtSecretKey=${JWT_SECRET_KEY}

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true

# Server
server.port=8080
```

---

## Project Structure

```
src/main/java/com/mekheainteractive/authenticator_service/
├── Controller/
│   └── AuthController.java       # POST /api/auth/login
├── Service/
│   ├── PlayFabService.java       # PlayFab session ticket verification
│   └── JwtService.java           # JWT generation & signing
└── Security/
    └── SecurityConfig.java       # Spring Security filter chain
```

---

## License

ISC
