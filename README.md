# task-scheduler

Monorepo de microsserviços Java/Spring Boot para um agendador de tarefas, organizado em um único repositório.

## Visão Geral

Cada microsserviço é um projeto Maven independente dentro de `services/`. A infraestrutura compartilhada (PostgreSQL e Redis) é orquestrada por um `docker-compose.yaml` na raiz.

## Estrutura do Monorepo

```
task-scheduler/
├── docker-compose.yaml      # Postgres + Redis compartilhados
├── .env                     # Variáveis de ambiente (não versionado)
├── .gitignore
├── .gitattributes
└── services/
    └── user-service/        # Microsserviço de usuários (Maven)
```

## Stack Tecnológico (geral)

- **Linguagem**: Java 25
- **Framework base**: Spring Boot 4.1.1
- **Build**: Maven
- **Persistência**: PostgreSQL 17 (Spring Data JPA)
- **Cache**: Redis 8 (Spring Data Redis)
- **Mensageria HTTP**: Spring Cloud OpenFeign
- **Documentação de API**: SpringDoc OpenAPI (Swagger UI)
- **Containerização**: Docker + Docker Compose

## Microsserviços

| Serviço | Descrição | Porta |
|---|---|---|
| `user-service` | Cadastro, autenticação e gerenciamento de usuários (com endereços e telefones) | `${API_PORT_USER:8080}` |

## Pré-requisitos

- Docker e Docker Compose
- Java 25 + Maven (somente se for rodar os serviços fora do Docker)

## Como Rodar

### 1. Subir a infraestrutura

```bash
docker compose up -d
```

Sobe:

- **PostgreSQL 17** (`user-service-db`) — porta `${DB_PORT_USER:5433}`, com healthcheck
- **Redis 8** (`user-service-redis`) — porta `${REDIS_PORT_USER:6380}`, autenticado por senha, com healthcheck

Ambos na network `user-service-network`.

### 2. Configurar variáveis de ambiente

Copie/ajuste o `.env` na raiz com as variáveis listadas em [Variáveis de Ambiente](#variáveis-de-ambiente).

### 3. Subir um serviço

Exemplo para `user-service`:

```bash
cd services/user-service
./mvnw spring-boot:run
```

Ou via jar:

```bash
./mvnw clean package
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

Swagger UI: `http://localhost:${API_PORT_USER:8080}/swagger-ui.html`

## Variáveis de Ambiente

Definidas em `.env` na raiz e consumidas pelos serviços via `spring.config.import: optional:file:.env[.properties]`.

| Variável | Descrição | Default |
|---|---|---|
| `DB_HOST` | Host do PostgreSQL | `localhost` |
| `DB_PORT_USER` | Porta do PostgreSQL | `5433` |
| `DB_DATABASE_USER` | Nome do banco | `user-service-db` |
| `DB_USERNAME_USER` | Usuário do banco | `user_service` |
| `DB_PASSWORD_USER` | Senha do banco | — |
| `REDIS_HOST` | Host do Redis | `localhost` |
| `REDIS_PORT_USER` | Porta do Redis | `6380` |
| `REDIS_PASSWORD_USER` | Senha do Redis | — |
| `SECRET_KEY` | Chave base64 usada para assinar os tokens JWT | — |
| `API_PORT_USER` | Porta HTTP do `user-service` | `8080` |
| `VIA_CEP_URL` | URL base da API ViaCEP | `https://viacep.com.br` |

> O `.env` está listado no `.gitignore`. Versionar apenas o `.env.example`.

## Decisões de Arquitetura

- **Monorepo com `services/`**: cada microsserviço é um projeto Maven isolado em sua própria pasta, mas compartilha o mesmo repositório git e a mesma infraestrutura Docker.
- **Infra compartilhada via Compose raiz**: PostgreSQL e Redis sobem juntos em uma network dedicada (`user-service-network`), evitando duplicação de containers entre serviços.
- **Config externalizada por `.env` central**: o `application.yaml` de cada serviço importa `optional:file:.env[.properties]`, então um único `.env` na raiz alimenta todos os serviços.
- **Healthchecks na infra**: Postgres usa `pg_isready`; Redis autentica via `REDISCLI_AUTH` antes de pingar.
