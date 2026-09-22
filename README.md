# task-scheduler

Monorepo de microsserviços Java/Spring Boot para um agendador de tarefas, organizado em um único repositório.

## Visão Geral

Cada microsserviço é um projeto Maven independente dentro de `services/`. A infraestrutura compartilhada (PostgreSQL, MongoDB e Redis) é orquestrada por um `docker-compose.yaml` na raiz.

## Estrutura do Monorepo

```
task-scheduler/
├── docker-compose.yaml      # Postgres + MongoDB + Redis compartilhados
├── .env                     # Variáveis de ambiente (não versionado)
├── .gitignore
├── .gitattributes
└── services/
    ├── user-service/        # Microsserviço de usuários (Maven)
    └── scheduler-service/   # Microsserviço de tarefas (Maven)
```

## Stack Tecnológico (geral)

- **Linguagem**: Java 25
- **Framework base**: Spring Boot 4.1.1
- **Build**: Maven
- **Persistência relacional**: PostgreSQL 17 (Spring Data JPA)
- **Persistência documental**: MongoDB 8 (Spring Data MongoDB)
- **Cache**: Redis 8 (Spring Data Redis)
- **Mensageria HTTP**: Spring Cloud OpenFeign
- **Mapeamento**: MapStruct (usado no `scheduler-service`)
- **Documentação de API**: SpringDoc OpenAPI / Swagger UI (apenas no `user-service`)
- **Containerização**: Docker + Docker Compose

## Microsserviços

| Serviço | Descrição | Porta |
|---|---|---|
| `user-service` | Cadastro, autenticação e gerenciamento de usuários (com endereços e telefones) | `${API_PORT_USER:8080}` |
| `scheduler-service` | CRUD de tarefas e listagem por período; consome o `user-service` via OpenFeign para validar o usuário autenticado | `${API_PORT_SCHEDULER:8080}` |

## Pré-requisitos

- Docker e Docker Compose
- Java 25 + Maven (somente se for rodar os serviços fora do Docker)

## Como Rodar

### 1. Subir a infraestrutura

```bash
docker compose up -d
```

Sobe:

- **PostgreSQL 17** (`user-service-postgres`) — porta `${POSTGRES_PORT_USER:5433}`, com healthcheck (`pg_isready`)
- **MongoDB 8** (`scheduler-service-mongodb`) — porta `${MONGO_PORT_SCHEDULER:27017}`, com healthcheck (`mongosh ping`)
- **Redis 8** (`user-service-redis`) — porta `${REDIS_PORT_USER:6380}`, autenticado por senha, com healthcheck

Todos os containers compartilham a network default do Compose (resolução por nome dentro do projeto).

### 2. Configurar variáveis de ambiente

Copie/ajuste o `.env` na raiz com as variáveis listadas em [Variáveis de Ambiente](#variáveis-de-ambiente).

### 3. Subir os serviços

Como o `scheduler-service` depende do `user-service` para validar tokens, suba o `user-service` primeiro.

```bash
# terminal 1
cd services/user-service
./mvnw spring-boot:run

# terminal 2
cd services/scheduler-service
./mvnw spring-boot:run
```

Ou via jar:

```bash
./mvnw clean package
java -jar target/<artifactId>-0.0.1-SNAPSHOT.jar
```

Swagger UI do `user-service`: `http://localhost:${API_PORT_USER:8080}/swagger-ui.html`

## Variáveis de Ambiente

Definidas em `.env` na raiz e consumidas pelos serviços via `spring.config.import: optional:file:.env[.properties]`.

| Variável | Descrição | Default |
|---|---|---|
| `POSTGRES_HOST` | Host do PostgreSQL | `localhost` |
| `POSTGRES_PORT_USER` | Porta do PostgreSQL | `5433` |
| `POSTGRES_DATABASE_USER` | Nome do banco relacional | `user-service-db` |
| `POSTGRES_USERNAME_USER` | Usuário do PostgreSQL | `user_service` |
| `POSTGRES_PASSWORD_USER` | Senha do PostgreSQL | — |
| `MONGO_HOST` | Host do MongoDB | `localhost` |
| `MONGO_PORT_SCHEDULER` | Porta do MongoDB | `27017` |
| `MONGO_DATABASE_SCHEDULER` | Nome do banco documental | `scheduler-service-mongodb` |
| `MONGO_USERNAME_SCHEDULER` | Usuário do MongoDB | `scheduler-service` |
| `MONGO_PASSWORD_SCHEDULER` | Senha do MongoDB | — |
| `REDIS_HOST` | Host do Redis | `localhost` |
| `REDIS_PORT_USER` | Porta do Redis | `6380` |
| `REDIS_PASSWORD_USER` | Senha do Redis | — |
| `SECRET_KEY` | Chave base64 usada para assinar os tokens JWT | — |
| `API_PORT_USER` | Porta HTTP do `user-service` | `8080` |
| `API_PORT_SCHEDULER` | Porta HTTP do `scheduler-service` | `8080` |
| `USER_SERVICE_URL` | URL do `user-service` consumida pelo `scheduler-service` (Feign) | `http://localhost:8080` |
| `VIA_CEP_URL` | URL base da API ViaCEP (consumida pelo `user-service`) | `https://viacep.com.br` |

> O `.env` está listado no `.gitignore`. Versionar apenas o `.env.example`.

## Decisões de Arquitetura

- **Monorepo com `services/`**: cada microsserviço é um projeto Maven isolado em sua própria pasta, mas compartilha o mesmo repositório git e a mesma infraestrutura Docker.
- **Infra compartilhada via Compose raiz**: PostgreSQL, MongoDB e Redis sobem juntos, na network default do Compose, evitando duplicação de containers entre serviços.
- **Bancos por serviço**: `user-service` usa PostgreSQL (dados relacionais: usuários, endereços, telefones); `scheduler-service` usa MongoDB (tarefas como documentos). Cada container expõe apenas a porta do seu banco para o host.
- **Comunicação entre serviços via OpenFeign**: o `scheduler-service` valida o usuário autenticado chamando o `user-service` via Feign Client (`UsuarioClient`), com URL configurável em `external-services.user-service.url` (`USER_SERVICE_URL`).
- **Config externalizada por `.env` central**: o `application.yaml` de cada serviço importa `optional:file:.env[.properties]`, então um único `.env` na raiz alimenta todos os serviços.
- **Healthchecks na infra**: Postgres usa `pg_isready`; MongoDB usa `mongosh ping`; Redis autentica via `REDISCLI_AUTH` antes de pingar.
