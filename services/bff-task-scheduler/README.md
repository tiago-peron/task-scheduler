# bff-task-scheduler

BFF (Backend for Frontend) que agrega os serviços `user-service`, `scheduler-service` e `notification-service` via OpenFeign, expondo uma API única para o front e disparando uma rotina periódica de notificações.

## Stack

- Java 25 + Spring Boot 4.1.1 (Maven)
- Spring Web MVC
- Spring Cloud OpenFeign (3 Feign Clients com tratamento de erro via `FeignConfig` / `FeignError`)
- SpringDoc OpenAPI 3.1.1 (Swagger UI)
- Lombok

## Estrutura de Pacotes

Pacote raiz: `com.tiagoperon.bfftaskscheduler`

```
com.tiagoperon.bfftaskscheduler
├── BffTaskSchedulerApplication
├── business/                      # camada de serviço
│   ├── CronService                # rotina periódica de notificações
│   ├── EmailService               # proxy para EmailClient
│   ├── TarefasService             # proxy para TarefasClient
│   ├── UsuarioService             # proxy para UsuarioClient
│   ├── dto/
│   │   ├── in/{LoginRequestDTO, UsuarioDTORequest, EnderecoDTOResquest, TelefoneDTORequest, TarefasDTORequest}
│   │   └── out/{UsuarioDTOResponse, EnderecoDTOResponse, TelefoneDTOResponse, TarefasDTOResponse, ViaCepDTOResponse}
│   └── enums/StatusNotificacaoEnum
├── controller/                    # camada REST
│   ├── TarefasController          # /tarefas/**
│   ├── UsuarioController          # /usuario/**
│   ├── GlobalExceptionHandler
│   └── ConfigDebug                # utilitário de debug
└── infrastructure/                # adapters
    ├── client/
    │   ├── {UsuarioClient, TarefasClient, EmailClient}
    │   └── config/{FeignConfig, FeignError}
    ├── config/SwaggerConfig
    ├── exceptions/{BusinessException, ConflictException, IllegalArgumentException,
    │               ResourceNotFoundException, UnauthorizedException}
    └── security/
        ├── SecurityConfig         # só define o esquema OpenAPI (sem SecurityFilterChain)
        └── CorsConfig             # libera http://localhost:4200 (Angular dev)
```

## Como Rodar Localmente

1. Subir a infraestrutura (Postgres + MongoDB + Redis) via Compose da raiz:

   ```bash
   docker compose -f ../../docker-compose.yaml up -d
   ```

2. Subir **na ordem** os demais serviços, pois o BFF delega a eles:

   ```bash
   # terminal 1
   cd ../user-service && ./mvnw spring-boot:run

   # terminal 2
   cd ../scheduler-service && ./mvnw spring-boot:run

   # terminal 3 (opcional, para disparo real de e-mail)
   cd ../notification-service && ./mvnw spring-boot:run
   ```

3. Configurar no `.env` da raiz as URLs dos serviços (`USER_SERVICE_URL`, `SCHEDULER_SERVICE_URL`, `NOTIFICATION_SERVICE_URL`) — já vêm com defaults apontando para as portas do `.env`. Ver tabela no README raiz.

4. Rodar o BFF:

   ```bash
   ./mvnw spring-boot:run
   ```

   Ou via jar:

   ```bash
   ./mvnw clean package
   java -jar target/bff-task-scheduler-0.0.1-SNAPSHOT.jar
   ```

5. Swagger UI: <http://localhost:${API_PORT_BFF:8080}/swagger-ui.html>

## Endpoints

Base: `/usuario`

| Método | Caminho | Auth | Descrição |
|---|---|---|---|
| POST | `/usuario` | público | Cadastra um novo usuário (delegado ao `user-service`) |
| POST | `/usuario/login` | público | Autentica e retorna JWT |
| GET | `/usuario?email={email}` | bearer* | Busca usuário por e-mail |
| DELETE | `/usuario/{email}` | bearer* | Remove usuário por e-mail |
| PUT | `/usuario` | bearer* | Atualiza dados do usuário autenticado |
| POST | `/usuario/endereco` | bearer* | Cadastra endereço para o usuário autenticado |
| PUT | `/usuario/endereco?id={id}` | bearer* | Atualiza endereço |
| POST | `/usuario/telefone` | bearer* | Cadastra telefone para o usuário autenticado |
| PUT | `/usuario/telefone?id={id}` | bearer* | Atualiza telefone |
| GET | `/usuario/endereco/{cep}` | público | Consulta endereço pelo CEP (ViaCEP) |

Base: `/tarefas`

| Método | Caminho | Auth | Descrição |
|---|---|---|---|
| POST | `/tarefas` | bearer* | Cria uma nova tarefa para o usuário autenticado |
| GET | `/tarefas` | bearer* | Lista as tarefas do usuário autenticado |
| GET | `/tarefas/eventos?dataInicial={ISO}&dataFinal={ISO}` | bearer* | Lista tarefas pendentes no período |
| PUT | `/tarefas?id={id}` | bearer* | Atualiza a tarefa |
| PATCH | `/tarefas?status={status}&id={id}` | bearer* | Altera o status de notificação |
| DELETE | `/tarefas?id={id}` | bearer* | Remove a tarefa |

> *Os endpoints marcados como `bearer` exigem `Authorization: Bearer <token>`, mas **a validação não é feita no BFF**: o token é repassado via `RequestHeader` para o `user-service` ou `scheduler-service`, que de fato autenticam a chamada.

## Segurança

- **Sem `SecurityFilterChain`**: o BFF **não** aplica autenticação localmente. A `SecurityConfig` existe apenas para registrar o esquema OpenAPI `bearerAuth` (HTTP Bearer, JWT) para a documentação Swagger.
- **Pass-through de token**: todos os endpoints autenticados recebem o header `Authorization` (opcional no controller) e o repassam para o serviço downstream via Feign Client.
- **CORS**: `CorsConfig` libera `http://localhost:4200` (Angular dev) para todos os métodos (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`) em `/**`.

## Cron de Notificações

`cron.horario = 0/30 0/1 * * * ?` — dispara a cada 30 minutos.

O `CronService` consulta o `scheduler-service` por tarefas pendentes no período atual e chama o `notification-service` para disparar os e-mails via SMTP.

## Configuração

`src/main/resources/application.yaml` lê do `.env` da raiz via `spring.config.import: optional:file:.env[.properties]`.

Variáveis consumidas por este serviço:

- `USER_SERVICE_URL` — URL do `user-service` para os Feign Clients (default `https://localhost:8080`)
- `SCHEDULER_SERVICE_URL` — URL do `scheduler-service` para os Feign Clients (default `https://localhost:8080`)
- `NOTIFICATION_SERVICE_URL` — URL do `notification-service` para o Feign Client (default `https://localhost:8080`)
- `API_PORT_BFF` — porta HTTP do BFF (default `8080`)
- `SECRET_KEY` — chave base64 usada para assinar os tokens JWT
- `cron.horario` — expressão cron fixa em `application.yaml` (não vem de env var)
