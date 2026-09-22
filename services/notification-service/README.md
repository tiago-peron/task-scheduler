# notification-service

Microsserviço de envio de e-mails de notificação de tarefas. Recebe um `TarefasDTO` via REST e dispara um e-mail via SMTP usando um template Thymeleaf.

## Stack

- Java 25 + Spring Boot 4.1.1 (Maven)
- Spring Web MVC
- Spring Boot Mail (Jakarta Mail)
- Thymeleaf (template `templates/notificacao.html`)
- Lombok

## Estrutura de Pacotes

Pacote raiz: `com.tiagoperon.notificationservice`

```
com.tiagoperon.notificationservice
├── NotificationServiceApplication
├── business/                      # camada de serviço
│   ├── EmailService
│   ├── dto/TarefasDTO
│   └── enums/StatusNotificacaoEnum
├── controller/                    # camada REST
│   └── EmailController            # /email/**
└── infrastructure/                # adapters
    └── exceptions/EmailException
```

Resources:

```
src/main/resources/
├── application.yaml
└── templates/
    └── notificacao.html           # template Thymeleaf do corpo do e-mail
```

## Como Rodar Localmente

1. Subir a infraestrutura (Postgres + MongoDB + Redis) via Compose da raiz, caso vá rodar os demais serviços também:

   ```bash
   docker compose -f ../../docker-compose.yaml up -d
   ```

2. Preencher no `.env` da raiz as variáveis `EMAIL_USERNAME`, `EMAIL_PASSWORD` e `API_PORT_NOTIFICATION` (ver tabela no README raiz). Para Gmail, `EMAIL_PASSWORD` deve ser uma **senha de app** (não a senha da conta).

3. Rodar o serviço:

   ```bash
   ./mvnw spring-boot:run
   ```

   Ou via jar:

   ```bash
   ./mvnw clean package
   java -jar target/notification-service-0.0.1-SNAPSHOT.jar
   ```

## Endpoints

Base: `/email`

| Método | Caminho | Auth | Descrição |
|---|---|---|---|
| POST | `/email` | público | Envia um e-mail de notificação para o usuário da tarefa |

## Segurança

Este serviço **não** possui `SecurityConfig`: todos os endpoints estão abertos. É esperado que ele seja chamado apenas pela rede interna / pelo `scheduler-service` (ou via API Gateway), não diretamente pela internet.

## Configuração SMTP

`src/main/resources/application.yaml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
```

Variáveis consumidas por este serviço:

- `EMAIL_USERNAME` (usuário SMTP — Gmail)
- `EMAIL_PASSWORD` (senha de app SMTP)
- `API_PORT_NOTIFICATION` (porta HTTP — default `8080`)
