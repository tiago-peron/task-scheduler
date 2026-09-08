# Task Scheduler

Sistema de agendamento de tarefas baseado em microserviços.

## Arquitetura

- **Autenticação**: Spring Security com OAuth2 Resource Server (JWT), Sessões STATELESS, validação
  de token `type=ACCESS`, Authorities extraídas da claim `role` com prefixo `ROLE_`.

## Serviços

| Serviço | Descrição                 | Porta | Docs                                      |
|:-------:|---------------------------|:-----:|-------------------------------------------|
|  User   | Gerenciamento de Usuários | 8081  | [README](services/user-service/README.md) |

## Stack

- Java 25
- Spring Boot 4.1.1
- Spring Security (OAuth2 Resource Server / JWT)
- Spring Data JPA
- Maven
