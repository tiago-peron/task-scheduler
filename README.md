# task-scheduler

Monorepo de microsserviços Java/Spring Boot para um agendador de tarefas, organizado em um único repositório.

## Visão Geral

Cada microsserviço é um projeto Maven independente dentro de `services/`, e o front (Angular) fica em `web/`. A infraestrutura compartilhada (PostgreSQL, MongoDB e Redis) é orquestrada por um `docker-compose.yaml` na raiz.

## Estrutura do Monorepo

```
task-scheduler/
├── docker-compose.yaml      # Postgres + MongoDB + Redis compartilhados
├── .env                     # Variáveis de ambiente (não versionado)
├── .gitignore
├── .gitattributes
├── services/
│   ├── user-service/        # Microsserviço de usuários (Maven)
│   ├── task-service/        # Microsserviço de tarefas (Maven)
│   ├── notification-service/# Microsserviço de envio de e-mails (Maven)
│   └── bff-task-scheduler/  # BFF que agrega user/task/notification para o front (Maven)
└── web/                     # Front-end Angular (pnpm)
```

## Stack Tecnológico (geral)

- **Linguagem**: Java 25
- **Framework base**: Spring Boot 4.1.2
- **Build**: Maven
- **Front-end**: Angular 22 + Angular Material (pnpm)
- **Persistência relacional**: PostgreSQL 17 (Spring Data JPA)
- **Persistência documental**: MongoDB 8 (Spring Data MongoDB)
- **Cache**: Redis 8 (Spring Data Redis)
- **Mensageria HTTP**: Spring Cloud OpenFeign
- **Mapeamento**: MapStruct (usado no `task-service`)
- **Envio de e-mail**: Spring Boot Mail + Thymeleaf (usados no `notification-service`)
- **Documentação de API**: SpringDoc OpenAPI / Swagger UI (`user-service` e `bff-task-scheduler`)
- **Containerização**: Docker (Dockerfile multi-stage por serviço) + Docker Compose

## Microsserviços

| Serviço | Descrição | Porta |
|---|---|---|
| `user-service` | Cadastro, autenticação e gerenciamento de usuários (com endereços e telefones) | `${API_PORT_USER}` |
| `task-service` | CRUD de tarefas e listagem por período; consome o `user-service` via OpenFeign para validar o usuário autenticado | `${API_PORT_TASK}` |
| `notification-service` | Envio de e-mails de notificação de tarefas, via SMTP (Gmail) com template Thymeleaf | `${API_PORT_NOTIFICATION}` |
| `bff-task-scheduler` | BFF (Backend for Frontend) que agrega `user`, `task` e `notification` via OpenFeign para consumo do front; expõe Swagger UI e roda um cron de notificações | `${API_PORT_BFF}` |

## Pré-requisitos

- Docker e Docker Compose
- Java 25 + Maven (somente se for rodar os serviços fora do Docker)
- Node.js + pnpm (somente para o front em `web/`)

## Como Rodar

### 1. Subir a infraestrutura

```bash
docker compose up -d
```

Sobe:

- **PostgreSQL 17** (`user-service-postgres`) — porta `${POSTGRES_PORT_USER}`, com healthcheck (`pg_isready`)
- **MongoDB 8** (`task-service-mongodb`) — porta `${MONGO_PORT_TASK}`, com healthcheck (`mongosh ping`)
- **Redis 8** (`user-service-redis`) — porta `${REDIS_PORT_USER}`, autenticado por senha, com healthcheck

Todos os containers compartilham a network default do Compose (resolução por nome dentro do projeto).

### 2. Configurar variáveis de ambiente

Copie/ajuste o `.env` na raiz com as variáveis listadas em [Variáveis de Ambiente](#variáveis-de-ambiente). **Todas são obrigatórias**: os `application.yaml` não definem mais valor default para portas nem para URLs de serviços.

### 3. Subir os serviços

Ordem sugerida: como o `task-service` e o `bff-task-scheduler` dependem do `user-service` (e o `bff` depende dos três), suba o `user-service` primeiro.

```bash
# terminal 1
cd services/user-service
./mvnw spring-boot:run

# terminal 2
cd services/task-service
./mvnw spring-boot:run

# terminal 3 (opcional, se houver envio de e-mail)
cd services/notification-service
./mvnw spring-boot:run

# terminal 4 (BFF consumido pelo front)
cd services/bff-task-scheduler
./mvnw spring-boot:run
```

Ou via jar:

```bash
./mvnw clean package
java -jar target/<artifactId>-0.0.1-SNAPSHOT.jar
```

### 4. Subir o front

```bash
cd web
pnpm install
pnpm start
```

O front (Angular) sobe em `http://localhost:4200` e consome o `bff-task-scheduler` — o `CorsConfig` do BFF libera essa origem.

Swagger UI:

- `user-service` — `http://localhost:${API_PORT_USER}/swagger-ui.html`
- `bff-task-scheduler` — `http://localhost:${API_PORT_BFF}/swagger-ui.html`

## Docker dos Serviços

Cada serviço tem um `Dockerfile` multi-stage: build Maven em `eclipse-temurin:25-jdk-alpine`, runtime em `eclipse-temurin:25-jre-alpine` com o jar quebrado em camadas (`spring-boot-loader`, `dependencies`, `application`) e execução como usuário não-root `spring`.

```bash
cd services/<serviço>
docker build -t <serviço>:latest .
docker run --rm -p <porta>:<porta> --env-file ../../.env <serviço>:latest
```

A porta é controlada pelo `ARG SERVER_PORT` de cada Dockerfile (`user-service` 8081, `task-service` 8082, `notification-service` 8083, `bff-task-scheduler` 8080). O healthcheck do container consulta `GET /actuator/health`.

## Variáveis de Ambiente

Definidas em `.env` na raiz e consumidas pelos serviços via `spring.config.import: optional:file:.env[.properties]`.

| Variável | Descrição | Valor no `.env` |
|---|---|---|
| `API_PORT_BFF` | Porta HTTP do `bff-task-scheduler` | `8080` |
| `API_PORT_NOTIFICATION` | Porta HTTP do `notification-service` | `8083` |
| `API_PORT_TASK` | Porta HTTP do `task-service` | `8082` |
| `API_PORT_USER` | Porta HTTP do `user-service` | `8081` |
| `POSTGRES_HOST` | Host do PostgreSQL | `localhost` |
| `POSTGRES_PORT_USER` | Porta do PostgreSQL | `5432` |
| `POSTGRES_DATABASE_USER` | Nome do banco relacional | `user-service-db` |
| `POSTGRES_USERNAME_USER` | Usuário do PostgreSQL | `user-service` |
| `POSTGRES_PASSWORD_USER` | Senha do PostgreSQL | — |
| `MONGO_HOST` | Host do MongoDB | `localhost` |
| `MONGO_PORT_TASK` | Porta do MongoDB | `27017` |
| `MONGO_DATABASE_TASK` | Nome do banco documental | `task-service-mongodb` |
| `MONGO_USERNAME_TASK` | Usuário do MongoDB | `task-service` |
| `MONGO_PASSWORD_TASK` | Senha do MongoDB | — |
| `REDIS_HOST` | Host do Redis | `localhost` |
| `REDIS_PORT_USER` | Porta do Redis | `6379` |
| `REDIS_PASSWORD_USER` | Senha do Redis | — |
| `SECRET_KEY` | Chave base64 usada para assinar os tokens JWT | — |
| `EMAIL_USERNAME` | Usuário SMTP (Gmail) usado pelo `notification-service` | — |
| `EMAIL_PASSWORD` | Senha de app SMTP (Gmail) usada pelo `notification-service` | — |
| `USER_SERVICE_URL` | URL do `user-service` (Feign) | `http://localhost:8081` |
| `TASK_SERVICE_URL` | URL do `task-service` (Feign) | `http://localhost:8082` |
| `NOTIFICATION_SERVICE_URL` | URL do `notification-service` (Feign) | `http://localhost:8083` |
| `VIA_CEP_URL` | URL base da API ViaCEP (consumida pelo `user-service`) | `https://viacep.com.br` |

> O `.env` está listado no `.gitignore`. Versionar apenas o `.env.example`.

## Decisões de Arquitetura

- **Monorepo com `services/` e `web/`**: cada microsserviço é um projeto Maven isolado em sua própria pasta, o front Angular é um projeto pnpm separado, e todos compartilham o mesmo repositório git e a mesma infraestrutura Docker.
- **Infra compartilhada via Compose raiz**: PostgreSQL, MongoDB e Redis sobem juntos, na network default do Compose, evitando duplicação de containers entre serviços.
- **Bancos por serviço**: `user-service` usa PostgreSQL (dados relacionais: usuários, endereços, telefones); `task-service` usa MongoDB (tarefas como documentos). Cada container expõe apenas a porta do seu banco para o host.
- **Comunicação entre serviços via OpenFeign**: o `task-service` valida o usuário autenticado chamando o `user-service` via Feign Client (`UsuarioClient`), com URL configurável em `external-services.user-service.url` (`USER_SERVICE_URL`).
- **BFF agregador para o front**: o `bff-task-scheduler` é o único ponto de entrada HTTP do front (esperado em `http://localhost:4200` — Angular). Ele usa três Feign Clients (`UsuarioClient`, `TarefasClient`, `EmailClient`) para chamar os demais serviços, repassando o header `Authorization` adiante. Não há `SecurityFilterChain` no BFF — a autenticação é delegada ao `user-service`.
- **Cron no BFF**: o `bff-task-scheduler` agenda uma rotina (`cron.horario = 0/30 0/1 * * * ?` — a cada 30 min) para buscar tarefas pendentes no período e disparar o `notification-service`.
- **Notificação por e-mail (SMTP)**: o `notification-service` não tem dependência de banco — recebe um `TarefasDTO` e dispara um e-mail via Gmail SMTP (`smtp.gmail.com:587`, STARTTLS) renderizando o template Thymeleaf `templates/notificacao.html`. Credenciais vêm de `EMAIL_USERNAME` / `EMAIL_PASSWORD`.
- **Token JWT sem prefixo**: `POST /usuario/login` devolve o JWT cru (sem o prefixo `"Bearer "`), que o cliente envia no header `Authorization: Bearer <token>`.
- **Datas em `Instant`**: DTOs e payloads de erro padronizam `java.time.Instant` (ISO-8601) no lugar de `LocalDateTime`, evitando ambiguidade de fuso horário na serialização.
- **Config externalizada por `.env` central**: o `application.yaml` de cada serviço importa `optional:file:.env[.properties]`, então um único `.env` na raiz alimenta todos os serviços.
- **Healthchecks na infra**: Postgres usa `pg_isready`; MongoDB usa `mongosh ping`; Redis autentica via `REDISCLI_AUTH` antes de pingar.
