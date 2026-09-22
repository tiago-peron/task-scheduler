# scheduler-service

Microsserviço de CRUD e listagem de tarefas. Valida o usuário autenticado consumindo o `user-service` via OpenFeign.

## Stack

- Java 25 + Spring Boot 4.1.1 (Maven)
- Spring Web MVC, Spring Security
- Spring Data MongoDB
- Spring Cloud OpenFeign (cliente do `user-service`)
- JWT (jjwt 0.13.0) — autenticação stateless
- MapStruct 1.6.3 — mapeamento DTO ↔ Entity
- Lombok (com `lombok-mapstruct-binding`)

## Estrutura de Pacotes

Pacote raiz: `com.tiagoperon.schedulerservice`

```
com.tiagoperon.schedulerservice
├── SchedulerServiceApplication
├── business/                      # camada de serviço
│   ├── TarefasService
│   ├── dto/{TarefasDTO, UsuarioDTO}
│   └── mapper/{TarefasConverter, TarefaUpdateConverter}
├── controller/                    # camada REST
│   └── TarefasController          # /tarefas/**
└── infrastructure/                # adapters
    ├── entity/TarefasEntity       # @Document (MongoDB)
    ├── enums/StatusNotificacaoEnum
    ├── exceptions/{GlobalExceptionHandler, ResourceNotFoundException, UnauthorizedException}
    │   └── dto/ErrorResponseDTO
    ├── repository/TarefasRepository
    └── security/
        ├── {JwtUtil, JwtRequestFilter, SecurityConfig, UserDetailsServiceImpl}
        └── client/UsuarioClient   # @FeignClient → user-service
```

## Como Rodar Localmente

1. Subir a infraestrutura (Postgres + MongoDB + Redis) via Compose da raiz:

   ```bash
   docker compose -f ../../docker-compose.yaml up -d
   ```

2. Garantir que o `.env` da raiz esteja preenchido com `MONGO_HOST`, `MONGO_PORT_SCHEDULER`, `MONGO_USERNAME_SCHEDULER`, `MONGO_PASSWORD_SCHEDULER`, `MONGO_DATABASE_SCHEDULER`, `USER_SERVICE_URL` e `SECRET_KEY` (ver tabela no README raiz).

3. Subir o `user-service` em outro terminal — este serviço depende dele para validar tokens.

4. Rodar o serviço:

   ```bash
   ./mvnw spring-boot:run
   ```

   Ou via jar:

   ```bash
   ./mvnw clean package
   java -jar target/scheduler-service-0.0.1-SNAPSHOT.jar
   ```

## Endpoints

Base: `/tarefas`

| Método | Caminho | Auth | Descrição |
|---|---|---|---|
| POST | `/tarefas` | bearer | Cria uma nova tarefa para o usuário autenticado |
| GET | `/tarefas` | bearer | Lista as tarefas do usuário autenticado |
| GET | `/tarefas/eventos?dataInicial={ISO}&dataFinal={ISO}` | bearer | Lista tarefas pendentes no período informado |
| PUT | `/tarefas?id={id}` | bearer | Atualiza a tarefa informada |
| PATCH | `/tarefas?status={status}&id={id}` | bearer | Altera o status de notificação da tarefa |
| DELETE | `/tarefas?id={id}` | bearer | Remove a tarefa informada |

## Segurança

- **Stateless**: `SessionCreationPolicy.STATELESS`
- **JWT Bearer**: header `Authorization: Bearer <token>`, validado pelo `JwtRequestFilter`
- **Validação do usuário via Feign**: o `UserDetailsServiceImpl` chama o `user-service` (`UsuarioClient.buscaUsuarioPorEmail`) para confirmar que o e-mail do token existe e obter a senha para validação.
- **Todas as rotas exigem autenticação**: `anyRequest().authenticated()`.

## Configuração

`src/main/resources/application.yaml` lê do `.env` da raiz via `spring.config.import: optional:file:.env[.properties]`.

Variáveis consumidas por este serviço:

- `MONGO_HOST`, `MONGO_PORT_SCHEDULER`, `MONGO_USERNAME_SCHEDULER`, `MONGO_PASSWORD_SCHEDULER`, `MONGO_DATABASE_SCHEDULER`
- `SECRET_KEY` (assinatura JWT)
- `API_PORT_SCHEDULER` (porta HTTP — default `8080`)
- `USER_SERVICE_URL` (URL do `user-service` para o Feign Client — default `https://localhost:8080`)
