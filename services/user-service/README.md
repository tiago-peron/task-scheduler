# user-service

Microsserviço de cadastro, autenticação e gerenciamento de usuários, com endereços e telefones associados.

## Stack

- Java 25 + Spring Boot 4.1.1 (Maven)
- Spring Web, Spring Security, Spring Data JPA, Spring Data Redis
- Spring Cloud OpenFeign (cliente ViaCEP)
- JWT (jjwt 0.13.0) — autenticação stateless
- SpringDoc OpenAPI 3.1.1 (Swagger UI)
- PostgreSQL + Redis

## Estrutura de Pacotes

Pacote raiz: `com.tiagoperon.userservice`

```
com.tiagoperon.userservice
├── UserServiceApplication
├── business/                      # camada de serviço
│   ├── UsuarioService
│   ├── ViaCepService
│   ├── converter/UsuarioConverter
│   └── dto/{UsuarioDTO, EnderecoDTO, TelefoneDTO}
├── controller/                    # camada REST
│   ├── UsuarioController          # /usuario/**
│   └── GlobalExceptionHandler
└── infrastructure/                # adapters
    ├── clients/{ViaCepClient, ViaCepDTO}
    ├── config/RedisConfig
    ├── entity/{Usuario, Endereco, Telefone, DataInitializer}
    ├── exceptions/{Conflict, IllegalArgument, ResourceNotFound, Unauthorized}
    │   └── dto/ErrorResponseDTO
    ├── repository/{Usuario, Endereco, Telefone}Repository
    └── security/{JwtUtil, JwtRequestFilter, SecurityConfig, UserDetailsServiceImpl}
```

## Como Rodar Localmente

1. Subir a infraestrutura (Postgres + MongoDB + Redis) via Compose da raiz:

   ```bash
   docker compose -f ../../docker-compose.yaml up -d
   ```

2. Garantir que o `.env` da raiz esteja preenchido com `POSTGRES_*`, `REDIS_*` e `SECRET_KEY` (ver tabela no README raiz).

3. Rodar o serviço:

   ```bash
   ./mvnw spring-boot:run
   ```

   Ou via jar:

   ```bash
   ./mvnw clean package
   java -jar target/user-service-0.0.1-SNAPSHOT.jar
   ```

4. Swagger UI: <http://localhost:${API_PORT_USER:8080}/swagger-ui.html>

## Endpoints

Base: `/usuario`

| Método | Caminho | Auth | Descrição |
|---|---|---|---|
| POST | `/usuario` | público | Cadastra um novo usuário |
| POST | `/usuario/login` | público | Autentica e retorna JWT |
| GET | `/usuario?email={email}` | bearer | Busca usuário por e-mail |
| DELETE | `/usuario/{email}` | bearer | Remove usuário por e-mail |
| PUT | `/usuario` | bearer | Atualiza dados do usuário autenticado |
| POST | `/usuario/endereco` | bearer | Cadastra endereço para o usuário autenticado |
| PUT | `/usuario/endereco?id={id}` | bearer | Atualiza endereço |
| POST | `/usuario/telefone` | bearer | Cadastra telefone para o usuário autenticado |
| PUT | `/usuario/telefone?id={id}` | bearer | Atualiza telefone |
| GET | `/usuario/endereco/{cep}` | público | Consulta endereço pelo CEP (ViaCEP) |

## Segurança

- **Stateless**: `SessionCreationPolicy.STATELESS`
- **JWT Bearer**: header `Authorization: Bearer <token>`
- **Criptografia de senha**: `BCryptPasswordEncoder`
- **Rotas públicas**: `POST /usuario`, `POST /usuario/login`, `GET /usuario/endereco/**`, Swagger/OpenAPI (`/v3/api-docs/**`, `/swagger-ui/**`)
- **Esquema OpenAPI**: `bearerAuth` (HTTP Bearer, JWT)

## Configuração

`src/main/resources/application.yaml` lê do `.env` da raiz via `spring.config.import: optional:file:.env[.properties]`.

Variáveis consumidas por este serviço:

- `POSTGRES_HOST`, `POSTGRES_PORT_USER`, `POSTGRES_DATABASE_USER`, `POSTGRES_USERNAME_USER`, `POSTGRES_PASSWORD_USER`
- `REDIS_HOST`, `REDIS_PORT_USER`, `REDIS_PASSWORD_USER`
- `SECRET_KEY` (assinatura JWT)
- `API_PORT_USER` (porta HTTP — default `8080`)
- `VIA_CEP_URL` (integração ViaCEP — default `https://viacep.com.br`)
